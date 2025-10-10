package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.CinemaImage;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.payload.response.business.CityMiniResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class CinemaMapper {

    public CinemaSummaryResponse toSummary(Cinema cinema) {
        if (cinema == null) return null;

        CityMiniResponse cityMiniResponse = null;
        if (cinema.getCity() != null) {
            cityMiniResponse = CityMiniResponse.builder()
                    .id(cinema.getCity().getId())
                    .name(cinema.getCity().getName())
                    .build();
        }

        // Build imageUrl: prefer external URL; otherwise expose your binary endpoint
        String imageUrl = null;
        if (cinema.getCinemaImage() != null) {
            String storedUrl = cinema.getCinemaImage().getUrl();
            if (storedUrl != null && !storedUrl.isBlank()) {
                imageUrl = storedUrl;
            } else {
                imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/cinemaimages/")
                        .path(String.valueOf(cinema.getId()))
                        .toUriString();
            }
        }

        return CinemaSummaryResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .city(cityMiniResponse)
                .imageUrl(imageUrl)
                .build();
    }
}