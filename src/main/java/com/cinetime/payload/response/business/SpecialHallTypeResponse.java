package com.cinetime.payload.response.business;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialHallTypeResponse {
    private Long id;
    private String name;
    private BigDecimal priceDiffPercent;
}
