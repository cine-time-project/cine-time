package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Cinema;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.payload.response.business.CityMiniResponse;
import org.springframework.stereotype.Component;

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
        return CinemaSummaryResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .city(cityMiniResponse)
                .build();

    }
}
