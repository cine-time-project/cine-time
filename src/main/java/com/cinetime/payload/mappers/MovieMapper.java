package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Movie;
import com.cinetime.payload.response.business.MovieResponse;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@Data
public class MovieMapper {

    public MovieResponse mapMovieToMovieResponse(Movie movie){
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
                .poster(movie.getPoster())
                .build();
    }

    public Page<MovieResponse> mapToResponsePage(Page<Movie> movies){
        return movies.map(this::mapMovieToMovieResponse);
    }


}
