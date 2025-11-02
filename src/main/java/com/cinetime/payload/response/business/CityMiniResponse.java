package com.cinetime.payload.response.business;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityMiniResponse {
    private Long id;
    private String name;
 

  private CountryMiniResponse countryMiniResponse;
    private Set<DistrictMiniResponse> districtMiniResponses;


    public CityMiniResponse(Long id, String name) {
        this.id = id;
        this.name = name;
        this.countryMiniResponse = null;
        this.districtMiniResponses = new HashSet<>();
    }
}
