package com.cinetime.payload.response.business;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeResponse {
    private Long id;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    //Hall/Cinema
    private Long hallId;
    private String hallName;
    private Long cinemaId;
    private String cinemaName;

    //Movie
    private Long movieId;
    private String movieTitle;

    private Long cityId;
    private String cityName;
    private Long countryId;
    private String countryName;


}
