package com.dsp.BookMyShow.repository;

import com.dsp.BookMyShow.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Query("SELECT s.seatLabel FROM ShowSeat s " +
            "WHERE s.show.id = :showId " +
            "AND s.reserved = false " +
            "ORDER BY s.id")
    List<String> findAvailableLabels(@Param("showId") Long showId);

    @Query("Select s from ShowSeat s where s.show.id=:showId and s.seatLabel in :labels")
    List<ShowSeat> findForUpdate(@Param("showId") Long showId, @Param("labels") List<String> labels);
}
