package com.cinetime.service.impl;


import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.dto.request.FavoriteCreateRequest;
import com.cinetime.payload.dto.response.FavoriteResponse;
import com.cinetime.payload.dto.response.FavoriteStatsResponse;
import com.cinetime.entity.Cinema;
import com.cinetime.entity.Favorite;
import com.cinetime.entity.Movie;
import com.cinetime.exception.ConflictException;
import com.cinetime.repository.CinemaRepository;
import com.cinetime.repository.FavoriteRepository;
import com.cinetime.repository.MovieRepository;
import com.cinetime.security.UserService;
//import com.cinetime.security.service.UserService;
import com.cinetime.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final UserService userService;

    @Override
    @Transactional
    public FavoriteResponse addFavorite(FavoriteCreateRequest request) {
        Long uid = userService.getCurrentUserId();

        if (request.getMovieId() != null && request.getCinemaId() != null) {
            throw new IllegalArgumentException("Cannot favorite both movie and cinema in a single request");
        }
        if (request.getMovieId() == null && request.getCinemaId() == null) {
            throw new IllegalArgumentException("Either movieId or cinemaId must be provided");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(uid);

        if (request.getMovieId() != null) {
            Long movieId = request.getMovieId();
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

            if (favoriteRepository.existsByUserIdAndMovieId(uid, movieId)) {
                throw new ConflictException("Movie is already in favorites");
            }
            favorite.setMovieId(movie.getId());

        } else {
            Long cinemaId = request.getCinemaId();
            Cinema cinema = cinemaRepository.findById(cinemaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + cinemaId));

            if (favoriteRepository.existsByUserIdAndCinemaId(uid, cinemaId)) {
                throw new ConflictException("Cinema is already in favorites");
            }
            favorite.setCinemaId(cinema.getId());
        }

        Favorite saved = favoriteRepository.save(favorite);
        log.info("Favorite added (user={}, movieId={}, cinemaId={})", uid, saved.getMovieId(), saved.getCinemaId());
        return mapToFavoriteResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteResponse> getUserFavorites(Pageable pageable) {
        Long uid = userService.getCurrentUserId();
        return favoriteRepository.findByUserId(uid, pageable)
                .map(this::mapToFavoriteResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteResponse> getUserMovieFavorites(Pageable pageable) {
        Long uid = userService.getCurrentUserId();
        return favoriteRepository.findMoviesFavoritesByUserId(uid, pageable)
                .map(this::mapToFavoriteResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteResponse> getUserCinemaFavorites(Pageable pageable) {
        Long uid = userService.getCurrentUserId();
        return favoriteRepository.findCinemasFavoritesByUserId(uid, pageable)
                .map(this::mapToFavoriteResponse);
    }

    @Override
    @Transactional
    public void removeFavorite(Long favoriteId) {
        Long uid = userService.getCurrentUserId();

        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found with id: " + favoriteId));

        if (!favorite.getUserId().equals(uid)) {
            throw new AccessDeniedException("You cannot remove another user's favorite");
        }

        favoriteRepository.delete(favorite);
        log.info("Favorite removed (id={}, user={})", favoriteId, uid);
    }

    @Override
    @Transactional
    public void removeFavoriteByMovieId(Long movieId) {
        Long uid = userService.getCurrentUserId();
        Favorite favorite = favoriteRepository.findByUserIdAndMovieId(uid, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie favorite not found"));
        favoriteRepository.delete(favorite);
        log.info("Movie favorite removed (user={}, movieId={})", uid, movieId);
    }

    @Override
    @Transactional
    public void removeFavoriteByCinemaId(Long cinemaId) {
        Long uid = userService.getCurrentUserId();
        Favorite favorite = favoriteRepository.findByUserIdAndCinemaId(uid, cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema favorite not found"));
        favoriteRepository.delete(favorite);
        log.info("Cinema favorite removed (user={}, cinemaId={})", uid, cinemaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMovieFavorite(Long movieId) {
        Long uid = userService.getCurrentUserId();
        return favoriteRepository.existsByUserIdAndMovieId(uid, movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCinemaFavorite(Long cinemaId) {
        Long uid = userService.getCurrentUserId();
        return favoriteRepository.existsByUserIdAndCinemaId(uid, cinemaId);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteStatsResponse getFavoriteStats() {
        Long uid = userService.getCurrentUserId();
        return new FavoriteStatsResponse(
                favoriteRepository.countByUserId(uid),
                favoriteRepository.countByUserIdAndMovieIdIsNotNull(uid),
                favoriteRepository.countByUserIdAndCinemaIdIsNotNull(uid)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long getFavoriteCountForMovie(Long movieId) {
        return favoriteRepository.countFavoritesByMovieId(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFavoriteCountForCinema(Long cinemaId) {
        return favoriteRepository.countFavoritesByCinemaId(cinemaId);
    }

    private FavoriteResponse mapToFavoriteResponse(Favorite f) {
        FavoriteResponse r = new FavoriteResponse();
        r.setId(f.getId());
        r.setUserId(f.getUserId());
        r.setCreatedAt(f.getCreatedAt());
        r.setUpdatedAt(f.getUpdatedAt());

        if (f.getMovieId() != null) {
            r.setMovieId(f.getMovieId());
            r.setType("MOVIE");
            // İsteğe bağlı zenginleştirme (N+1’e dikkat): küçük listelerde sorun olmaz
            movieRepository.findById(f.getMovieId()).ifPresent(m -> {
                r.setMovieTitle(m.getTitle());
                r.setMovieSlug(m.getSlug());
            });
        } else if (f.getCinemaId() != null) {
            r.setCinemaId(f.getCinemaId());
            r.setType("CINEMA");
            cinemaRepository.findById(f.getCinemaId()).ifPresent(c -> {
                r.setCinemaName(c.getName());
                r.setCinemaSlug(c.getSlug());
            });
        }
        return r;
    }
}