package com.cinetime.service.business;

import com.cinetime.entity.business.Favorite;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.FavoriteRepository;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

// FavoriteService.java
@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepo;
    private final UserRepository userRepo;
    private final MovieRepository movieRepo;
    private final CinemaRepository cinemaRepo;

    public ResponseMessage<Void> addMovieFavorite(String username, Long movieId) {
        var user = userRepo.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User"));
        var movie = movieRepo.findById(movieId).orElseThrow(() -> new ResourceNotFoundException("Movie"));

        if (favoriteRepo.existsByUserIdAndMovieId(user.getId(), movie.getId())) {
            return ResponseMessage.<Void>builder()
                    .message("Already in favorites")
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }
        var fav = new Favorite();
        fav.setUser(user);
        fav.setMovie(movie);
        fav.setCinema(null);
        fav.setCreatedAt(OffsetDateTime.now().toLocalDateTime());
        fav.setUpdatedAt(OffsetDateTime.now().toLocalDateTime());
        favoriteRepo.save(fav);

        return ResponseMessage.<Void>builder()
                .message("Movie added to favorites")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<Void> removeMovieFavorite(String username, Long movieId) {
        var user = userRepo.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User"));
        var opt = favoriteRepo.findByUserIdAndMovieId(user.getId(), movieId);
        if (opt.isEmpty()) {
            return ResponseMessage.<Void>builder()
                    .message("Favorite not found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
        }
        favoriteRepo.delete(opt.get());
        return ResponseMessage.<Void>builder()
                .message("Movie removed from favorites")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // (İsteğe bağlı) listeleme – FE’de FAVORITE_MOVIES_AUTH_API için
    public ResponseMessage<List<Long>> getMyFavoriteMovieIds(String username) {
        var user = userRepo.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User"));
        var list = favoriteRepo.findAllByUserIdAndMovieIdIsNotNull(user.getId())
                .stream().map(f -> f.getMovie().getId()).toList();
        return ResponseMessage.<List<Long>>builder()
                .message("OK")
                .httpStatus(HttpStatus.OK)
                .returnBody(list)
                .build();
    }

    // Benzeri sinema için de yazılabilir (add/remove/list)
}
