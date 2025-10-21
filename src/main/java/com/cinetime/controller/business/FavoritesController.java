package com.cinetime.controller.business;

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
    public void addMovie(Authentication auth, @PathVariable Long movieId) {
        svc.addMovieFavorite(principalId(auth), movieId);
    }

    @DeleteMapping("/movies/{movieId}")
    public void removeMovie(Authentication auth, @PathVariable Long movieId) {
        svc.removeMovieFavorite(principalId(auth), movieId);
    }

    private Long principalId(Authentication auth) {
        return ((UserDetailsImpl) auth.getPrincipal()).getId(); // senin principal tipin
    }
}
