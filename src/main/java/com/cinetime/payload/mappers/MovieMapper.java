package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Movie;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.ImageResponse;
import com.cinetime.payload.response.business.MovieResponse;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Data
public class MovieMapper {

    private final ImageMapper imageMapper;

    public MovieResponse mapMovieToMovieResponse(Movie movie) {
        if (movie == null) return null;

        List<ImageResponse> imageResponses = movie.getImages()
                .stream()
                .map(imageMapper::toResponse)
                .toList();

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
                .images(imageResponses)
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
    public Page<MovieResponse> mapToResponsePage(Page<Movie> movies) {
        return movies.map(this::mapMovieToMovieResponse);
    }


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


    public Movie mapMovieRequestToMovie(MovieRequest movieRequest) {

        return Movie.builder()
                .title(movieRequest.getTitle())
                .summary(movieRequest.getSummary())
                .releaseDate(movieRequest.getReleaseDate())
                .duration(movieRequest.getDuration())
                .rating(movieRequest.getRating())
                .specialHalls(movieRequest.getSpecialHalls())
                .director(movieRequest.getDirector())
                .cast(movieRequest.getCast())
                .formats(movieRequest.getFormats())
                .genre(movieRequest.getGenre())
                .status(movieRequest.getStatus())
                //slug, cinemas and images will be assigned in MovieService.
                .build();
    }

    public void updateMovieFromRequest(MovieRequest request, Movie movie) {
        movie.setTitle(request.getTitle());
        movie.setSlug(request.getSlug());
        movie.setSummary(request.getSummary());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setDuration(request.getDuration());
        movie.setRating(request.getRating());
        movie.setSpecialHalls(request.getSpecialHalls());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setFormats(request.getFormats());
        movie.setGenre(request.getGenre());
        movie.setStatus(request.getStatus());
        // cinemas ve images servis tarafında set edilecek
    }
}
