package com.dsp.BookMyShow.Controller;

import com.dsp.BookMyShow.Service.BookingService;
import com.dsp.BookMyShow.dto.BookingResponse;
import com.dsp.BookMyShow.dto.CreateBookingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/shows/{showId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@PathVariable Long showId, @RequestBody CreateBookingRequest request){
        return bookingService.book(showId, request);
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancel(@PathVariable Long bookingId, @RequestParam Long profileId){
        return bookingService.cancel(bookingId, profileId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse find(@PathVariable Long bookingId){
        return bookingService.find(bookingId);
    }



}
