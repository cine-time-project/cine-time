package com.cinetime.controller.business;

import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Country;
import com.cinetime.entity.business.District;
import com.cinetime.payload.mappers.DistrictMapper;
import com.cinetime.payload.request.business.CityRequest;
import com.cinetime.payload.request.business.DistrictRequest;
import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.payload.response.business.DistrictMiniResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.CityRepository;
import com.cinetime.repository.business.DistrictRepository;
import com.cinetime.service.business.DistrictService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/districts") // <-- tek base path
@RequiredArgsConstructor
public class DistrictController {
    private final DistrictService districtService;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;


    @PermitAll
    @GetMapping()
    public ResponseEntity<List<DistrictMiniResponse>> listDistricts (){
        return ResponseEntity.ok(districtService.listDistricts());
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseMessage> saveDistrict(@RequestBody DistrictRequest districtRequest) {
        Optional<District> existingDistrict =
                districtRepository.findByNameIgnoreCase(districtRequest.getName());

        Optional<City> mandatoryCity =
                cityRepository.findById(districtRequest.getCityId());

        if (existingDistrict.isPresent()){
            return ResponseEntity
                    .status(409)
                    .body(ResponseMessage.builder()
                            .message("District already exists")
                            .build());
        }

        if (mandatoryCity.isEmpty()){
            return ResponseEntity
                    .badRequest()
                    .body(ResponseMessage.builder()
                            .message("District not found for id " + districtRequest.getCityId())
                            .build());
        }
        District newDistrict = new District();
        newDistrict.setName(districtRequest.getName());
        newDistrict.setCity(mandatoryCity.get());

        districtRepository.save(newDistrict);

        return ResponseEntity
                .status(201)
                .body(ResponseMessage.builder()
                        .message("District saved successfully, districtId: "+newDistrict.getId()+" districtName: "+ newDistrict.getName())
                        .build());
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("{districtId}")
    public ResponseEntity<ResponseMessage> deleteDistrict(@PathVariable Long districtId) {
        Optional<District> districtToCheck = districtRepository.findById(districtId);
        if (districtToCheck.isEmpty()){
            return ResponseEntity
                    .status(404)
                    .body(ResponseMessage.builder()
                            .message("District does not exist")
                            .build());
        }
        District districtToDelete = districtToCheck.get();
        districtRepository.delete(districtToDelete);
        return ResponseEntity
                .status(200)
                .body(ResponseMessage.builder()
                        .message(districtToDelete.getName()+" deleted successfully")
                        .build());

    }


    @PermitAll
    @GetMapping("/{districtId}")
    public ResponseEntity <DistrictMiniResponse> getDistrict(
            @PathVariable Long districtId
    ){
        return  districtService.findDistrictById(districtId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{districtId}")
    public ResponseEntity<ResponseMessage> updateDistrict(
            @PathVariable Long districtId, @RequestBody @Valid DistrictRequest districtRequest
    ){
        return districtService.updateDistrict(districtId,districtRequest);
    }


}







