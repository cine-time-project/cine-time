package com.cinetime.service.business;

import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.helper.MovieServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceUpdateMovieTest {

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
                .title("Updated Movie")
                .slug("updated-slug")
                .summary("Updated Summary")
                .build();

        movie = Movie.builder()
                .id(1L)
                .title("Old Movie")
                .slug("old-slug")
                .summary("Old Summary")
                .build();

        movieResponse = MovieResponse.builder()
                .title("Updated Movie")
                .summary("Updated Summary")
                .slug("updated-slug")
                .build();
    }

    @Test
    void testUpdateMovie_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieServiceHelper.generateUniqueSlug(anyString(), any(), anyInt(), eq(1L))).thenReturn("updated-slug");
        doNothing().when(movieMapper).updateMovieFromRequest(movieRequest, movie);
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(movieResponse);

        ResponseMessage<MovieResponse> response = movieService.updateMovie(movieRequest, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(movieResponse, response.getReturnBody());

        verify(movieRepository, times(1)).save(movie);
    }

    @Test
    void testUpdateMovie_NotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.updateMovie(movieRequest, 1L));
    }
}
