package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.City;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.payload.response.business.CityMiniResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class CinemaMapper {

    public CinemaSummaryResponse toSummary(Cinema cinema){
        if (cinema == null) return null;

        var cities = cinema.getCities() == null
                ? new LinkedHashSet<CityMiniResponse>()
                : cinema.getCities().stream()
                .map(c -> CityMiniResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return CinemaSummaryResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .cities(cities)
                .build();
    }
}
