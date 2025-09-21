package com.cinetime.repository.business;

import com.cinetime.entity.business.Hall;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    @EntityGraph(attributePaths = "cinema")
    List<Hall> findByIsSpecialTrueOrderByNameAsc();
}
