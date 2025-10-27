package com.cinetime.service.business;

import com.cinetime.entity.business.Country;
import com.cinetime.payload.mappers.CountryMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.request.business.CountryRequest;
import com.cinetime.payload.response.business.CountryMiniResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;


    public List<CountryMiniResponse> listCountries() {
        return countryRepository.findAll().stream()
                .map(countryMapper::countryToCountryMiniResponse)
                .collect(Collectors.toList());


    }

    public ResponseEntity<ResponseMessage> saveCountry(CountryRequest countryRequest) {
        // Check if country already exists (case-insensitive)
        if (countryRepository.findByNameIgnoreCase(countryRequest.getName()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ResponseMessage.builder()
                            .message("Country already exists")
                            .build());
        }

        // If not exists, create and save
        Country country = new Country();
        country.setName(countryRequest.getName());
        countryRepository.save(country);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseMessage.builder()
                        .message("Country added successfully")
                        .build());
    }

    public ResponseEntity<ResponseMessage> updateCountry(Long countryId, CountryRequest countryRequest) {
       Optional<Country> countryToUpdate = countryRepository.findById(countryId);

       if (countryToUpdate.isEmpty()){
           return ResponseEntity
                   .status(HttpStatus.NOT_FOUND)
                   .body(ResponseMessage.builder()
                           .message("Country you want to update is not exist")
                           .build());
       }

        // 4. Block rename to an existing country name
        Optional<Country> sameName = countryRepository.findByNameIgnoreCase(countryRequest.getName());

        if (sameName.isPresent() && !sameName.get().getId().equals(countryId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ResponseMessage.builder()
                            .message("Another country with this name already exists")
                            .build());
        }
        Country country = countryToUpdate.get();
        // 5. Update fields
        country.setName(countryRequest.getName());

        // 6. Save it
        countryRepository.save(country);

        // 7. Return success
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseMessage.builder()
                        .message("Country updated successfully")
                        .build());
    }

    public ResponseEntity<ResponseMessage> deleteCountry(Long countryId) {
        Optional<Country> countryToDelete = countryRepository.findById(countryId);

        if (countryToDelete.isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseMessage.builder()
                            .message("Country you want to delete is not exist")
                            .build());
        }

        Country country= countryToDelete.get();
        countryRepository.delete(country);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseMessage.builder()
                        .message("Country deleted successfully")
                        .build());

    }

    public ResponseEntity<CountryMiniResponse>getCountry(Long countryId) {
       Optional<Country> foundCountry = countryRepository.findById(countryId);

        return foundCountry.map(country -> ResponseEntity.ok()
                .body(countryMapper.countryToCountryMiniResponse(country))).orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build());


    }
}
