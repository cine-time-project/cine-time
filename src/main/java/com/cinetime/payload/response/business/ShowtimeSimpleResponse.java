package com.cinetime.payload.response.business;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShowtimeSimpleResponse {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    //Movie Data
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private String movieSlug;
    //Hall Data
    private Long hallId;
    //CinemaData
    private Long cinemaId;
    private Long cityId;

}
