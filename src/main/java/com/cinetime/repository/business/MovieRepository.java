package com.cinetime.repository.business;

import com.cinetime.entity.business.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie,Long> {

    Page<Movie> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String title, String summary, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.cinemas c WHERE LOWER(c.slug) = LOWER(:cinemaSlug)")
    Page<Movie> findAllByCinemaSlugIgnoreCase(@Param("cinemaSlug") String cinemaSlug, Pageable pageable);


    @Query("SELECT DISTINCT m FROM Movie m JOIN m.cinemas c JOIN c.halls h WHERE LOWER(h.name) = LOWER(:hallName)")
    Page<Movie> findAllByHallIgnoreCase(@Param("hallName") String hallName, Pageable pageable);

}
