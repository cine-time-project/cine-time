package com.cinetime.repository.business;

import com.cinetime.entity.business.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistrictRepository  extends JpaRepository<District,Long> {
    Optional<District> findByNameIgnoreCase(String name);
}
