package com.cinetime.controller.business;

import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// FavoritesController.java
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoritesController {
    private final FavoriteService favoriteService;

    // --- MOVIES ---
    @PreAuthorize("hasAnyAuthority('MEMBER','EMPLOYEE','ADMIN')")
    @PostMapping("/movies/{movieId}")
    public ResponseEntity<ResponseMessage<Void>> addMovieFavorite(
            @PathVariable Long movieId,
            Authentication auth) {
        var resp = favoriteService.addMovieFavorite(auth.getName(), movieId);
        return ResponseEntity.status(resp.getHttpStatus()).body(resp);
    }

    @PreAuthorize("hasAnyAuthority('MEMBER','EMPLOYEE','ADMIN')")
    @DeleteMapping("/movies/{movieId}")
    public ResponseEntity<ResponseMessage<Void>> removeMovieFavorite(
            @PathVariable Long movieId,
            Authentication auth) {
        var resp = favoriteService.removeMovieFavorite(auth.getName(), movieId);
        return ResponseEntity.status(resp.getHttpStatus()).body(resp);
    }

    // FE: FAVORITE_MOVIES_AUTH_API = /favorites/movies/auth
    @PreAuthorize("hasAnyAuthority('MEMBER','EMPLOYEE','ADMIN')")
    @GetMapping("/movies/auth")
    public ResponseEntity<ResponseMessage<List<Long>>> getMyMovieFavorites(Authentication auth) {
        var resp = favoriteService.getMyFavoriteMovieIds(auth.getName());
        return ResponseEntity.status(resp.getHttpStatus()).body(resp);
    }

    // --- (İsteğe bağlı) CINEMAS ---
    @PreAuthorize("hasAnyAuthority('MEMBER','EMPLOYEE','ADMIN')")
    @PostMapping("/cinemas/{cinemaId}")
    public ResponseEntity<ResponseMessage<Void>> addCinemaFavorite(
            @PathVariable Long cinemaId, Authentication auth) {
        // favoriteService.addCinemaFavorite(...)
        throw new UnsupportedOperationException("TODO");
    }

    @PreAuthorize("hasAnyAuthority('MEMBER','EMPLOYEE','ADMIN')")
    @DeleteMapping("/cinemas/{cinemaId}")
    public ResponseEntity<ResponseMessage<Void>> removeCinemaFavorite(
            @PathVariable Long cinemaId, Authentication auth) {
        // favoriteService.removeCinemaFavorite(...)
        throw new UnsupportedOperationException("TODO");
    }

    @PreAuthorize("hasAnyAuthority('MEMBER','EMPLOYEE','ADMIN')")
    @GetMapping("/cinemas/auth")
    public ResponseEntity<ResponseMessage<List<Long>>> getMyCinemaFavorites(Authentication auth) {
        // favoriteService.getMyFavoriteCinemaIds(...)
        throw new UnsupportedOperationException("TODO");
    }
}
