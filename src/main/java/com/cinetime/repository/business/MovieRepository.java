package com.cinetime.repository.business;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie,Long> {

    Page<Movie> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String title, String summary, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.cinemas c WHERE LOWER(c.slug) = LOWER(:cinemaSlug)")
    Page<Movie> findAllByCinemaSlugIgnoreCase(@Param("cinemaSlug") String cinemaSlug, Pageable pageable);


    @Query("SELECT DISTINCT m FROM Movie m JOIN m.cinemas c JOIN c.halls h WHERE LOWER(h.name) = LOWER(:hallName)")
    Page<Movie> findAllByHallIgnoreCase(@Param("hallName") String hallName, Pageable pageable);

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    Page<Movie> findByStatusAndReleaseDateGreaterThanEqual(MovieStatus status, LocalDate from, Pageable pageable);

    Page<Movie> findByStatusAndReleaseDateAfter(MovieStatus status, LocalDate date, Pageable pageable);


    Page<Movie> findAllByGenreContainingIgnoreCase(String genre, Pageable pageable);

    Set<Movie> findAllByIdIn(Set<Long> ids);


    Page<Movie> findAllByGenreIgnoreCaseContaining(String genre, Pageable pageable);

    boolean existsBySlugIgnoreCase(String candidate);

    boolean existsBySlugIgnoreCaseAndIdNot(String candidate, Long movieId);

    @Query("""
        select distinct m
        from Movie m
        where exists (
          select 1
          from Showtime s
          join s.hall h
          join h.cinema c
          where s.movie = m
            and c.id = :cinemaId
            and s.date = :date
        )
        order by m.title asc
    """)
    List<Movie> findByCinemaAndDate(@Param("cinemaId") Long cinemaId,
                                    @Param("date") LocalDate date);
}


