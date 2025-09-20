package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.service.business.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceGetMovieByIdTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
    }

    @Test
    void testGetMovieById_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(new MovieResponse());

        ResponseMessage<MovieResponse> response = movieService.getMovieById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        verify(movieRepository, times(1)).findById(1L);
        verify(movieMapper, times(1)).mapMovieToMovieResponse(movie);
    }

    @Test
    void testGetMovieById_NotFound() {
        when(movieRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> movieService.getMovieById(2L));

        assertTrue(exception.getMessage().contains("not found"));
        verify(movieRepository, times(1)).findById(2L);
    }
}

