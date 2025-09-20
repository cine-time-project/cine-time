package com.cinetime.repository.business;


import com.cinetime.entity.business.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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
}