package com.cinetime.repository.business;

import com.cinetime.entity.business.SpecialHallType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialHallTypeRepository extends JpaRepository<SpecialHallType, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<SpecialHallType> findByNameIgnoreCase(String name);
}