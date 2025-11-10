package com.cinetime.repository.business;

import com.cinetime.entity.business.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cinetime.entity.business.Movie;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
    boolean existsByUserIdAndCinemaId(Long userId, Long cinemaId);

    @Modifying void deleteByUserIdAndMovieId(Long userId, Long movieId);
    @Modifying void deleteByUserIdAndCinemaId(Long userId, Long cinemaId);

    @Query("select f.movie.id from Favorite f where f.user.id = :uid and f.movie is not null")
    List<Long> findMovieIdsByUserId(@Param("uid") Long userId);

    void deleteByMovie(Movie movie);

    void deleteAllByUser_Id(Long userId);


    @Query("""
    SELECT m.id, m.title, m.releaseDate, m.posterUrl, COUNT(f.id)
    FROM Favorite f
    JOIN f.movie m
    GROUP BY m.id, m.title, m.releaseDate, m.posterUrl
    ORDER BY COUNT(f.id) DESC
""")
    List<Object[]> findMovieFavoriteStats();


    @Query("""
    SELECT f.user.id, CONCAT(f.user.name, ' ', f.user.surname), f.user.email
    FROM Favorite f
    WHERE f.movie.id = :movieId
""")
    List<Object[]> findUsersByMovieId(@Param("movieId") Long movieId);




}
