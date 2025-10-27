package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.business.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService.getComingSoonMovies Tests")
class GetComingSoonMoviesTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Pageable pageable;
    private Movie movie;
    private Page<Movie> moviePage;
    private MovieResponse movieResponse;
    private Page<MovieResponse> movieResponsePage;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        // Create a sample Movie entity
        movie = Movie.builder()
                .id(1L)
                .title("Dune: Part Two")
                .status(MovieStatus.COMING_SOON)
                .releaseDate(LocalDate.of(2025, 6, 1))
                .build();

        moviePage = new PageImpl<>(Collections.singletonList(movie), pageable, 1);

        // Create corresponding MovieResponse
        movieResponse = MovieResponse.builder()
                .id(1L)
                .title("Dune: Part Two")
                .status(MovieStatus.COMING_SOON)
                .releaseDate(LocalDate.of(2025, 6, 1))
                .build();

        movieResponsePage = new PageImpl<>(Collections.singletonList(movieResponse), pageable, 1);
    }

    @Test
    @DisplayName("Should fetch all coming soon movies when date is null")
    void shouldFetchAllComingSoonMovies_whenDateIsNull() {
        // Given
        when(movieRepository.findByStatus(eq(MovieStatus.COMING_SOON), eq(pageable)))
                .thenReturn(moviePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found");
        assertThat(result.getReturnBody()).isNotNull();
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Dune: Part Two");

        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, pageable);
        verify(movieMapper).mapToResponsePage(any(Page.class));
        verify(movieRepository, never()).findByStatusAndReleaseDateGreaterThanEqual(
                any(MovieStatus.class), any(LocalDate.class), any(Pageable.class));
        verifyNoMoreInteractions(movieRepository, movieMapper);
    }

    @Test
    @DisplayName("Should fetch coming soon movies by date when date is provided")
    void shouldFetchComingSoonMoviesByDate_whenDateIsProvided() {
        // Given
        LocalDate date = LocalDate.of(2025, 6, 1);
        when(movieRepository.findByStatusAndReleaseDateGreaterThanEqual(
                eq(MovieStatus.COMING_SOON), eq(date), eq(pageable)))
                .thenReturn(moviePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(date, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Movies found");
        assertThat(result.getReturnBody()).isNotNull();
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Dune: Part Two");

        verify(movieRepository).findByStatusAndReleaseDateGreaterThanEqual(
                MovieStatus.COMING_SOON, date, pageable);
        verify(movieMapper).mapToResponsePage(any(Page.class));
        verify(movieRepository, never()).findByStatus(any(MovieStatus.class), any(Pageable.class));
        verifyNoMoreInteractions(movieRepository, movieMapper);
    }

    @Test
    @DisplayName("Should return empty page when no movies found")
    void shouldReturnEmptyPage_whenNoMoviesFound() {
        // Given
        Page<Movie> emptyMoviePage = Page.empty(pageable);
        Page<MovieResponse> emptyResponsePage = Page.empty(pageable);

        when(movieRepository.findByStatus(eq(MovieStatus.COMING_SOON), eq(pageable)))
                .thenReturn(emptyMoviePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(emptyResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("No movies");
        assertThat(result.getReturnBody().getContent()).isEmpty();
        assertThat(result.getReturnBody().getTotalElements()).isEqualTo(0);

        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, pageable);
        verify(movieMapper).mapToResponsePage(any(Page.class));
    }

    @Test
    @DisplayName("Should handle multiple movies in response")
    void shouldHandleMultipleMovies_whenRepositoryReturnsMultiple() {
        // Given
        Movie movie2 = Movie.builder()
                .id(2L)
                .title("Avatar 3")
                .status(MovieStatus.COMING_SOON)
                .releaseDate(LocalDate.of(2025, 12, 15))
                .build();

        MovieResponse movieResponse2 = MovieResponse.builder()
                .id(2L)
                .title("Avatar 3")
                .status(MovieStatus.COMING_SOON)
                .releaseDate(LocalDate.of(2025, 12, 15))
                .build();

        List<Movie> movies = List.of(movie, movie2);
        List<MovieResponse> movieResponses = List.of(movieResponse, movieResponse2);

        Page<Movie> multiplePage = new PageImpl<>(movies, pageable, 2);
        Page<MovieResponse> multipleResponsePage = new PageImpl<>(movieResponses, pageable, 2);

        when(movieRepository.findByStatus(eq(MovieStatus.COMING_SOON), eq(pageable)))
                .thenReturn(multiplePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(multipleResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(null, pageable);

        // Then
        assertThat(result.getReturnBody().getContent()).hasSize(2);
        assertThat(result.getReturnBody().getTotalElements()).isEqualTo(2);
        assertThat(result.getReturnBody().getContent())
                .extracting(MovieResponse::getTitle)
                .containsExactly("Dune: Part Two", "Avatar 3");

        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, pageable);
        verify(movieMapper).mapToResponsePage(any(Page.class));
    }

    @Test
    @DisplayName("Should propagate exception when repository throws exception")
    void shouldPropagateException_whenRepositoryThrows() {
        // Given
        when(movieRepository.findByStatus(eq(MovieStatus.COMING_SOON), eq(pageable)))
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThatThrownBy(() -> movieService.getComingSoonMovies(null, pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");

        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, pageable);
        verifyNoInteractions(movieMapper);
    }

    @Test
    @DisplayName("Should use correct pageable parameters")
    void shouldUseCorrectPageableParameters() {
        // Given
        Pageable customPageable = PageRequest.of(2, 5);
        LocalDate date = LocalDate.of(2025, 7, 1);

        when(movieRepository.findByStatusAndReleaseDateGreaterThanEqual(
                eq(MovieStatus.COMING_SOON), eq(date), eq(customPageable)))
                .thenReturn(moviePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(movieResponsePage);

        // When
        movieService.getComingSoonMovies(date, customPageable);

        // Then
        verify(movieRepository).findByStatusAndReleaseDateGreaterThanEqual(
                MovieStatus.COMING_SOON, date, customPageable);
        verify(movieMapper).mapToResponsePage(any(Page.class));
    }

    @Test
    @DisplayName("Should handle different page sizes")
    void shouldHandleDifferentPageSizes() {
        // Given
        Pageable smallPage = PageRequest.of(0, 5);
        Page<Movie> smallMoviePage = new PageImpl<>(Collections.singletonList(movie), smallPage, 1);
        Page<MovieResponse> smallResponsePage = new PageImpl<>(Collections.singletonList(movieResponse), smallPage, 1);

        when(movieRepository.findByStatus(eq(MovieStatus.COMING_SOON), eq(smallPage)))
                .thenReturn(smallMoviePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(smallResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(null, smallPage);

        // Then
        assertThat(result.getReturnBody().getSize()).isEqualTo(5);
        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, smallPage);
        verify(movieMapper).mapToResponsePage(any(Page.class));
    }

    @Test
    @DisplayName("Should fetch movies for specific future date")
    void shouldFetchMoviesForSpecificFutureDate() {
        // Given
        LocalDate futureDate = LocalDate.of(2026, 1, 1);
        Movie futureMovie = Movie.builder()
                .id(3L)
                .title("Future Movie")
                .status(MovieStatus.COMING_SOON)
                .releaseDate(futureDate)
                .build();

        MovieResponse futureMovieResponse = MovieResponse.builder()
                .id(3L)
                .title("Future Movie")
                .status(MovieStatus.COMING_SOON)
                .releaseDate(futureDate)
                .build();

        Page<Movie> futurePage = new PageImpl<>(Collections.singletonList(futureMovie));
        Page<MovieResponse> futureResponsePage = new PageImpl<>(Collections.singletonList(futureMovieResponse));

        when(movieRepository.findByStatusAndReleaseDateGreaterThanEqual(
                eq(MovieStatus.COMING_SOON), eq(futureDate), eq(pageable)))
                .thenReturn(futurePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(futureResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(futureDate, pageable);

        // Then
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Future Movie");

        verify(movieRepository).findByStatusAndReleaseDateGreaterThanEqual(
                MovieStatus.COMING_SOON, futureDate, pageable);
        verify(movieMapper).mapToResponsePage(any(Page.class));
    }

    @Test
    @DisplayName("Should return correct HTTP status")
    void shouldReturnCorrectHttpStatus() {
        // Given
        when(movieRepository.findByStatus(eq(MovieStatus.COMING_SOON), eq(pageable)))
                .thenReturn(moviePage);
        when(movieMapper.mapToResponsePage(any(Page.class)))
                .thenReturn(movieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(null, pageable);

        // Then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        verify(movieRepository).findByStatus(MovieStatus.COMING_SOON, pageable);
        verify(movieMapper).mapToResponsePage(any(Page.class));
    }


}