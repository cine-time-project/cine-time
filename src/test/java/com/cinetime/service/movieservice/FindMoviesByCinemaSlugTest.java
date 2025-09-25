package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.business.MovieService;
import com.cinetime.service.helper.MovieServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindMoviesByCinemaSlugTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private MovieServiceHelper movieServiceHelper;

    @InjectMocks
    private MovieService movieService;

    private Pageable mockPageable;
    private Page<Movie> mockMoviePage;
    private CinemaMovieResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockPageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        Movie movie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();

        mockResponse = CinemaMovieResponse.builder()
                .id(1L)
                .title("Test Movie")
                .build();

        mockMoviePage = new PageImpl<>(List.of(movie), mockPageable, 1);
    }

    @Test
    @DisplayName("Should return movies when cinema slug exists")
    void findMoviesByCinemaSlug_WithValidSlug_ShouldReturnMovies() {
        String slug = "test-slug";

        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapMovieToCinemaMovieResponse(any(Movie.class))).thenReturn(mockResponse);

        ResponseMessage<Page<CinemaMovieResponse>> result =
                movieService.findMoviesByCinemaSlug(slug, mockPageable);

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Test Movie");
        assertThat(result.getMessage()).isEqualTo(String.format(SuccessMessages.MOVIE_WITH_SLUG_FOUND, slug));

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no movies exist for slug")
    void findMoviesByCinemaSlug_WithNoMovies_ShouldReturnNotFound() {
        String slug = "empty-slug";
        Page<Movie> emptyPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        ResponseMessage<Page<CinemaMovieResponse>> emptyResponse =
                ResponseMessage.<Page<CinemaMovieResponse>>builder()
                        .returnBody(Page.empty(mockPageable))
                        .message(ErrorMessages.MOVIES_NOT_FOUND)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build();

        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable)).thenReturn(emptyPage);
        when(movieServiceHelper.buildEmptyPageResponse(mockPageable, ErrorMessages.MOVIES_NOT_FOUND, HttpStatus.NOT_FOUND))
                .thenReturn((ResponseMessage) emptyResponse);

        ResponseMessage<Page<CinemaMovieResponse>> result =
                movieService.findMoviesByCinemaSlug(slug, mockPageable);

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);
        assertThat(result.getReturnBody().getContent()).isEmpty();

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
        verify(movieServiceHelper).buildEmptyPageResponse(mockPageable, ErrorMessages.MOVIES_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should propagate exception when repository throws error")
    void findMoviesByCinemaSlug_WhenRepositoryThrowsException_ShouldPropagate() {
        String slug = "fail-slug";

        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() ->
                movieService.findMoviesByCinemaSlug(slug, mockPageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
    }

    @Test
    @DisplayName("Should handle case-insensitive slug correctly")
    void findMoviesByCinemaSlug_WithMixedCaseSlug_ShouldReturnMovies() {
        String slug = "TesT-sLuG";

        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapMovieToCinemaMovieResponse(any(Movie.class))).thenReturn(mockResponse);

        ResponseMessage<Page<CinemaMovieResponse>> result =
                movieService.findMoviesByCinemaSlug(slug, mockPageable);

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
    }

    @Test
    @DisplayName("Should throw exception when slug is null")
    void findMoviesByCinemaSlug_WithNullSlug_ShouldThrowException() {
        assertThatThrownBy(() -> movieService.findMoviesByCinemaSlug(null, mockPageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cinema slug cannot be null or empty");

        verifyNoInteractions(movieRepository);
        verifyNoInteractions(movieMapper);
    }

    @Test
    @DisplayName("Should throw exception when slug is empty")
    void findMoviesByCinemaSlug_WithEmptySlug_ShouldThrowException() {
        assertThatThrownBy(() -> movieService.findMoviesByCinemaSlug("   ", mockPageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cinema slug cannot be null or empty");

        verifyNoInteractions(movieRepository);
        verifyNoInteractions(movieMapper);
    }
}
