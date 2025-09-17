package com.cinetime.payload.response.business;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityMiniResponse {
    private Long id;
    private String name;
}
