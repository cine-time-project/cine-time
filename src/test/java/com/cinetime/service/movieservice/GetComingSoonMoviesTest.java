package com.cinetime.service.movieservice;

import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.MovieService;
import com.cinetime.service.helper.MovieServiceHelper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService.getComingSoonMovies Tests")
class GetComingSoonMoviesTest {

    @Mock
    private MovieServiceHelper movieServiceHelper;

    @InjectMocks
    private MovieService movieService;

    private Pageable pageable;
    private ResponseMessage<Page<MovieResponse>> responseWithMovie;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        Page<MovieResponse> page = new PageImpl<>(Collections.singletonList(
                MovieResponse.builder().id(1L).title("Dune: Part Two").build()
        ));
        responseWithMovie = ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Movies found")
                .returnBody(page)
                .build();
    }

    @Test
    @DisplayName("Should delegate to helper.getAllComingSoon when date is null")
    void shouldCallHelperForAllComingSoon_whenDateIsNull() {
        when(movieServiceHelper.getAllComingSoon(pageable)).thenReturn(responseWithMovie);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(null, pageable);

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(movieServiceHelper).getAllComingSoon(pageable);
        verifyNoMoreInteractions(movieServiceHelper);
    }

    @Test
    @DisplayName("Should delegate to helper.getComingSoonByDate when date is provided")
    void shouldCallHelperForComingSoonByDate_whenDateIsProvided() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        when(movieServiceHelper.getComingSoonByDate(date, pageable)).thenReturn(responseWithMovie);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(date, pageable);

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Dune: Part Two");

        verify(movieServiceHelper).getComingSoonByDate(date, pageable);
        verifyNoMoreInteractions(movieServiceHelper);
    }

    @Test
    @DisplayName("Should return empty page response when helper returns empty")
    void shouldReturnEmpty_whenHelperReturnsEmpty() {
        Page<MovieResponse> emptyPage = Page.empty(pageable);
        ResponseMessage<Page<MovieResponse>> emptyResponse =
                ResponseMessage.<Page<MovieResponse>>builder()
                        .httpStatus(HttpStatus.OK)
                        .message("No movies")
                        .returnBody(emptyPage)
                        .build();

        when(movieServiceHelper.getAllComingSoon(pageable)).thenReturn(emptyResponse);

        ResponseMessage<Page<MovieResponse>> result =
                movieService.getComingSoonMovies(null, pageable);

        assertThat(result.getReturnBody().getContent()).isEmpty();
        assertThat(result.getMessage()).isEqualTo("No movies");
    }

    @Test
    @DisplayName("Should propagate exception from helper")
    void shouldPropagateException_whenHelperThrows() {
        when(movieServiceHelper.getAllComingSoon(pageable))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> movieService.getComingSoonMovies(null, pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    @DisplayName("Should use correct pageable parameters")
    void shouldUseCorrectPageableParameters() {
        Pageable customPageable = PageRequest.of(2, 5);
        LocalDate date = LocalDate.of(2025, 7, 1);

        when(movieServiceHelper.getComingSoonByDate(date, customPageable)).thenReturn(responseWithMovie);

        movieService.getComingSoonMovies(date, customPageable);

        verify(movieServiceHelper).getComingSoonByDate(date, customPageable);
    }
}
