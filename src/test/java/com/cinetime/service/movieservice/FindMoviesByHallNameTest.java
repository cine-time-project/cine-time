package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.business.MovieService;
import com.cinetime.service.helper.MovieServiceHelper;
import com.cinetime.service.helper.PageableHelper;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService - findMoviesByHallName Tests")
class FindMoviesByHallNameTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private MovieServiceHelper movieServiceHelper;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private MovieResponse testMovieResponse;
    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(1L)
                .title("Dune: Part Two")
                .slug("dune-part-two")
                .summary("Epic sci-fi sequel")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .duration(165)
                .rating(8.5)
                .director("Denis Villeneuve")
                .cast(Arrays.asList("Timothée Chalamet", "Zendaya"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Sci-Fi", "Adventure"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovieResponse = MovieResponse.builder()
                .id(1L)
                .title("Dune: Part Two")
                .slug("dune-part-two")
                .summary("Epic sci-fi sequel")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .duration(165)
                .rating(8.5)
                .director("Denis Villeneuve")
                .cast(Arrays.asList("Timothée Chalamet", "Zendaya"))
                .formats(Arrays.asList("IMAX", "Standard"))
                .genre(Arrays.asList("Sci-Fi", "Adventure"))
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        defaultPageable = PageRequest.of(0, 10, Sort.by("releaseDate").ascending());
    }

    @Test
    @DisplayName("Should return OK with movies when hall has movies")
    void shouldReturnOkWithMovies_whenHallHasMovies() {
        String hallName = "IMAX";
        Page<Movie> moviesPage = new PageImpl<>(Collections.singletonList(testMovie), defaultPageable, 1);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc"))
                .thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(hallName, defaultPageable))
                .thenReturn(moviesPage);
        when(movieMapper.mapMovieToMovieResponse(testMovie))
                .thenReturn(testMovieResponse);

        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(hallName, 0, 10, "releaseDate", "asc");

        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(res.getReturnBody()).isNotNull();
        assertThat(res.getReturnBody().getContent()).hasSize(1);
        assertThat(res.getReturnBody().getContent().get(0).getTitle())
                .isEqualTo("Dune: Part Two");
        assertThat(res.getMessage())
                .isEqualTo(String.format(SuccessMessages.MOVIES_FOUND_IN_HALL, hallName));
    }

    @Test
    @DisplayName("Should return NOT_FOUND when hall has no movies")
    void shouldReturnNotFound_whenHallHasNoMovies() {
        String hallName = "VIP";
        Page<Movie> emptyPage = Page.empty(defaultPageable);

        ResponseMessage<Page<MovieResponse>> emptyResponse =
                ResponseMessage.<Page<MovieResponse>>builder()
                        .returnBody(Page.empty(defaultPageable))
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(ErrorMessages.MOVIES_NOT_FOUND_IN_HALL)
                        .build();

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc"))
                .thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(hallName, defaultPageable))
                .thenReturn(emptyPage);
        when(movieServiceHelper.buildEmptyPageResponse(defaultPageable,
                ErrorMessages.MOVIES_NOT_FOUND_IN_HALL, HttpStatus.NOT_FOUND))
                .thenReturn((ResponseMessage) emptyResponse);

        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(hallName, 0, 10, "releaseDate", "asc");

        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND_IN_HALL);
    }

    @Test
    @DisplayName("Should handle different pagination and sort parameters correctly")
    void shouldHandleDifferentPaginationAndSort() {
        String hallName = "4DX";
        Pageable custom = PageRequest.of(2, 5, Sort.by("releaseDate").descending());
        Page<Movie> moviesPage = new PageImpl<>(Collections.singletonList(testMovie), custom, 1);

        when(pageableHelper.buildPageable(2, 5, "releaseDate", "desc"))
                .thenReturn(custom);
        when(movieRepository.findAllByHallIgnoreCase(hallName, custom))
                .thenReturn(moviesPage);
        when(movieMapper.mapMovieToMovieResponse(testMovie))
                .thenReturn(testMovieResponse);

        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(hallName, 2, 5, "releaseDate", "desc");

        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(res.getMessage())
                .isEqualTo(String.format(SuccessMessages.MOVIES_FOUND_IN_HALL, hallName));
    }

    @Test
    @DisplayName("Should throw exception when hallName is null")
    void shouldHandleNullHallName() {
        assertThatThrownBy(() ->
                movieService.findMoviesByHallName(null, 0, 10, "releaseDate", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hall name cannot be null or empty");

        verifyNoInteractions(movieRepository, movieMapper, movieServiceHelper);
    }

    @Test
    @DisplayName("Should throw exception when hallName is empty")
    void shouldHandleEmptyHallName() {
        assertThatThrownBy(() ->
                movieService.findMoviesByHallName("   ", 0, 10, "releaseDate", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hall name cannot be null or empty");

        verifyNoInteractions(movieRepository, movieMapper, movieServiceHelper);
    }

    @Test
    @DisplayName("Should propagate exception when repository throws")
    void shouldPropagateWhenRepositoryThrows() {
        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc"))
                .thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() ->
                movieService.findMoviesByHallName("IMAX", 0, 10, "releaseDate", "asc"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }
}
