package com.cinetime.payload.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private Long id;
    private Long userId;

    private Long movieId;
    private String movieTitle;
    private String movieSlug;

    private Long cinemaId;
    private String cinemaName;
    private String cinemaSlug;

    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
