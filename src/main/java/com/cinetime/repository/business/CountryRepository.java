package com.cinetime.repository.business;

import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country,Long> {

    Optional<Country> findByNameIgnoreCase(String name);

    boolean existById(Long countryId);




}
