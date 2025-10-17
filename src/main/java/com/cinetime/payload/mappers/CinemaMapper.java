package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.CinemaImage;
import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Country;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.payload.response.business.CountryMiniResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class CinemaMapper {

    public CinemaSummaryResponse toSummary(Cinema cinema) {
        if (cinema == null) return null;

        CityMiniResponse cityMiniResponse = null;
        CountryMiniResponse countryMiniResponse= null;
        if (cinema.getCity() != null) {
            City city= cinema.getCity();

            cityMiniResponse = CityMiniResponse.builder()
                    .id(city.getId())
                    .name(city.getName())
                    .build();
            if (city.getCountry() !=null){
                Country country= city.getCountry();
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
}