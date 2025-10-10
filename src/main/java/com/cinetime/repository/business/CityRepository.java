package com.cinetime.repository.business;

import com.cinetime.entity.business.City;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByNameIgnoreCase(String name);

    // Only cities that are connected to at least one cinema
    @Query("""
        select distinct c
        from City c
        join c.cinemas cn
        order by c.name asc
    """)
    List<City> findCitiesWithCinemas();




}
