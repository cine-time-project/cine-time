package com.cinetime.repository.business;

import java.time.LocalDate;
import java.time.LocalTime;

public interface HallMovieTimeRow {
    Long getHallId();
    String getHallName();
    Integer getSeatCapacity();
    Boolean getIsSpecial();

    Long getMovieId();
    String getMovieTitle();   // Movie.title alias'ı ile eşleşecek

    LocalDate getDate();      // Showtime.date
    LocalTime getStartTime(); // Showtime.startTime
}
