package com.cinetime.repository.business;

import com.cinetime.entity.business.SpecialHall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpecialHallRepository extends JpaRepository<SpecialHall, Long> {
    Optional<SpecialHall> findByHallId(Long hallId);
    boolean existsByHallId(Long hallId);
    void deleteByHallId(Long hallId);
}