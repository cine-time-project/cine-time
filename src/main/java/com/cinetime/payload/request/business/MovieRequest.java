package com.cinetime.payload.request.business;

import com.cinetime.entity.enums.MovieStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
// eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBjaW5ldGltZS5sb2NhbCIsImlhdCI6MTc1ODQyNDM3MCwiZXhwIjoxNzU4NTEwNzcwfQ.X217O-eDdOxg-QezMUdwX3Nc0iTXbzIqFRmi9h5k2TyWC7rvlTvc2o0BeWggikT46JpV-NO_AJqunwU-28-NTA

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MovieRequest {

    @NotNull(message = "Movie title cannot be empty")
    @Size(min = 3, max = 100)
    private String title;

    @Size(min = 5, max = 50)
    private String slug;

    @NotNull(message = "Summary cannot be empty")
    @Size(min = 3, max = 300)
    private String summary;

    @NotNull(message = "Release date cannot be empty")
    private LocalDate releaseDate;

    @NotNull(message = "Duration cannot be empty")
    private Integer duration;

    private Double rating;

    private String specialHalls;

    @Size(min = 5, max = 20)
    private String director;

    @NotNull(message = "Cast cannot be empty")
    @Size(min = 1)
    private List<String> cast;

    @NotNull(message = "Formats cannot be empty")
    @Size(min = 1)
    private List<String> formats;

    @NotNull(message = "Genre cannot be empty")
    @Size(min = 1)
    private List<String> genre;

    @NotNull(message = "Movie Status cannot be empty")
    private MovieStatus status;

    private Set<Long> cinemaIds;

    private Set<Long> imageIds;

}

