package com.cinetime.service;


import com.cinetime.payload.dto.request.FavoriteCreateRequest;
import com.cinetime.payload.dto.response.FavoriteResponse;
import com.cinetime.payload.dto.response.FavoriteStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {
    FavoriteResponse addFavorite(FavoriteCreateRequest request);

    Page<FavoriteResponse> getUserFavorites(Pageable pageable);
    Page<FavoriteResponse> getUserMovieFavorites(Pageable pageable);
    Page<FavoriteResponse> getUserCinemaFavorites(Pageable pageable);

    void removeFavorite(Long favoriteId);
    void removeFavoriteByMovieId(Long movieId);
    void removeFavoriteByCinemaId(Long cinemaId);

    boolean isMovieFavorite(Long movieId);
    boolean isCinemaFavorite(Long cinemaId);

    FavoriteStatsResponse getFavoriteStats();
    long getFavoriteCountForMovie(Long movieId);
    long getFavoriteCountForCinema(Long cinemaId);
}