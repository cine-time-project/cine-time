package com.cinetime.payload.request.business;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialHallTypeRequest {
    @NotBlank @Size(max=100)
    private String name;

    @NotNull
    @DecimalMin(value="0.00") @DecimalMax(value="100.00")
    private BigDecimal priceDiffPercent;
}
