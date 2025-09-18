package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.business.MovieService;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchMoviesTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie mockMovie;
    private MovieResponse mockMovieResponse;
    private Page<Movie> mockMoviePage;
    private Page<MovieResponse> mockMovieResponsePage;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        // Mock Movie entity
        mockMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .summary("Test Summary")
                .slug("test-movie")
                .releaseDate(LocalDate.now())
                .duration(120)
                .director("Test Director")
                .genre(Collections.singletonList("Action"))
                .status(MovieStatus.IN_THEATERS)
                .build();

        // Mock MovieResponse DTO
        mockMovieResponse = MovieResponse.builder()
                .id(1L)
                .title("Test Movie")
                .summary("Test Summary")
                .slug("test-movie")
                .releaseDate(LocalDate.now())
                .duration(120)
                .director("Test Director")
                .genre(Collections.singletonList("Action"))
                .status(MovieStatus.IN_THEATERS)
                .build();

        // Mock Pageable
        mockPageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        // Mock Page objects
        mockMoviePage = new PageImpl<>(List.of(mockMovie), mockPageable, 1);
        mockMovieResponsePage = new PageImpl<>(List.of(mockMovieResponse), mockPageable, 1);
    }

    @Test
    @DisplayName("Should return movies when valid query is provided")
    void searchMovies_WithValidQuery_ShouldReturnMovies() {
        // Given
        String query = "test";

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, 0, 10, "title", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Test Movie");

        verify(pageableHelper).buildPageable(0, 10, "title", "asc");
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper).mapToResponsePage(mockMoviePage);
    }

    @Test
    @DisplayName("Should return empty page when no movies match the query")
    void searchMovies_WithNoMatches_ShouldReturnEmptyPage() {
        // Given
        String query = "nonexistent";
        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);
        Page<MovieResponse> emptyResponsePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(emptyMoviePage);
        when(movieMapper.mapToResponsePage(emptyMoviePage)).thenReturn(emptyResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, 0, 10, "title", "asc");

        // Then
        assertThat(result.getReturnBody().getContent()).isEmpty();
        assertThat(result.getReturnBody().getTotalElements()).isEqualTo(0);

        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper).mapToResponsePage(emptyMoviePage);
    }

    @Test
    @DisplayName("Should return all movies when query is empty")
    void searchMovies_WithEmptyQuery_ShouldReturnAllMovies() {
        // Given
        String query = "";

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findAll(mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, 0, 10, "title", "asc");

        // Then
        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(movieRepository).findAll(mockPageable);
        verify(movieMapper).mapToResponsePage(mockMoviePage);
    }

    @Test
    @DisplayName("Should return all movies when query is null")
    void searchMovies_WithNullQuery_ShouldReturnAllMovies() {
        // Given
        String query = null;

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findAll(mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, 0, 10, "title", "asc");

        // Then
        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(movieRepository).findAll(mockPageable);
        verify(movieMapper).mapToResponsePage(mockMoviePage);
    }

    @Test
    @DisplayName("Should handle repository exception")
    void searchMovies_WhenRepositoryThrowsException_ShouldPropagate() {
        // Given
        String query = "test";

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> movieService.searchMovies(query, 0, 10, "title", "asc"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper, never()).mapToResponsePage(any());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void searchMovies_WithMixedCaseQuery_ShouldReturnMovies() {
        // Given
        String query = "TeSt";

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, 0, 10, "title", "asc");

        // Then
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
    }

    @Test
    @DisplayName("Should use correct pagination parameters")
    void searchMovies_WithCustomPagination_ShouldApplyCorrectParameters() {
        // Given
        String query = "test";
        Pageable customPageable = PageRequest.of(2, 20, Sort.by("releaseDate").descending());

        when(pageableHelper.buildPageable(2, 20, "releaseDate", "desc")).thenReturn(customPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, customPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        movieService.searchMovies(query, 2, 20, "releaseDate", "desc");

        // Then
        verify(pageableHelper).buildPageable(2, 20, "releaseDate", "desc");
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, customPageable);
    }

    @Test
    @DisplayName("Should fallback to default sort when type is invalid")
    void searchMovies_WithInvalidSortType_ShouldDefaultToAsc() {
        // Given
        String query = "test";
        Pageable defaultPageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        when(pageableHelper.buildPageable(0, 10, "title", "invalid")).thenReturn(defaultPageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, defaultPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, 0, 10, "title", "invalid");

        // Then
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, defaultPageable);
    }

    @Test
    @DisplayName("Should return empty page when page number is too large")
    void searchMovies_WithLargePageNumber_ShouldReturnEmpty() {
        // Given
        String query = "test";
        Pageable largePageable = PageRequest.of(9999, 10, Sort.by("title").ascending());
        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), largePageable, 0);
        Page<MovieResponse> emptyResponsePage = new PageImpl<>(Collections.emptyList(), largePageable, 0);

        when(pageableHelper.buildPageable(9999, 10, "title", "asc")).thenReturn(largePageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, largePageable))
                .thenReturn(emptyMoviePage);
        when(movieMapper.mapToResponsePage(emptyMoviePage)).thenReturn(emptyResponsePage);

        // When
        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, 9999, 10, "title", "asc");

        // Then
        assertThat(result.getReturnBody().getContent()).isEmpty();
        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, largePageable);
    }
}

