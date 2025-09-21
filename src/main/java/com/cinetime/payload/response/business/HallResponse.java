package com.cinetime.payload.response.business;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HallResponse {

    private Long id;
    private String name;
    private Integer seatCapacity;
    private Boolean isSpecial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optionally include cinema info
    private Long cinemaId;
    private String cinemaName;
}
