package com.cinetime.service.movieservice;

import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService.getMoviesInTheatres Tests")
class GetMoviesInTheatresTest {

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieServiceHelper movieServiceHelper;

    @InjectMocks
    private MovieService movieService;

    private Pageable pageable;
    private ResponseMessage<Page<MovieResponse>> responseWithMovie;

    @BeforeEach
    void setUp() {
        pageable = Pageable.ofSize(10);
        Page<MovieResponse> page = new PageImpl<>(Collections.singletonList(
                MovieResponse.builder().id(1L).title("Inception").build()
        ));
        responseWithMovie = ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Movies found")
                .returnBody(page)
                .build();
    }

    @Test
    @DisplayName("Should delegate to helper.getCurrentlyInTheatres when date is null")
    void shouldCallHelperForCurrentlyInTheatres_whenDateIsNull() {
        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(movieServiceHelper.getCurrentlyInTheatres(pageable)).thenReturn(responseWithMovie);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.getMoviesInTheatres(null, 0, 10, "releaseDate", "asc");

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(pageableHelper).buildPageable(0, 10, "releaseDate", "asc");
        verify(movieServiceHelper).getCurrentlyInTheatres(pageable);
        verifyNoMoreInteractions(movieServiceHelper);
    }

    @Test
    @DisplayName("Should delegate to helper.getMoviesByDate when date is provided")
    void shouldCallHelperForMoviesByDate_whenDateIsProvided() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        when(pageableHelper.buildPageable(1, 5, "title", "desc")).thenReturn(pageable);
        when(movieServiceHelper.getMoviesByDate(date, pageable)).thenReturn(responseWithMovie);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.getMoviesInTheatres(date, 1, 5, "title", "desc");

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Inception");

        verify(pageableHelper).buildPageable(1, 5, "title", "desc");
        verify(movieServiceHelper).getMoviesByDate(date, pageable);
        verifyNoMoreInteractions(movieServiceHelper);
    }
}
