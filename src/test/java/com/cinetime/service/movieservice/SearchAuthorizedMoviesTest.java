package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.service.business.MovieService;
import com.cinetime.service.helper.MovieServiceHelper;
import com.cinetime.service.helper.PageableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService - searchAuthorizedMovies Tests")
class SearchAuthorizedMoviesTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private ShowtimeRepository showtimeRepository;

    private MovieServiceHelper movieServiceHelper; // manuel spy

    @InjectMocks
    private MovieService movieService;

    private Pageable pageable;
    private Movie testMovie;
    private MovieResponse testMovieResponse;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        // dependencies verip spy oluşturuyoruz
        movieServiceHelper = Mockito.spy(new MovieServiceHelper(showtimeRepository, movieMapper, movieRepository));

        // movieService içindeki helper field'ını replace et
        movieService = new MovieService(
                movieRepository,
                pageableHelper,
                movieMapper,
                null, // cinemaService testte lazım değil
                null, // imageService testte lazım değil
                showtimeRepository,
                movieServiceHelper
        );

        testMovie = Movie.builder()
                .id(1L)
                .title("Inception")
                .slug("inception")
                .summary("A mind-bending thriller")
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testMovieResponse = MovieResponse.builder()
                .id(1L)
                .title("Inception")
                .slug("inception")
                .summary("A mind-bending thriller")
                .status(MovieStatus.IN_THEATERS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return OK with movies when keyword matches")
    void shouldReturnOk_whenKeywordMatches() {
        Page<Movie> moviesPage = new PageImpl<>(Collections.singletonList(testMovie), pageable, 1);
        Page<MovieResponse> mappedPage = new PageImpl<>(Collections.singletonList(testMovieResponse), pageable, 1);

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(pageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase("inception", "inception", pageable))
                .thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedPage);

        ResponseMessage<Page<MovieResponse>> response =
                movieService.searchAuthorizedMovies("inception", 0, 10, "title", "asc");

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo(SuccessMessages.MOVIES_LISTED);
        assertThat(response.getReturnBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no movies match keyword")
    void shouldReturnNotFound_whenNoMoviesFound() {
        Page<Movie> emptyPage = Page.empty(pageable);

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(pageable);
        when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase("matrix", "matrix", pageable))
                .thenReturn(emptyPage);

        ResponseMessage<Page<MovieResponse>> response =
                movieService.searchAuthorizedMovies("matrix", 0, 10, "title", "asc");

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);
        assertThat(response.getReturnBody().getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return OK when no query provided and movies exist")
    void shouldReturnOk_whenNoQueryProvided() {
        Page<Movie> moviesPage = new PageImpl<>(Collections.singletonList(testMovie), pageable, 1);
        Page<MovieResponse> mappedPage = new PageImpl<>(Collections.singletonList(testMovieResponse), pageable, 1);

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(pageable);
        when(movieRepository.findAll(pageable)).thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedPage);

        ResponseMessage<Page<MovieResponse>> response =
                movieService.searchAuthorizedMovies(null, 0, 10, "title", "asc");

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo(SuccessMessages.MOVIES_LISTED);
        assertThat(response.getReturnBody().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no query provided and repository is empty")
    void shouldReturnNotFound_whenNoQueryProvidedAndNoMovies() {
        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(pageable);
        when(movieRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        ResponseMessage<Page<MovieResponse>> response =
                movieService.searchAuthorizedMovies(null, 0, 10, "title", "asc");

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND);
        assertThat(response.getReturnBody().getContent()).isEmpty();
    }
}
