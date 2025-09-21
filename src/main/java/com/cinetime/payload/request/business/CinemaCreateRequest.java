package com.cinetime.payload.request.business;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaCreateRequest {

    @NotBlank
    private String name;

    private String slug; //optional

    private Set<Long> cityIds; //optional
}
