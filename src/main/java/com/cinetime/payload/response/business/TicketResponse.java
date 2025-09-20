package com.cinetime.payload.response.business;

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






}
