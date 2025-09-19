package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.CinemaMovieResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindMoviesByCinemaSlugTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Pageable mockPageable;
    private Page<Movie> mockMoviePage;
    private Page<CinemaMovieResponse> mockResponsePage;
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
        mockResponsePage = new PageImpl<>(List.of(mockResponse), mockPageable, 1);
    }

    @Test
    @DisplayName("Should return movies when cinema slug exists")
    void findMoviesByCinemaSlug_WithValidSlug_ShouldReturnMovies() {
        // given
        String slug = "test-slug";
        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapToCinemaResponsePage(mockMoviePage)).thenReturn(mockResponsePage);

        // when
        ResponseMessage<Page<CinemaMovieResponse>> result =
                movieService.findMoviesByCinemaSlug(slug, 0, 10, "title", "asc");

        // then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);
        assertThat(result.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Test Movie");
        assertThat(result.getMessage()).isEqualTo(String.format(SuccessMessages.MOVIE_WITH_SLUG_FOUND, slug));

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no movies exist for slug")
    void findMoviesByCinemaSlug_WithNoMovies_ShouldReturnNotFound() {
        // given
        String slug = "empty-slug";
        Page<Movie> emptyPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);
        Page<CinemaMovieResponse> emptyResponsePage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable)).thenReturn(emptyPage);
        when(movieMapper.mapToCinemaResponsePage(emptyPage)).thenReturn(emptyResponsePage);

        // when
        ResponseMessage<Page<CinemaMovieResponse>> result =
                movieService.findMoviesByCinemaSlug(slug, 0, 10, "title", "asc");

        // then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.MOVIES_NOT_FOUND); // ✅ düzeltildi
        assertThat(result.getReturnBody()).isNull();

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
    }


    @Test
    @DisplayName("Should propagate exception when repository throws error")
    void findMoviesByCinemaSlug_WhenRepositoryThrowsException_ShouldPropagate() {
        // given
        String slug = "fail-slug";
        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable))
                .thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThatThrownBy(() ->
                movieService.findMoviesByCinemaSlug(slug, 0, 10, "title", "asc"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
    }

    @Test
    @DisplayName("Should handle case-insensitive slug correctly")
    void findMoviesByCinemaSlug_WithMixedCaseSlug_ShouldReturnMovies() {
        // given
        String slug = "TesT-sLuG";
        when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(mockPageable);
        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, mockPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapToCinemaResponsePage(mockMoviePage)).thenReturn(mockResponsePage);

        // when
        ResponseMessage<Page<CinemaMovieResponse>> result =
                movieService.findMoviesByCinemaSlug(slug, 0, 10, "title", "asc");

        // then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getReturnBody().getContent()).hasSize(1);

        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, mockPageable);
    }

    @Test
    @DisplayName("Should use correct pagination parameters")
    void findMoviesByCinemaSlug_WithCustomPagination_ShouldUseCorrectParams() {
        // given
        String slug = "test-slug";
        Pageable customPageable = PageRequest.of(2, 20, Sort.by("releaseDate").descending());

        when(pageableHelper.buildPageable(2, 20, "releaseDate", "desc")).thenReturn(customPageable);
        when(movieRepository.findAllByCinemaSlugIgnoreCase(slug, customPageable)).thenReturn(mockMoviePage);
        when(movieMapper.mapToCinemaResponsePage(mockMoviePage)).thenReturn(mockResponsePage);

        // when
        movieService.findMoviesByCinemaSlug(slug, 2, 20, "releaseDate", "desc");

        // then
        verify(pageableHelper).buildPageable(2, 20, "releaseDate", "desc");
        verify(movieRepository).findAllByCinemaSlugIgnoreCase(slug, customPageable);
    }

    @Test
    @DisplayName("Should throw exception when slug is null")
    void findMoviesByCinemaSlug_WithNullSlug_ShouldThrowException() {
        // No stubbing here because service should fail before calling repository

        assertThatThrownBy(() -> movieService.findMoviesByCinemaSlug(null, 0, 10, "title", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cinema slug cannot be null or empty");

        // Verify repository is never called
        verifyNoInteractions(movieRepository);
        verifyNoInteractions(movieMapper);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when cinemaSlug is empty")
    void findMoviesByCinemaSlug_WithEmptySlug_ShouldThrowException() {
        assertThatThrownBy(() -> movieService.findMoviesByCinemaSlug("   ", 0, 10, "title", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cinema slug cannot be null or empty");

        verifyNoInteractions(movieRepository);
        verifyNoInteractions(movieMapper);
    }
}
