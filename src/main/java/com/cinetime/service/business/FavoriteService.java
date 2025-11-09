package com.cinetime.service.business;

import com.cinetime.entity.business.Favorite;
import com.cinetime.payload.response.business.MovieStatsResponse;
import com.cinetime.payload.response.business.FavoritedUserResponse;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.FavoriteRepository;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favRepo;
    private final MovieRepository movieRepo;
    private final CinemaRepository cinemaRepo;
    private final UserRepository userRepo;

    public List<Long> listMovieIds(Long userId) {
        return favRepo.findMovieIdsByUserId(userId);
    }

    @Transactional
    public void addMovieFavorite(Long userId, Long movieId) {
        if (!favRepo.existsByUserIdAndMovieId(userId, movieId)) {
            Favorite f = new Favorite();
            f.setUser(userRepo.getReferenceById(userId));
            f.setMovie(movieRepo.getReferenceById(movieId));
            f.setCinema(null);
            favRepo.save(f);
        }
    }

    @Transactional
    public void removeMovieFavorite(Long userId, Long movieId) {
        favRepo.deleteByUserIdAndMovieId(userId, movieId);
    }

    @Transactional
    public void addCinemaFavorite(Long userId, Long cinemaId) {
        if (!favRepo.existsByUserIdAndCinemaId(userId, cinemaId)) {
            Favorite f = new Favorite();
            f.setUser(userRepo.getReferenceById(userId));
            f.setCinema(cinemaRepo.getReferenceById(cinemaId));
            f.setMovie(null); // <-- ZORUNLU
            favRepo.save(f);
        }
    }

    @Transactional
    public void removeCinemaFavorite(Long userId, Long cinemaId) {
        favRepo.deleteByUserIdAndCinemaId(userId, cinemaId);
    }

    public List<MovieStatsResponse> getMovieFavoriteStats() {
        List<Object[]> rows = favRepo.findMovieFavoriteStats();
        return rows.stream()
                .map(r -> new MovieStatsResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        r[2] != null ? r[2].toString() : null,
                        (String) r[3],
                        ((Number) r[4]).longValue()
                ))
                .toList();
    }



    public List<Map<String, Object>> getUsersWhoFavoritedMovie(Long movieId) {
        List<Object[]> rows = favRepo.findUsersByMovieId(movieId);
        return rows.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", ((Number) r[0]).longValue());
                    map.put("username", (String) r[1]);
                    map.put("email", (String) r[2]);
                    return map;
                })
                .toList();
    }


    public List<FavoritedUserResponse> getUsersWhoFavoritedMovieDto(Long movieId) {
        List<Object[]> rows = favRepo.findUsersByMovieId(movieId);
        return rows.stream()
                .map(r -> new FavoritedUserResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        (String) r[2]
                ))
                .toList();
    }
}

