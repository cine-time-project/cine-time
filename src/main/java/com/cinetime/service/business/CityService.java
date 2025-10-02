package com.cinetime.service.business;

import com.cinetime.payload.mappers.CityMapper;
import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.repository.business.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public List<CityMiniResponse> listCities() {
        return
                cityRepository.findCitiesWithCinemas().stream()
                        .map(cityMapper::cityToCityMiniResponse)
                        .collect(Collectors.toList());

    }
}
