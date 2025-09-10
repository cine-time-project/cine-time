package com.cinetime.payload.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class FavoriteCreateRequest {

    private Long movieId;
    private Long cinemaId;


    @AssertTrue(message = "Provide exactly one of movieId or cinemaId")
    public boolean xor() {
        return (movieId != null) ^ (cinemaId != null);

    }

}
