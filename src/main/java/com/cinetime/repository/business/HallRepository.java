package com.cinetime.repository.business;

import com.cinetime.entity.business.Hall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    @EntityGraph(attributePaths = "cinema")
    List<Hall> findByIsSpecialTrueOrderByNameAsc();

    boolean existsByCinemaIdAndName(Long cinemaId, String name);

    Page<Hall> findByNameContainingIgnoreCaseOrCinema_NameContainingIgnoreCase(String name, String cinemaName, Pageable pageable);


        @Modifying
        @Query("update Hall h set h.isSpecial = :flag where h.id = :id")
        int updateIsSpecialById(@Param("id") Long id, @Param("flag") boolean flag);

    @Query(value = """
      SELECT
         h.id                                                        AS hall_id,
         h.name                                                      AS hall_name,
         (CASE WHEN sh.id IS NOT NULL THEN TRUE ELSE FALSE END)      AS is_special,
         sht.name                                                    AS type_name,
         COALESCE(sht.price_diff_percent, 0)                         AS surcharge_percent,
         0::numeric                                                  AS surcharge_fixed
      FROM halls h
      LEFT JOIN special_halls sh
             ON sh.hall_id = h.id
      LEFT JOIN special_hall_types sht
             ON sht.id = sh.type_id
      WHERE h.cinema_id = :cinemaId
      ORDER BY h.id
      """, nativeQuery = true)
    List<Object[]> findHallPricingRaw(@Param("cinemaId") Long cinemaId);

}
