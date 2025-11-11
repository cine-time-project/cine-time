package com.cinetime.payload.response.business;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialHallResponse {
    private Long id;

    private Long hallId;
    private String hallName;
    private Integer seatCapacity;

    private Long cinemaId;
    private String cinemaName;

    private Long typeId;
    private String typeName;
    private BigDecimal priceDiffPercent;
}