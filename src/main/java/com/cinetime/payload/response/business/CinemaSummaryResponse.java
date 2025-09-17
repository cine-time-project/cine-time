package com.cinetime.payload.response.business;

import lombok.*;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CinemaSummaryResponse {
    private Long id;
    private String name;
    private Set<CityMiniResponse> cities; // Many-to-Many
}
