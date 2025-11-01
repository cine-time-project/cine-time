package com.cinetime.payload.response.business;


import com.cinetime.entity.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private LocalDate releaseDate;
    private Integer duration;
    private Double rating;
    private String director;
    private List<String> cast;
    private List<String> formats;
    private List<String> genre;
    private MovieStatus status;
    private List<ImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trailerUrl;
    private String posterUrl;
    private Long posterId;
    private Long heroId;

}
