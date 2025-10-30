package com.cinetime.service.business;

import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Country;
import com.cinetime.payload.mappers.CityMapper;
import com.cinetime.payload.mappers.DistrictMapper;
import com.cinetime.payload.request.business.CityRequest;
import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.payload.response.business.CountryMiniResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.CityRepository;
import com.cinetime.repository.business.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;
    private final CountryRepository countryRepository;
    private final DistrictMapper districtMapper;
    @Transactional(readOnly = true)
    public List<CityMiniResponse> listCitiesWithCinemas() {
        return
                cityRepository.findCitiesWithCinemas().stream()
                        .map(cityMapper::cityToCityMiniResponse)
                        .collect(Collectors.toList());

    }


    public ResponseEntity<ResponseMessage> saveCity(CityRequest cityRequest) {
    Optional<City> existingCity =
            cityRepository.findByNameIgnoreCase(cityRequest.getName());
    Optional<Country> mandatoryCountry =
            countryRepository.findById(cityRequest.getCountryId());

    if (existingCity.isPresent()){
        return ResponseEntity
                .status(409)
                .body(ResponseMessage.builder()
                        .message("City already exists")
                        .build());
    }

    if (mandatoryCountry.isEmpty()){
        return ResponseEntity
                .badRequest()
                .body(ResponseMessage.builder()
                        .message("Country not found for id " + cityRequest.getCountryId())
                        .build());
    }
        City newCity = new City();
        newCity.setName(cityRequest.getName());
        newCity.setCountry(mandatoryCountry.get());

        cityRepository.save(newCity);

    return ResponseEntity
            .status(201)
            .body(ResponseMessage.builder()
                    .message("City saved successfully with id: "+ newCity.getId())
                    .build());
    }

    public ResponseEntity<ResponseMessage> deleteCity(Long cityId) {
        Optional<City> cityToCheck = cityRepository.findById(cityId);
        if (cityToCheck.isEmpty()){
            return ResponseEntity
                    .status(404)
                    .body(ResponseMessage.builder()
                            .message("City does not exist")
                            .build());
        }
        City cityToDelete = cityToCheck.get();
        cityRepository.delete(cityToDelete);
        return ResponseEntity
                .status(200)
                .body(ResponseMessage.builder()
                        .message(cityToDelete.getName()+" deleted successfully with id:" +cityToDelete.getId()+ "city name "+ cityToDelete.getName())
                        .build());

    }
    @Transactional(readOnly = true)
    public ResponseEntity <CityMiniResponse> findCityById(Long cityId) {
        return cityRepository.findById(cityId)
                .map(city ->ResponseEntity.ok(cityMapper.cityToCityMiniResponse(city)))
                .orElseGet(() ->ResponseEntity.status(404).build());

    }
    @Transactional
    public ResponseEntity<ResponseMessage> updateCity(Long cityId, CityRequest cityRequest) {

        // 1. Load the city we're updating
        Optional<City> existingCityOpt = cityRepository.findById(cityId);
        if (existingCityOpt.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(ResponseMessage.builder()
                            .message("City you want to update does not exist")
                            .build());
        }
        City existingCity = existingCityOpt.get();

        // 2. Validate the country
        Optional<Country> mandatoryCountryOpt =
                countryRepository.findById(cityRequest.getCountryId());
        if (mandatoryCountryOpt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ResponseMessage.builder()
                            .message("Country not found for id " + cityRequest.getCountryId())
                            .build());
        }
        Country mandatoryCountry = mandatoryCountryOpt.get();

        // 3. Check for duplicate city name (ignore case), but allow keeping same name
        Optional<City> cityWithSameNameOpt =
                cityRepository.findByNameIgnoreCase(cityRequest.getName());
        if (cityWithSameNameOpt.isPresent()
                && !cityWithSameNameOpt.get().getId().equals(existingCity.getId())) {
            return ResponseEntity
                    .status(409)
                    .body(ResponseMessage.builder()
                            .message("Another city with this name already exists")
                            .build());
        }

        // 4. Update the managed entity instead of deleting/recreating
        existingCity.setName(cityRequest.getName());
        existingCity.setCountry(mandatoryCountry);

        cityRepository.save(existingCity);

        return ResponseEntity
                .status(200)
                .body(ResponseMessage.builder()
                        .message("City updated successfully with cityId "+ existingCity.getId()+" countryId: " + existingCity.getCountry().getId())
                        .build());
    }
    @Transactional(readOnly = true)
    public List<CityMiniResponse> listAllCities() {
        return cityRepository.findAll().stream()
                .map(cityMapper::cityToCityMiniResponse).collect(Collectors.toList());


    }

    @Transactional(readOnly = true)
    public ResponseEntity<CityMiniResponse> listCityWithDistrict(Long cityId) {
        City foundCity = cityRepository.findByIdWithDistricts(cityId).orElseThrow(()->new RuntimeException("City not exits"));

        CityMiniResponse cityMiniResponse= new CityMiniResponse();
        cityMiniResponse.setId(foundCity.getId());
        cityMiniResponse.setCountryMiniResponse(new CountryMiniResponse(foundCity.getCountry().getId(),foundCity.getCountry().getName()));
        cityMiniResponse.setName(foundCity.getName());
        cityMiniResponse.setDistrictMiniResponses(foundCity.getDistricts().stream()
                .map(districtMapper::districtToDistrictMiniResponse).collect(Collectors.toSet()));

        return ResponseEntity.ok(cityMiniResponse);
    }
}
