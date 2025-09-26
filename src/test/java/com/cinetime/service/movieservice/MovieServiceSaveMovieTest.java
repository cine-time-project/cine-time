package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.business.CinemaService;
import com.cinetime.service.business.ImageService;
import com.cinetime.service.business.MovieService;
import com.cinetime.service.helper.MovieServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

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
    @Mock
    private MovieServiceHelper movieServiceHelper;

    @InjectMocks
    private MovieService movieService;

    private MovieRequest movieRequest;
    private Movie movie;
    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        movieRequest = MovieRequest.builder()
                .title("Test Movie")
                .slug(null)
                .summary("Test Summary")
                .build();

        movie = Movie.builder()
                .title("Test Movie")
                .summary("Test Summary")
                .build();

        movieResponse = MovieResponse.builder()
                .title("Test Movie")
                .summary("Test Summary")
                .build();
    }

    @Test
    void testSaveMovie_Success() {
        when(movieMapper.mapMovieRequestToMovie(movieRequest)).thenReturn(movie);
        when(movieServiceHelper.generateUniqueSlug(anyString(), any(), anyInt(), isNull())).thenReturn("test-movie");
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(movieResponse);

        ResponseMessage<MovieResponse> response = movieService.saveMovie(movieRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(movieResponse, response.getReturnBody());
        assertEquals("test-movie", movie.getSlug());

        verify(movieRepository, times(1)).save(movie);
    }
}
