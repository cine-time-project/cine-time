package com.cinetime.payload.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String name;
    private String type;
    private boolean isPoster;
    private Long movieId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}