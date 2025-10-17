package com.cinetime.payload.response.business;

import lombok.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaSummaryResponse {
    private Long id;
    private String name;
    private CityMiniResponse city;
    private CountryMiniResponse country;
    private String imageUrl;

    public CinemaSummaryResponse(Long id, String name, CityMiniResponse city) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.imageUrl = null; // will be filled later in service/mapper if needed
    }

    // Constructor used by JPQL: new CinemaSummaryResponse(c.id, c.name, new CityMiniResponse(...), imageUrl)
    public CinemaSummaryResponse(Long id, String name, CityMiniResponse city, String imageUrl) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.imageUrl = imageUrl;
        // country intentionally left null when created via this ctor
    }
}
