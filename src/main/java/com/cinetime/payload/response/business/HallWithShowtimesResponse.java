package com.cinetime.payload.response.business;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HallWithShowtimesResponse {
    private Long id;
    private String name;
    private Integer seatCapacity;
    private Boolean isSpecial;
    private List<HallMovieShowtimesResponse> movies;
}
