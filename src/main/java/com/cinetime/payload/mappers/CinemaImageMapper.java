package com.cinetime.payload.mappers;

import com.cinetime.entity.business.CinemaImage;
import com.cinetime.payload.response.business.CinemaImageResponse;
import com.cinetime.payload.response.business.ImageResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class CinemaImageMapper{

    public CinemaImageResponse cinemaImageToResponse(CinemaImage cinemaImage){
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/cinemaimages/") // was /api/images/
                .path(String.valueOf(cinemaImage.getId()))
                .toUriString();

        return CinemaImageResponse.builder()
                .id(cinemaImage.getId())
                .name(cinemaImage.getName())
                .type(cinemaImage.getType())
                .cinemaId(cinemaImage.getCinema() == null ? null : cinemaImage.getCinema().getId())
                .createdAt(cinemaImage.getCreatedAt())
                .updatedAt(cinemaImage.getUpdatedAt())
                .url(url)
                .build();
    }

    }




