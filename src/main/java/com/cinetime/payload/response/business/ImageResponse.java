package com.cinetime.payload.response.business;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ImageResponse {
    private Long id;
    private String name;
    private String type;     // may be null per schema
    private boolean poster;
    private Long movieId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String url;      // self link (e.g., /api/images/{id})
}