package com.cinetime.payload.response.business;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CinemaSummaryResponse {

    private Long id;
    private String name;

    private CityMiniResponse city;
    private CountryMiniResponse country;

    // null olabilir (örneğin JPQL select'te imageUrl döndürmeden)
    private String imageUrl;
    private String cinemaImageUrl;

    /**
     * JPQL için özel constructor:
     * Country dahil şekilde city + country + basic info
     */
    public CinemaSummaryResponse(
            Long id,
            String name,
            CityMiniResponse city,
            CountryMiniResponse country,
            String imageUrl
    ) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.country = country;
        this.imageUrl = imageUrl;

        // JPQL bu alanı vermez → null
        this.cinemaImageUrl = null;
    }
}
