package com.cinetime.payload.response.business;

import com.cinetime.entity.business.Image;
import com.cinetime.entity.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String movieName;
    private String cinema;
    private String hall;
    private LocalDate date;
    private LocalTime startTime;
    private String seatLetter;
    private int seatNumber;
    private TicketStatus status;
    private Double price;

    private Long movieId;


    // NEW: public URL for the movie's image (external URL if present; otherwise your /api/cinemaimages/{cinemaId} endpoint)
    private String moviePosterUrl;

    // Backward‑compat constructor (old 10‑arg signature). Any legacy `new TicketResponse(...)` calls still work.
    public TicketResponse(Long id,
                          String movieName,
                          String cinema,
                          String hall,
                          LocalDate date,
                          LocalTime startTime,
                          String seatLetter,
                          int seatNumber,
                          TicketStatus status,
                          Double price) {
        this(id, movieName, cinema, hall, date, startTime, seatLetter, seatNumber, status, price,null,null);
    }
}
