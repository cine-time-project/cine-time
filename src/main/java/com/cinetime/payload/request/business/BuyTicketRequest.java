package com.cinetime.payload.request.business;

import com.cinetime.entity.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyTicketRequest {
    @NotNull(message = "Movie name can not be null")
    String movieName;
    @NotNull(message = "Cinema can not be null")
    String cinema;
    @NotNull(message = "Moviehall can not be null")
    String hall;
    @NotNull(message = "Show start time can not be null")
    LocalTime showtime;
    Integer count;
    @NotNull(message = "Seat has to be selected")
    List<SeatInfo> seatInformation;
    LocalDate date;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatInfo {
        private String seatLetter;
        private int seatNumber;
    }






}
