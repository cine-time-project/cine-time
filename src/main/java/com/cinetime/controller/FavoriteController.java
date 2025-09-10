package com.cinetime.controller;

import com.cinetime.payload.dto.request.FavoriteCreateRequest;
import com.cinetime.payload.dto.response.FavoriteResponse;
import com.cinetime.payload.dto.response.FavoriteStatsResponse;
import com.cinetime.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // C02: GET /api/favorites/auth?type=movie|cinema (paging + sort)
    @GetMapping("/auth")
    public ResponseEntity<Page<FavoriteResponse>> getUserFavorites(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false)      String type) {

        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<FavoriteResponse> favorites = switch (type == null ? "" : type.toLowerCase()) {
            case "movie"  -> favoriteService.getUserMovieFavorites(pageable);
            case "cinema" -> favoriteService.getUserCinemaFavorites(pageable);
            default       -> favoriteService.getUserFavorites(pageable);
        };
        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    public ResponseEntity<FavoriteResponse> addFavorite(@Valid @RequestBody FavoriteCreateRequest request) {
        FavoriteResponse response = favoriteService.addFavorite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        favoriteService.removeFavorite(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/movie/{movieId}")
    public ResponseEntity<Void> removeFavoriteByMovie(@PathVariable Long movieId) {
        favoriteService.removeFavoriteByMovieId(movieId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cinema/{cinemaId}")
    public ResponseEntity<Void> removeFavoriteByCinema(@PathVariable Long cinemaId) {
        favoriteService.removeFavoriteByCinemaId(cinemaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movie/{movieId}/check")
    public ResponseEntity<Boolean> isMovieFavorite(@PathVariable Long movieId) {
        return ResponseEntity.ok(favoriteService.isMovieFavorite(movieId));
    }

    @GetMapping("/cinema/{cinemaId}/check")
    public ResponseEntity<Boolean> isCinemaFavorite(@PathVariable Long cinemaId) {
        return ResponseEntity.ok(favoriteService.isCinemaFavorite(cinemaId));
    }

    @GetMapping("/stats")
    public ResponseEntity<FavoriteStatsResponse> getFavoriteStats() {
        return ResponseEntity.ok(favoriteService.getFavoriteStats());
    }

    @GetMapping("/movie/{movieId}/count")
    public ResponseEntity<Long> getMovieFavoriteCount(@PathVariable Long movieId) {
        return ResponseEntity.ok(favoriteService.getFavoriteCountForMovie(movieId));
    }

    @GetMapping("/cinema/{cinemaId}/count")
    public ResponseEntity<Long> getCinemaFavoriteCount(@PathVariable Long cinemaId) {
        return ResponseEntity.ok(favoriteService.getFavoriteCountForCinema(cinemaId));
    }
}
