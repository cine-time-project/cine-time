package com.cinetime.controller.business;

import com.cinetime.payload.request.business.CountryRequest;
import com.cinetime.payload.response.business.CountryMiniResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.CountryService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries") // <-- tek base path
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;


    @PermitAll
    @GetMapping
    public ResponseEntity<List<CountryMiniResponse>> listCountries(){
        return ResponseEntity.ok(countryService.listCountries());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ResponseMessage> addCountry(
            @RequestBody @Valid CountryRequest countryRequest) {
        return  countryService.saveCountry(countryRequest);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<ResponseMessage> updateCountry(
        @RequestParam Long countryId, @RequestBody @Valid CountryRequest countryRequest){
        return countryService.updateCountry(countryId,countryRequest);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseMessage> deleteCountry(
            @RequestParam Long countryId){
        return countryService.deleteCountry(countryId);
    }


    @PermitAll
    @GetMapping("/getCountry")
    public ResponseEntity<CountryMiniResponse> getCountry(
            @RequestParam Long countryId)
    {
        return ResponseEntity.ok(countryService.getCountry(countryId));
    }







}
