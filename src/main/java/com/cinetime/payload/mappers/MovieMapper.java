package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Movie;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.MovieResponse;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@Data
public class MovieMapper {

    // Movie entity → MovieResponse DTO
    public MovieResponse mapMovieToMovieResponse(Movie movie) {
        if (movie == null) return null;
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .summary(movie.getSummary())
                .releaseDate(movie.getReleaseDate())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .formats(movie.getFormats())
                .genre(movie.getGenre())
                .status(movie.getStatus())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }

    public Page<MovieResponse> mapToResponsePage(Page<Movie> movies) {
        return movies.map(this::mapMovieToMovieResponse);
    }

    // Movie entity → CinemaMovieResponse DTO
    public CinemaMovieResponse mapMovieToCinemaMovieResponse(Movie movie) {
        if (movie == null) return null;
        return new CinemaMovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getSlug(),
                movie.getSummary(),
                movie.getReleaseDate(),
                movie.getDuration(),
                movie.getRating(),
                movie.getDirector(),
                movie.getCast(),
                movie.getFormats(),
                movie.getGenre(),
                movie.getStatus()
        );
    }

    public Page<CinemaMovieResponse> mapToCinemaResponsePage(Page<Movie> movies) {
        return movies.map(this::mapMovieToCinemaMovieResponse);
    }


}
