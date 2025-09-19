package com.cinetime.payload.request;

import com.cinetime.entity.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyTicketRequest {
    String movieName;
    String cinema;
    String hall;
    LocalDateTime showtime;
    Integer count;
    List<SeatInfo> seatInformation;

     //Yukaridaki seatInfo bilgilerini saklamak icin ChatGpt yeni bir entity olusturmak yerine sadece burda bi static
    //yardimci bir static inner class yapmanin daha mantikli oldugunu soyledi. Sonradan degistirebiliriz.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatInfo {
        private String seatLetter;
        private int seatNumber;
    }






}
