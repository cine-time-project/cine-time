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
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("✅Should return OK with movies when hall has movies")
    void shouldReturnOkWithMovies_whenHallHasMovies() {
        // given
        String hallName = "IMAX";
        Page<Movie> moviesPage = new PageImpl<>(Collections.singletonList(testMovie), defaultPageable, 1);
        Page<MovieResponse> mappedPage = new PageImpl<>(Collections.singletonList(testMovieResponse), defaultPageable, 1);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(hallName, defaultPageable)).thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedPage);

        // when
        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(hallName, 0, 10, "releaseDate", "asc");

        // then
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(res.getReturnBody()).isNotNull();
        assertThat(res.getReturnBody().getContent()).hasSize(1);
        assertThat(res.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Dune: Part Two");
        assertThat(res.getMessage()).contains("IMAX"); // ✅ message assertion

        InOrder inOrder = inOrder(pageableHelper, movieRepository, movieMapper);
        inOrder.verify(pageableHelper).buildPageable(0, 10, "releaseDate", "asc");
        inOrder.verify(movieRepository).findAllByHallIgnoreCase(hallName, defaultPageable);
        inOrder.verify(movieMapper).mapToResponsePage(moviesPage);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("⚠️ Should return NOT_FOUND when hall has no movies")
    void shouldReturnNotFound_whenHallHasNoMovies() {
        // given
        String hallName = "VIP";
        Page<Movie> moviesPage = new PageImpl<>(Collections.emptyList(), defaultPageable, 0);
        Page<MovieResponse> mappedEmpty = new PageImpl<>(Collections.emptyList(), defaultPageable, 0);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(hallName, defaultPageable)).thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedEmpty);

        // when
        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(hallName, 0, 10, "releaseDate", "asc");

        // then
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getReturnBody()).isNull();
        assertThat(res.getMessage()).contains("VIP"); // ✅ message assertion
        verify(movieRepository).findAllByHallIgnoreCase(hallName, defaultPageable);
    }

    @Test
    @DisplayName("🔄 Should handle different pagination and sort parameters correctly")
    void shouldHandleDifferentPaginationAndSort() {
        // given
        String hallName = "4DX";
        Pageable custom = PageRequest.of(2, 5, Sort.by("releaseDate").descending());
        Page<Movie> moviesPage = new PageImpl<>(Collections.singletonList(testMovie), custom, 1);
        Page<MovieResponse> mappedPage = new PageImpl<>(Collections.singletonList(testMovieResponse), custom, 1);

        when(pageableHelper.buildPageable(2, 5, "releaseDate", "desc")).thenReturn(custom);
        when(movieRepository.findAllByHallIgnoreCase(hallName, custom)).thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedPage);

        // when
        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(hallName, 2, 5, "releaseDate", "desc");

        // then
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(res.getReturnBody().getContent()).hasSize(1);
        assertThat(res.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Dune: Part Two");
        assertThat(res.getMessage()).contains("4DX"); // ✅ message assertion
    }

    @Test
    @DisplayName("🚫 Should return NOT_FOUND when hallName is null")
    void shouldHandleNullHallName() {
        // given
        Page<Movie> moviesPage = Page.empty(defaultPageable);
        Page<MovieResponse> mappedEmpty = Page.empty(defaultPageable);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(null, defaultPageable)).thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedEmpty);

        // when
        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(null, 0, 10, "releaseDate", "asc");

        // then
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getReturnBody()).isNull();
        assertThat(res.getMessage()).contains("null"); // ✅ message assertion
        verify(movieRepository).findAllByHallIgnoreCase(null, defaultPageable);
    }

    @Test
    @DisplayName("⬜ Should return NOT_FOUND when hallName is empty")
    void shouldHandleEmptyHallName() {
        // given
        String hallName = "";
        Page<Movie> moviesPage = Page.empty(defaultPageable);
        Page<MovieResponse> mappedEmpty = Page.empty(defaultPageable);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(hallName, defaultPageable)).thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedEmpty);

        // when
        ResponseMessage<Page<MovieResponse>> res =
                movieService.findMoviesByHallName(hallName, 0, 10, "releaseDate", "asc");

        // then
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getReturnBody()).isNull();
        assertThat(res.getMessage()).contains(""); // ✅ message assertion
        verify(movieRepository).findAllByHallIgnoreCase("", defaultPageable);
    }

    @Test
    @DisplayName("🔡 Should pass hallName to repository exactly as provided (case handled in query)")
    void shouldPassCaseMixedHallNameToRepository() {
        // given
        String hallName = "iMAX";
        Page<Movie> moviesPage = Page.empty(defaultPageable);
        Page<MovieResponse> mappedEmpty = Page.empty(defaultPageable);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(eq(hallName), any(Pageable.class)))
                .thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedEmpty);

        // when
        movieService.findMoviesByHallName(hallName, 0, 10, "releaseDate", "asc");

        // then
        verify(movieRepository).findAllByHallIgnoreCase(eq("iMAX"), any(Pageable.class));
    }

    @Test
    @DisplayName("🔁 Should call buildPageable -> repository -> mapper in correct order")
    void shouldCallMethodsInOrder() {
        // given
        String hallName = "IMAX";
        Page<Movie> moviesPage = new PageImpl<>(Collections.singletonList(testMovie), defaultPageable, 1);
        Page<MovieResponse> mappedPage = new PageImpl<>(Collections.singletonList(testMovieResponse), defaultPageable, 1);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(hallName, defaultPageable)).thenReturn(moviesPage);
        when(movieMapper.mapToResponsePage(moviesPage)).thenReturn(mappedPage);

        // when
        movieService.findMoviesByHallName(hallName, 0, 10, "releaseDate", "asc");

        // then
        InOrder inOrder = inOrder(pageableHelper, movieRepository, movieMapper);
        inOrder.verify(pageableHelper).buildPageable(0, 10, "releaseDate", "asc");
        inOrder.verify(movieRepository).findAllByHallIgnoreCase(hallName, defaultPageable);
        inOrder.verify(movieMapper).mapToResponsePage(moviesPage);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("💥 Should propagate exception when repository throws")
    void shouldPropagateWhenRepositoryThrows() {
        // given
        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(defaultPageable);
        when(movieRepository.findAllByHallIgnoreCase(any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        // then
        assertThatThrownBy(() ->
                movieService.findMoviesByHallName("IMAX", 0, 10, "releaseDate", "asc"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }
}
