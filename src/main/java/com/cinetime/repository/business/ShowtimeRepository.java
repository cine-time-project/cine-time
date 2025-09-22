package com.cinetime.repository.business;

import com.cinetime.entity.business.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    @Query("""
        select
            h.id           as hallId,
            h.name         as hallName,
            h.seatCapacity as seatCapacity,
            h.isSpecial    as isSpecial,
            m.id           as movieId,
            m.title        as movieTitle,
            s.date         as date,
            s.startTime    as startTime
        from Showtime s
        join s.hall  h
        join s.movie m
        where h.cinema.id = :cinemaId
        order by h.name asc, m.title asc, s.date asc, s.startTime asc
    """)
    List<HallMovieTimeRow> findShowtimesByCinemaId(@Param("cinemaId") Long cinemaId);

    Page<Showtime> findByDate(LocalDate date, Pageable pageable);

    Page<Showtime> findByDateAndStartTimeAfter(LocalDate date, LocalTime time, Pageable pageable);

    interface HallMovieTimeRow {
        Long getHallId();
        String getHallName();
        Integer getSeatCapacity();
        Boolean getIsSpecial();
        Long getMovieId();
        String getMovieTitle();
        java.time.LocalDate getDate();
        java.time.LocalTime getStartTime();
    }


}