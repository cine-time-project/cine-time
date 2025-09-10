package com.cinetime.repository;

import com.cinetime.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    @Query("select f from Favorite f where f.userId = :userId and f.movieId is not null")
    Page<Favorite> findMoviesFavoritesByUserId(Long userId, Pageable pageable);

    @Query("select f from Favorite f where f.userId = :userId and f.cinemaId is not null")
    Page<Favorite> findCinemasFavoritesByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
    boolean existsByUserIdAndCinemaId(Long userId, Long cinemaId);

    Optional<Favorite> findByUserIdAndMovieId(Long userId, Long movieId);
    Optional<Favorite> findByUserIdAndCinemaId(Long userId, Long cinemaId);

    @Query("select count(f) from Favorite f where f.movieId = :movieId")
    long countFavoritesByMovieId(Long movieId);

    @Query("select count(f) from Favorite f where f.cinemaId = :cinemaId")
    long countFavoritesByCinemaId(Long cinemaId);

    // Direct COUNT queries for Stats — performant
    long countByUserId(Long userId);
    long countByUserIdAndMovieIdIsNotNull(Long userId);
    long countByUserIdAndCinemaIdIsNotNull(Long userId);

}
