package com.cinetime.payload.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class FavoriteStatsResponse {
    private long totalFavorites;
    private long movieFavorites;
    private long cinemaFavorites;
}
