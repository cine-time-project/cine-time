package com.cinetime.repository.business;

import com.cinetime.entity.business.Cinema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    // --- Search: sadece City filtresi ---
    @EntityGraph(attributePaths = {"cities"})
    @Query(
            value = """
            select distinct c
            from Cinema c
            left join c.cities ct
            where (:cityId is null or ct.id = :cityId)
            """,
            countQuery = """
            select count(distinct c)
            from Cinema c
            left join c.cities ct
            where (:cityId is null or ct.id = :cityId)
            """
    )
    Page<Cinema> search(@Param("cityId") Long cityId, Pageable pageable);

    // --- Search: City + Hall.isSpecial filtresi ---
    @EntityGraph(attributePaths = {"cities"})
    @Query(
            value = """
            select distinct c
            from Cinema c
            left join c.cities ct
            where (:cityId is null or ct.id = :cityId)
              and (
                    :specialHall is null
                    or exists (
                        select 1
                        from Hall h
                        where h.cinema = c and h.isSpecial = :specialHall
                    )
                  )
            """,
            countQuery = """
            select count(distinct c)
            from Cinema c
            left join c.cities ct
            where (:cityId is null or ct.id = :cityId)
              and (
                    :specialHall is null
                    or exists (
                        select 1
                        from Hall h
                        where h.cinema = c and h.isSpecial = :specialHall
                    )
                  )
            """
    )
    Page<Cinema> search(@Param("cityId") Long cityId,
                        @Param("specialHall") Boolean specialHall,
                        Pageable pageable);

    // --- Auth kullanıcının favori sinemaları ---
// Auth kullanıcının favori sinemaları – kök: Cinema
    @EntityGraph(attributePaths = {"cities"})
    @Query(
            value = """
        select distinct c
        from Cinema c
        join c.favorites f
        where f.user.id = :userId
        """,
            countQuery = """
        select count(distinct c)
        from Cinema c
        join c.favorites f
        where f.user.id = :userId
        """
    )
    Page<Cinema> findFavoriteCinemasByUserId(@Param("userId") Long userId, Pageable pageable);


    // --- Detay: City'lerle birlikte getir ---
    @EntityGraph(attributePaths = {"cities"})
    Optional<Cinema> findById(Long id);

    @Query("select u.id from User u where u.email = :email")
    Optional<Long> findIdByUsername(@Param("email") String email);


}
