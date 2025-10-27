package com.cinetime.service.business;

import com.cinetime.payload.mappers.DistrictMapper;
import com.cinetime.payload.response.business.DistrictMiniResponse;
import com.cinetime.repository.business.CityRepository;
import com.cinetime.repository.business.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictService {
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;

    public  List<DistrictMiniResponse> listDistricts() {
        return
                districtRepository.findAll().stream()
                        .map(districtMapper::districtToDistrictMiniResponse).collect(Collectors.toList());


    }

    public ResponseEntity <DistrictMiniResponse> findDistrictById(Long districtId) {
        return districtRepository.findById(districtId)
                .map(district ->ResponseEntity.ok(districtMapper.districtToDistrictMiniResponse(district)))
                .orElseGet(() ->ResponseEntity.status(404).build());

    }

}
