package com.cinetime.repository.business;

import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;


import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.payload.response.business.CountryMiniResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    Optional<Showtime> findByMovie_TitleIgnoreCaseAndHall_NameIgnoreCaseAndHall_Cinema_NameIgnoreCaseAndDateAndStartTime(
            String movieTitle, String hallName, String cinemaName, LocalDate date, LocalTime startTime);

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

    interface HallMovieTimeRow {
        Long getHallId();

        String getHallName();

        Integer getSeatCapacity();

        Boolean getIsSpecial();

        Long getMovieId();

        String getMovieTitle();

        java.time.LocalDate getDate();

        java.time.LocalTime getStartTime();
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Showtime s where s.hall.cinema.id = :cinemaId")
    void deleteByCinemaId(@Param("cinemaId") Long cinemaId);



    @EntityGraph(attributePaths = {
            "hall",
            "hall.cinema",
            "hall.cinema.city",
            "hall.cinema.city.country"
    })
    Page<Showtime> findAllByMovie(Movie movie, Pageable pageable);

    @Query("""
        select distinct new com.cinetime.payload.response.business.CityMiniResponse(ct.id, ct.name)
        from Showtime s
          join s.hall h
          join h.cinema cn
          join cn.city ct
        where (:movieId is null or s.movie.id = :movieId)
          and (:onOrAfter is null or s.date >= :onOrAfter)
    """)
    List<CityMiniResponse> findCitiesWithShowtimes(
            @Param("onOrAfter") LocalDate onOrAfter,
            @Param("movieId") Long movieId
    );

    @Query("""
    select distinct new com.cinetime.payload.response.business.CountryMiniResponse(co.id, co.name)
    from Showtime s
      join s.hall h
      join h.cinema cn
      join cn.city ct
      join ct.country co
    where (:movieId   is null or s.movie.id = :movieId)
      and (:onOrAfter is null or s.date     >= :onOrAfter)
    order by co.name asc
""")
    List<CountryMiniResponse> findCountriesWithShowtimes(
            @Param("onOrAfter") LocalDate onOrAfter,
            @Param("movieId")   Long movieId
    );


    @Query("""
  select s
  from Showtime s
  join s.hall h
  where h.cinema.id = :cinemaId
    and s.movie.id  = :movieId
    and s.date      = :date
  order by s.startTime
""")
    List<Showtime> findByCinemaMovieAndDate(Long cinemaId, Long movieId, LocalDate date);


    @Query("""
    SELECT DISTINCT city
    FROM City city
      JOIN city.country ctry
      JOIN city.cinemas cin
      JOIN cin.halls hall
      JOIN hall.showtimes st
    WHERE (:countryId IS NULL OR ctry.id = :countryId)
      AND (:movieId IS NULL OR st.movie.id = :movieId)
      AND (:onOrAfterStart IS NULL OR st.startTime >= :onOrAfterStart)
""")
    List<City> findCitiesWithShowtimesFiltered(@Param("onOrAfterStart") LocalDateTime onOrAfterStart,
                                               @Param("movieId") Long movieId,
                                               @Param("countryId") Long countryId);


    @Query("""
    SELECT DISTINCT new com.cinetime.payload.response.business.CityMiniResponse(c.id, c.name)
    FROM City c
      JOIN c.country co
      JOIN c.cinemas ci
      JOIN ci.halls h
      JOIN h.showtimes st
    WHERE (:countryId IS NULL OR co.id = :countryId)
      AND (:movieId IS NULL OR st.movie.id = :movieId)
      AND (:onOrAfter IS NULL OR st.startTime >= :onOrAfter)
""")
    List<CityMiniResponse> findCitiesWithShowtimes(
            @Param("onOrAfter") LocalDate onOrAfter,
            @Param("movieId") Long movieId,
            @Param("countryId") Long countryId
    );


    @Query("""
    SELECT DISTINCT new com.cinetime.payload.response.business.CityMiniResponse(c.id, c.name)
    FROM City c
      JOIN c.country co
      JOIN c.cinemas ci
      JOIN ci.halls h
      JOIN h.showtimes st
    WHERE (:countryId IS NULL OR co.id = :countryId)
      AND (:movieId   IS NULL OR st.movie.id = :movieId)
      AND (:onOrAfter IS NULL OR st.startTime >= :onOrAfter)
""")
    List<CityMiniResponse> findCitiesWithShowtimesByCountry(
            @Param("onOrAfter") LocalDate onOrAfter,
            @Param("movieId")   Long movieId,
            @Param("countryId") Long countryId
    );

// import com.cinetime.entity.business.City;

    @Query("""
    SELECT DISTINCT c
    FROM City c
      JOIN c.country co
      JOIN c.cinemas ci
      JOIN ci.halls h
      JOIN h.showtimes st
    WHERE (:countryId IS NULL OR co.id = :countryId)
      AND (:movieId   IS NULL OR st.movie.id = :movieId)
      AND (:onOrAfter IS NULL OR st.date >= :onOrAfter)
""")
    List<com.cinetime.entity.business.City> findCitiesWithShowtimesByCountryTic(
            @Param("onOrAfter") LocalDate onOrAfter,
            @Param("movieId")   Long movieId,
            @Param("countryId") Long countryId
    );

    @Query("""
        SELECT s
          FROM Showtime s
          JOIN s.hall h
          JOIN h.cinema c
         WHERE (:cinemaId IS NULL OR c.id = :cinemaId)
           AND (:hallId   IS NULL OR h.id = :hallId)
           AND (:movieId  IS NULL OR s.movie.id = :movieId)
           AND (:dateFrom IS NULL OR s.date >= :dateFrom)
           AND (:dateTo   IS NULL OR s.date <= :dateTo)
        """)
    Page<Showtime> findAllFiltered(
            @Param("cinemaId") Long cinemaId,
            @Param("hallId")   Long hallId,
            @Param("movieId")  Long movieId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo")   LocalDate dateTo,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"hall", "hall.cinema", "movie"})
    Optional<Showtime> findById(Long id);

    @EntityGraph(attributePaths = {"hall", "movie"})
    Page<Showtime> findAll(Pageable pageable);



    // minimal  EntityGraph
    @EntityGraph(attributePaths = {"hall", "hall.cinema", "movie"})
    Optional<Showtime> findWithRefsById(Long id);

    // Alternatif: HQL fetch-join
    @Query("""
           select s from Showtime s
             left join fetch s.hall h
             left join fetch h.cinema c
             left join fetch s.movie m
           where s.id = :id
           """)
    Optional<Showtime> findByIdFetchAll(@Param("id") Long id);

}





