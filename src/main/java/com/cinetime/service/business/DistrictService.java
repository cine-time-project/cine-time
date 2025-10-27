package com.cinetime.service.business;

import com.cinetime.entity.business.City;
import com.cinetime.entity.business.District;
import com.cinetime.payload.mappers.DistrictMapper;
import com.cinetime.payload.request.business.DistrictRequest;
import com.cinetime.payload.response.business.DistrictMiniResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.CityRepository;
import com.cinetime.repository.business.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<ResponseMessage> saveDistrict(DistrictRequest districtRequest) {
        // 1. Check if district with same name already exists (case-insensitive)
        Optional<District> existingDistrict =
                districtRepository.findByNameIgnoreCase(districtRequest.getName());

        if (existingDistrict.isPresent()) {
            return ResponseEntity
                    .status(400)
                    .body(ResponseMessage.builder()
                            .message("District with name '" + districtRequest.getName() + "' already exists")
                            .build());
        }

        // 2. Check if the referenced city exists (this is mandatory)
        Optional<City> mandatoryCity = cityRepository.findById(districtRequest.getCityId());
        if (mandatoryCity.isEmpty()) {
            return ResponseEntity
                    .status(400)
                    .body(ResponseMessage.builder()
                            .message("City not found for id " + districtRequest.getCityId())
                            .build());
        }

        // 3. Build and save the new District
        District newDistrict = new District();
        newDistrict.setName(districtRequest.getName());
        newDistrict.setCity(mandatoryCity.get());

        District savedDistrict = districtRepository.save(newDistrict);

        // 4. Return success message
        return ResponseEntity
                .status(201)
                .body(ResponseMessage.builder()
                        .message("District '" + savedDistrict.getName() + "' created successfully")
                        .build());
    }

    public ResponseEntity<ResponseMessage> deleteDistrict(Long districtId) {
        Optional<District> districtToCheck = districtRepository.findById(districtId);

        if (districtToCheck.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(ResponseMessage.builder()
                            .message("District does not exist")
                            .build());
        }

        District districtToDelete = districtToCheck.get();

        // This will respect cascade/orphanRemoval rules on District -> other entities
        districtRepository.delete(districtToDelete);

        return ResponseEntity
                .status(200)
                .body(ResponseMessage.builder()
                        .message(districtToDelete.getName() + " deleted successfully")
                        .build());
    }


}
