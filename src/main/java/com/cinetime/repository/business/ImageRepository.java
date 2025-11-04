package com.cinetime.repository.business;


import com.cinetime.entity.business.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ImageRepository extends JpaRepository<Image, Long> {


    boolean existsByMovieIdAndIsPosterTrue(Long movieId);

    /** Check if there is another poster for the same movie (excluding current id). */
    @Query("""
           select (count(i) > 0)
           from Image i
           where i.movie.id = :movieId and i.isPoster = true and i.id <> :excludeId
           """)
    boolean existsByMovieIdAndIsPosterTrueAndIdNot(Long movieId, Long excludeId);

    /** Optional listing helper for admin screens. */
    List<Image> findByMovie_IdOrderByCreatedAtDesc(Long movieId);

    Set<Image> findAllByIdIn(Set<Long> ids);

    @Query("select i.id from Image i where i.movie.id = :movieId and i.isPoster = true")
    Optional<Long> findPosterImageIdByMovieId(@Param("movieId") Long movieId);

    /**
     * Find images by movie ID with pagination
     */
    org.springframework.data.domain.Page<Image> findByMovieId(
            Long movieId,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * Search images by name (case-insensitive) with pagination
     */
    org.springframework.data.domain.Page<Image> findByNameContainingIgnoreCase(
            String name,
            org.springframework.data.domain.Pageable pageable
    );

}