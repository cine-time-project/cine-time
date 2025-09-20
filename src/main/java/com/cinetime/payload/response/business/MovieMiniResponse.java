package com.cinetime.payload.response.business;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MovieMiniResponse {
    private Long id;
    private String title;
}
