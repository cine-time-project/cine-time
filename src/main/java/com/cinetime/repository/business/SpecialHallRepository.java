package com.cinetime.repository.business;

import com.cinetime.entity.business.SpecialHall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialHallRepository extends JpaRepository<SpecialHall, Long> {
    boolean existsByHall_Id(Long hallId);
    Optional<SpecialHall> findByHall_Id(Long hallId);
}