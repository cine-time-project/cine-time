package com.cinetime.payload.response.business;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CinemaDetailedResponse {
    private Long id;
    private String name;
    private String slug;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CityMiniResponse city;
    private Set<HallResponse> halls;
    private Set<MovieMiniResponse> movies;
}
