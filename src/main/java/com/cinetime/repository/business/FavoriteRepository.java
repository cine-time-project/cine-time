package com.cinetime.repository.business;

import com.cinetime.entity.business.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// FavoriteRepository.java
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
    boolean existsByUserIdAndCinemaId(Long userId, Long cinemaId);

    Optional<Favorite> findByUserIdAndMovieId(Long userId, Long movieId);
    Optional<Favorite> findByUserIdAndCinemaId(Long userId, Long cinemaId);

    void deleteByUserIdAndMovieId(Long userId, Long movieId);
    void deleteByUserIdAndCinemaId(Long userId, Long cinemaId);

    List<Favorite> findAllByUserIdAndMovieIdIsNotNull(Long userId);   // film favorileri
    List<Favorite> findAllByUserIdAndCinemaIdIsNotNull(Long userId);  // sinema favorileri
}
