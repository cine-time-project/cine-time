package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Country;
import com.cinetime.payload.response.business.CountryMiniResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class CountryMapper {
    public CountryMiniResponse countryToCountryMiniResponse(Country country){
        if (country==null) return null;
        return CountryMiniResponse.builder()
                .name(country.getName())
                .id(country.getId())
                .build();
    }


}
