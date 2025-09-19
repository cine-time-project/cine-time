package com.cinetime.payload.response.business;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HallMovieShowtimesResponse {
    private MovieMiniResponse movie;
    private List<LocalDateTime> times; // date+startTime
}
