package com.cinetime.repository.business;

import com.cinetime.entity.business.Showtime;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository <Showtime,Long> {


    @Query("""
                select s
                  from Showtime s
                 where s.movie.id = :movieId
                   and (
                         s.date > :today
                      or (s.date = :today and s.startTime > :showTime)
                   )
                 order by s.date asc, s.startTime asc
            """)
    Optional<Showtime> findNextFutureShowtime(
            @Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("showTime") LocalTime showTime
    );


    //Ileriki tarihte baslayacak filmler icin ya ShowTimeEntity si icine startAt gibi field koyup asagidaki gibi bi yontem
    // ile sonuca varmak lazimmis, ya da en asagida ChatGpt den yardimla koydugum methodu kullanip page ile donus alabiliriz
//    Page<Showtime> findAllByMovie_IdAndStartsAtAfter(
//            Long movieId,
//            LocalDateTime after,
//            Pageable pageable
//    );

    // All *future* showtimes for a movie, paginated (date > today OR same-day with startTime > now)
    @Query("""
            select s
              from Showtime s
             where s.movie.id = :movieId
               and (
                     s.date > :today
                  or (s.date = :today and s.startTime > :showTime)
               )
             order by s.date asc, s.startTime asc
            """)
    Page<Showtime> findAllFutureShowtimesByMovieId(
            @Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("showTime") LocalTime showTime,
            Pageable pageable
    );

    default Page<Showtime> findAllFutureShowtimesByMovieId(Long movieId, Pageable pageable) {
        return findAllFutureShowtimesByMovieId(movieId, LocalDate.now(), LocalTime.now(), pageable);
    }

    default Optional<Showtime> findNextFutureShowtime(Long movieId) {
        return findNextFutureShowtime(movieId, LocalDate.now(), LocalTime.now());
    }

    Optional<Showtime> findByMovie_IdAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
            Long movieId,
            String hallName,
            String cinemaName,
            LocalDate date,
            LocalTime startTime
    );

    @Query("""
                select
                    h.id           as hallId,
                    h.name         as hallName,
                    h.seatCapacity as seatCapacity,
                    h.isSpecial    as isSpecial,
                    m.id           as movieId,
                    m.title        as movieTitle,
                    s.date         as date,
                    s.startTime    as startTime
                from Showtime s
                join s.hall  h
                join s.movie m
                where h.cinema.id = :cinemaId
                order by h.name asc, m.title asc, s.date asc, s.startTime asc
            """)
    List<HallMovieTimeRow> findShowtimesByCinemaId(@Param("cinemaId") Long cinemaId);

    Page<Showtime> findByDate(LocalDate date, Pageable pageable);

    Page<Showtime> findByDateAndStartTimeAfter(LocalDate date, LocalTime time, Pageable pageable);
}

