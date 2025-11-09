package com.cinetime.controller.business;

import com.cinetime.payload.response.business.FavoritedUserResponse;
import com.cinetime.payload.response.business.MovieStatsResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.security.service.UserDetailsImpl;
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

    private final FavoriteService svc;

    // Filmler
    @GetMapping("/movies/auth")
    public List<Long> myMovieFavs(Authentication auth) {
        Long uid = principalId(auth);
        return svc.listMovieIds(uid);
    }

    @PostMapping("/movies/{movieId}")
    public void addMovie(Authentication auth, @PathVariable("movieId") Long movieId) {
        svc.addMovieFavorite(principalId(auth), movieId);
    }

    @DeleteMapping("/movies/{movieId}")
    public void removeMovie(Authentication auth, @PathVariable("movieId") Long movieId) {
        svc.removeMovieFavorite(principalId(auth), movieId);
    }

    private Long principalId(Authentication auth) {
        return ((UserDetailsImpl) auth.getPrincipal()).getId(); // senin principal tipin
    }

    // ADMIN veya EMPLOYEE için: tüm filmler için favori istatistiklerini getir
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @GetMapping("/movies/stats")
    public ResponseEntity<ResponseMessage<List<MovieStatsResponse>>> getAllMovieFavoriteStats() {
        List<MovieStatsResponse> stats = svc.getMovieFavoriteStats();
        ResponseMessage<List<MovieStatsResponse>> resp = ResponseMessage.<List<MovieStatsResponse>>builder()
                .httpStatus(org.springframework.http.HttpStatus.OK)
                .message("Movie favorite statistics retrieved successfully")
                .returnBody(stats)
                .build();
        return ResponseEntity.ok(resp);
    }

    // ADMIN veya EMPLOYEE için: bir filmi favorileyen tüm kullanıcıları getir
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @GetMapping("/movies/{movieId}/users")
    public ResponseEntity<ResponseMessage<List<FavoritedUserResponse>>> getUsersWhoFavorited(
            @PathVariable("movieId") Long movieId) {
        List<FavoritedUserResponse> users = svc.getUsersWhoFavoritedMovieDto(movieId);

        ResponseMessage<List<FavoritedUserResponse>> resp = ResponseMessage.<List<FavoritedUserResponse>>builder()
                .httpStatus(org.springframework.http.HttpStatus.OK)
                .message("Users who favorited the movie retrieved successfully")
                .returnBody(users)
                .build();
        return ResponseEntity.ok(resp);
    }
}
