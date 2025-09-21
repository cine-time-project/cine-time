package com.cinetime.payload.request.business;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallRequest {

    @NotBlank(message = "Hall name cannot be empty")
    private String name;

    @NotNull(message = "Seat capacity cannot be null")
    @Min(value = 1, message = "Seat capacity must be at least 1")
    private Integer seatCapacity;

    @Builder.Default
    private Boolean isSpecial = false;

    @NotNull(message = "Cinema ID cannot be null")
    private Long cinemaId;
}
