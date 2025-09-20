package com.cinetime.payload.response.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialHallResponse {
    private Long id;
    private String name;
    private Integer seatCapacity;
    private Long cinemaId;
    private String cinemaName;
}
