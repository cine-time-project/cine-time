package com.cinetime.payload.response.business;

import lombok.*;

import java.math.BigDecimal;

@Data @Builder
public class HallPricingResponse {
    private Long hallId;
    private String hallName;
    private boolean special;
    private String typeName;
    private BigDecimal surchargePercent; // from special_hall_types.price_diff_percent
    private BigDecimal surchargeFixed;
}