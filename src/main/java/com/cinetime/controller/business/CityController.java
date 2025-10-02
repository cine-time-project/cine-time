package com.cinetime.controller.business;

import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.service.business.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities") // <-- tek base path
@RequiredArgsConstructor
public class CityController {
    private final CityService cityService;

    @PreAuthorize("permitAll()")
    @GetMapping()
    public ResponseEntity<List<CityMiniResponse>> listCities(){
        return ResponseEntity.ok(cityService.listCities());
    }


}
