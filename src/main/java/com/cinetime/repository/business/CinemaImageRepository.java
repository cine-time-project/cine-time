package com.cinetime.repository.business;

import com.cinetime.entity.business.CinemaImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CinemaImageRepository extends JpaRepository<CinemaImage,Long> {


    Optional<CinemaImage> findByCinema_Id(Long cinemaId);
}
