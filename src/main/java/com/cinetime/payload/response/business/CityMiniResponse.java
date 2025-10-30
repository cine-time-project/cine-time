package com.cinetime.payload.response.business;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityMiniResponse {
    private Long id;
    private String name;

  private CountryMiniResponse countryMiniResponse;

    public CityMiniResponse(Long id, String name) {
        this.id = id;
        this.name = name;
        this.countryMiniResponse = null;
    }
}
