package com.cinetime.repository.business;

import com.cinetime.entity.business.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository <Showtime,Long> {



    @Query("""
        select s
          from Showtime s
         where s.movie.id = :movieId
           and (
                 s.date > :today
              or (s.date = :today and s.startTime > :nowTime)
           )
         order by s.date asc, s.startTime asc
    """)
    Optional<Showtime> findNextFutureShowtime(
            @Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime
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
          or (s.date = :today and s.startTime > :nowTime)
       )
     order by s.date asc, s.startTime asc
""")
    Page<Showtime> findAllFutureShowtimesByMovieId(
            @Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime,
            Pageable pageable
    );





}
