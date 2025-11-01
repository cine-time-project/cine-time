package com.cinetime.payload.mappers;

import com.cinetime.entity.business.City;
import com.cinetime.payload.request.business.CityRequest;
import com.cinetime.payload.response.business.CityMiniResponse;
import com.cinetime.payload.response.business.CountryMiniResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class CityMapper {

    public CityMiniResponse cityToCityMiniResponse(City city){
        if (city==null) return null;

        return CityMiniResponse.builder()
                .name(city.getName())
                .id(city.getId())
                .countryMiniResponse(
                               CountryMiniResponse.builder()
                                       .id(city.getCountry().getId())
                                       .name(city.getCountry().getName())
                                       .build()
                )
                .build();
    }


}
