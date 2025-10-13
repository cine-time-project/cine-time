package com.cinetime.payload.response.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieWithShowtimesResponse {
    private CinemaMovieResponse movie;
    private List<ShowtimeSimpleResponse> showtimes;


}
