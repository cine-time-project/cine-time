package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.MovieResponse;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchMoviesTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private MovieServiceHelper movieServiceHelper;

    @InjectMocks
    private MovieService movieService;

    private Movie mockMovie;
    private MovieResponse mockMovieResponse;
    private Page<Movie> mockMoviePage;
    private Page<MovieResponse> mockMovieResponsePage;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
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

        mockPageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        mockMoviePage = new PageImpl<>(List.of(mockMovie), mockPageable, 1);
        mockMovieResponsePage = new PageImpl<>(List.of(mockMovieResponse), mockPageable, 1);
    }

    @Test
    @DisplayName("Should return movies when valid query is provided")
    void searchMovies_WithValidQuery_ShouldReturnMovies() {
        String query = "test";

        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, mockPageable);

        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Test Movie");

        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper).mapToResponsePage(mockMoviePage);
    }

    @Test
    @DisplayName("Should return empty page when no movies match the query")
    void searchMovies_WithNoMatches_ShouldReturnEmptyPage() {
        String query = "nonexistent";
        Page<Movie> emptyMoviePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        ResponseMessage<Page<MovieResponse>> emptyResponse =
                ResponseMessage.<Page<MovieResponse>>builder()
                        .httpStatus(HttpStatus.OK)
                        .message(ErrorMessages.MOVIES_NOT_FOUND)
                        .returnBody(Page.empty(mockPageable))
                        .build();

        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenReturn(emptyMoviePage);
        when(movieServiceHelper.buildEmptyPageResponse(eq(mockPageable), eq(ErrorMessages.MOVIES_NOT_FOUND), eq(HttpStatus.OK)))
                .thenReturn((ResponseMessage) emptyResponse);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, mockPageable);

        assertThat(result.getReturnBody().getContent()).isEmpty();
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
    }

    @Test
    @DisplayName("Should return all movies when query is empty")
    void searchMovies_WithEmptyQuery_ShouldReturnAllMovies() {
        String query = "";

        when(movieRepository.findAll(mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, mockPageable);

        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(movieRepository).findAll(mockPageable);
        verify(movieMapper).mapToResponsePage(mockMoviePage);
    }

    @Test
    @DisplayName("Should return all movies when query is null")
    void searchMovies_WithNullQuery_ShouldReturnAllMovies() {
        String query = null;

        when(movieRepository.findAll(mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapToResponsePage(mockMoviePage)).thenReturn(mockMovieResponsePage);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.searchMovies(query, mockPageable);

        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(movieRepository).findAll(mockPageable);
        verify(movieMapper).mapToResponsePage(mockMoviePage);
    }

    @Test
    @DisplayName("Should handle repository exception")
    void searchMovies_WhenRepositoryThrowsException_ShouldPropagate() {
        String query = "test";

        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> movieService.searchMovies(query, mockPageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(movieRepository).findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query, mockPageable);
        verify(movieMapper, never()).mapToResponsePage(any());
    }
}
