package com.cinetime.controller.business;

import com.cinetime.payload.request.business.CityRequest;
import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.CityService;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities") // <-- tek base path
@RequiredArgsConstructor
public class CityController {
    private final CityService cityService;

    @PermitAll
    @GetMapping()
    public ResponseEntity<List<CityMiniResponse>> listCitiesWithCinemas(){
        return ResponseEntity.ok(cityService.listCitiesWithCinemas());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseMessage> addCity(
            @RequestBody @Valid CityRequest cityRequest
            ){
        return cityService.saveCity(cityRequest);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{cityId}")
    public ResponseEntity<ResponseMessage> deleteCity(
            @PathVariable Long cityId
    ){
        return cityService.deleteCity(cityId);
    }


    @PermitAll
    @GetMapping("/{cityId}")
    public ResponseEntity <CityMiniResponse> getCity(
            @PathVariable Long cityId
    ){
        return  cityService.findCityById(cityId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{cityId}")
    public ResponseEntity<ResponseMessage> updateCity(
            @PathVariable Long cityId, @RequestBody @Valid CityRequest cityRequest
    ){
        return cityService.updateCity(cityId,cityRequest);
    }

    @PermitAll
    @GetMapping("/listAllCities")
    public ResponseEntity<List<CityMiniResponse>> listAllCities(){
        return ResponseEntity.ok(cityService.listAllCities());
    }

    @PermitAll
    @GetMapping("/listCityWithItsDistrict/{cityId}")
    public ResponseEntity<CityMiniResponse> listAllCitiesWithDistricts(
            @PathVariable Long cityId
    ){
        return cityService.listCityWithDistrict(cityId);
    }



}
