package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.business.CinemaService;
import com.cinetime.service.business.ImageService;
import com.cinetime.service.business.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceSaveMovieTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private CinemaService cinemaService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private MovieService movieService;

    private MovieRequest movieRequest;
    private Movie movie;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // MovieRequest oluştur
        movieRequest = MovieRequest.builder()
                .title("Inception")
                .slug("inception-2025")
                .summary("A mind-bending thriller")
                .releaseDate(LocalDate.of(2025, 12, 25))
                .duration(148)
                .rating(8.8)
                .specialHalls("IMAX")
                .director("Christopher Nolan")
                .cast(List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt"))
                .formats(List.of("2D", "3D"))
                .genre(List.of("Action", "Sci-Fi"))
                .status(MovieStatus.COMING_SOON)
                .cinemaIds(Set.of(1L))
                .imageIds(Set.of(1L))
                .build();

        // Movie entity oluştur
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
    }

    @Test
    void testSaveMovie_Success() {
        // Mock dönüşler
        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(cinemaService.getById(1L)).thenReturn(null); // sadece mock, gerçek entity dönmüyor
        when(imageService.getImageEntity(1L)).thenReturn(null); // sadece mock
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(new MovieResponse());

        // Test edilen method
        ResponseMessage<MovieResponse> response = movieService.saveMovie(movieRequest);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        verify(movieRepository, times(1)).save(movie);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(movie);
        verify(cinemaService, times(1)).getById(1L);
        verify(imageService, times(1)).getImageEntity(1L);
    }
}
