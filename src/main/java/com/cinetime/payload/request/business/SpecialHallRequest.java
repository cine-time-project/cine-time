package com.cinetime.payload.request.business;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpecialHallRequest {
    @NotNull private Long hallId;
    @NotNull private Long typeId;
}