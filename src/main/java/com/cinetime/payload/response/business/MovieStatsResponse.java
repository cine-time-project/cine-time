package com.cinetime.payload.response.business;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieStatsResponse {
    private Long id;
    private String title;
    private String releaseDate;
    private String posterUrl;
    private Long favCount;
}
