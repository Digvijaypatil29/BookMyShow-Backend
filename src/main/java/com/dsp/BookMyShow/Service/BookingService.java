package com.dsp.BookMyShow.Service;

import com.dsp.BookMyShow.dto.BookingResponse;
import com.dsp.BookMyShow.dto.CreateBookingRequest;
import com.dsp.BookMyShow.entity.Booking;
import com.dsp.BookMyShow.entity.BookingStatus;
import com.dsp.BookMyShow.entity.Show;
import com.dsp.BookMyShow.entity.ShowSeat;
import com.dsp.BookMyShow.repository.BookingRepository;
import com.dsp.BookMyShow.repository.CustomerRepository;
import com.dsp.BookMyShow.repository.ShowRepository;
import com.dsp.BookMyShow.repository.ShowSeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class BookingService {

    private final ShowRepository showRepository;

    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    public BookingService(ShowRepository showRepository, ShowSeatRepository showSeatRepository, BookingRepository bookingRepository, CustomerRepository customerRepository) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public BookingResponse book(Long showId, CreateBookingRequest request){
        Show show = showRepository.findById(showId).orElseThrow(() -> new ResourceNotFoundException("Show Not Found"));
        var customer = customerRepository.findById(request.profileId()).orElseThrow(()-> new ResourceNotFoundException("Profile Not Found"));

        List<String> labels = request.seatLabels().stream()
                .map(lable -> lable.trim().toUpperCase(Locale.ROOT)).toList();

        if(labels.stream().distinct().count() != labels.size()){
            throw new SeatUnavailableException("Duplicate seat labels are not allowed");
        }

        List<ShowSeat> seats = showSeatRepository.findForUpdate(showId, labels);
        if(seats.size() != labels.size() || seats.stream().anyMatch(ShowSeat::isReserved)){
            throw new SeatUnavailableException("One or more selected seats are unavailable");
        }

        seats.forEach(ShowSeat::reserve);
        show.reserve(labels.size());

        BigDecimal total = show.getTicketPrice().multiply(BigDecimal.valueOf(labels.size()));

        Booking booking = bookingRepository.save(new Booking(show, customer, total, labels));

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse find(Long bookingId){

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return BookingResponse.from(booking);
    }

    @Transactional
    public List<BookingResponse> findByProfileId(Long profileId){
        if(!customerRepository.existsById(profileId)){
            throw new ResourceNotFoundException("Profile not found");
        }

        List<BookingResponse> list = bookingRepository.findByCustomerIdOrderByBookedAtDesc(profileId).stream()
                .map(BookingResponse::from).toList();
        return list;
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, Long profileId){
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking Not Found"));
        if(booking.getStatus()== BookingStatus.CONFIRMED){
            List<ShowSeat> seats = showSeatRepository.findForUpdate(booking.getShow().getId(), booking.getSeatLabels());
            seats.forEach(ShowSeat::reserve);
            booking.getShow().release(booking.getSeatLabels().size());
            booking.cancel();
        }
        return BookingResponse.from(booking);
    }


}
