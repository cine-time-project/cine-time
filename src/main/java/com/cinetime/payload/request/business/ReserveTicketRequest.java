package com.cinetime.payload.request.business;



import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReserveTicketRequest {
    private Long movieId;
    private String cinema;      // cinema name
    private String hall;        // hall name
    private LocalDate date;     // show date
    private LocalTime startTime;// show start time
    private List<SeatInfo> seats;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SeatInfo {
        private String seatLetter;
        private int seatNumber;
    }
}