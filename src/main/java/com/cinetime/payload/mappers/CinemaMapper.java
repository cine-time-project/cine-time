package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.CinemaImage;
import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Country;
import com.cinetime.payload.response.business.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CinemaMapper {

    private final CityMapper cityMapper;
    private final HallMapper hallMapper;
    private final MovieMapper movieMapper;
    private final CinemaImageMapper cinemaImageMapper;

    public CinemaSummaryResponse toSummary(Cinema cinema) {
        if (cinema == null) return null;

        CityMiniResponse cityMiniResponse = null;
        CountryMiniResponse countryMiniResponse = null;
        if (cinema.getCity() != null) {
            City city = cinema.getCity();

            cityMiniResponse = CityMiniResponse.builder()
                    .id(city.getId())
                    .name(city.getName())
                    .build();
            if (city.getCountry() != null) {
                Country country = city.getCountry();
                countryMiniResponse = CountryMiniResponse.builder()
                        .id(country.getId())
                        .name(country.getName())
                        .build();
            }
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
                .country(countryMiniResponse)
                .imageUrl(imageUrl)
                .build();
    }

    public CinemaDetailedResponse toDetailedResponse(Cinema cinema) {
        // -----------------------------
        // imageUrl: varsa CinemaImage.url, yoksa default
        // -----------------------------
        String imageUrl = null;
        CinemaImage cinemaImage = cinema.getCinemaImage();
        if (cinemaImage != null) {
            if (cinemaImage.getUrl() != null && !cinemaImage.getUrl().isBlank()) {
                imageUrl = cinemaImage.getUrl();
            } else {
                imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/cinemaimages/")
                        .path(String.valueOf(cinema.getId()))
                        .toUriString();
            }
        }

        // -----------------------------
        // city mapper
        // -----------------------------
        CityMiniResponse cityMiniResponse = cityMapper.cityToCityMiniResponse(cinema.getCity());

        // -----------------------------
        // halls & showtimes
        // -----------------------------
        Set<HallResponse> hallResponses = (cinema.getHalls() != null && !cinema.getHalls().isEmpty())
                ? cinema.getHalls().stream()
                .map(hallMapper::mapHallToResponse)
                .collect(Collectors.toSet())
                : null;

        // -----------------------------
        // movies
        // -----------------------------
        Set<MovieMiniResponse> movieMiniResponses = (cinema.getMovies() != null && !cinema.getMovies().isEmpty())
                ? cinema.getMovies().stream()
                .map(movieMapper::toMiniResponse)
                .collect(Collectors.toSet())
                : null;

        // -----------------------------
        // Build response
        // -----------------------------
        return CinemaDetailedResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .slug(cinema.getSlug())
                .imageUrl(imageUrl)
                // frontend URL kullanacak
                .cinemaImageUrl(
                        ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/cinemaimages/")
                                .path(String.valueOf(cinema.getId()))
                                .toUriString()
                )
                .createdAt(cinema.getCreatedAt())
                .updatedAt(cinema.getUpdatedAt())
                .city(cityMiniResponse)
                .halls(hallResponses)
                .movies(movieMiniResponses)
                .build();
    }


}