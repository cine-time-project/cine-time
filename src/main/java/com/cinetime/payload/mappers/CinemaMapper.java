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

        // -----------------------------
        // city & country
        // -----------------------------
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

        // -----------------------------
        // imageUrl & cinemaImageUrl
        // -----------------------------
        String imageUrl = null;
        String cinemaImageUrl = null;

        CinemaImage cinemaImage = cinema.getCinemaImage();
        if (cinemaImage != null) {
            if (cinemaImage.getData() != null && cinemaImage.getData().length > 0) {
                // DB’de binary data varsa endpoint öncelikli
                cinemaImageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/cinemaimages/")
                        .path(String.valueOf(cinema.getId()))
                        .toUriString();
            }

            // Dış URL varsa fallback
            if (cinemaImage.getUrl() != null && !cinemaImage.getUrl().isBlank()) {
                imageUrl = cinemaImage.getUrl();
            }

            // Eğer binary data varsa, imageUrl’yi de endpoint olarak atayabiliriz
            if (cinemaImageUrl != null && imageUrl == null) {
                imageUrl = cinemaImageUrl;
            }
        }

        // -----------------------------
        // build response
        // -----------------------------
        return CinemaSummaryResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .city(cityMiniResponse)
                .country(countryMiniResponse)
                .imageUrl(imageUrl)
                .cinemaImageUrl(cinemaImageUrl)
                .build();
    }




    public CinemaDetailedResponse toDetailedResponse(Cinema cinema) {
        if (cinema == null) return null;

        // -----------------------------
        // imageUrl & cinemaImageUrl
        // -----------------------------
        String imageUrl = null;
        String cinemaImageUrl = null;

        CinemaImage cinemaImage = cinema.getCinemaImage();
        if (cinemaImage != null) {
            if (cinemaImage.getData() != null && cinemaImage.getData().length > 0) {
                // DB’de binary data varsa endpoint üzerinden fetch
                cinemaImageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/cinemaimages/")
                        .path(String.valueOf(cinema.getId()))
                        .toUriString();
            }

            // DB’de URL varsa fallback
            if (cinemaImage.getUrl() != null && !cinemaImage.getUrl().isBlank()) {
                imageUrl = cinemaImage.getUrl();
            }

            // Eğer binary data varsa ve imageUrl null ise endpoint kullan
            if (cinemaImageUrl != null && imageUrl == null) {
                imageUrl = cinemaImageUrl;
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
                .cinemaImageUrl(cinemaImageUrl)
                .createdAt(cinema.getCreatedAt())
                .updatedAt(cinema.getUpdatedAt())
                .city(cityMiniResponse)
                .halls(hallResponses)
                .movies(movieMiniResponses)
                .build();
    }



}