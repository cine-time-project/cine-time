package com.cinetime.service.business;

import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.helper.PageableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MovieServiceTest {

  @Mock
  private MovieRepository movieRepository;

  @Mock
  private PageableHelper pageableHelper;

  @Mock
  private MovieMapper movieMapper;

  @InjectMocks
  private MovieService movieService;

  private Movie movie;
  private MovieResponse movieResponse;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    movie = new Movie();
    movie.setId(1L);
    movie.setTitle("Inception");

    movieResponse = new MovieResponse();
    movieResponse.setId(1L);
    movieResponse.setTitle("Inception");
  }

  @Test
  void searchMovies_WithQuery_ShouldReturnFilteredMovies() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
    when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(pageable);

    Page<Movie> moviePage = new PageImpl<>(Arrays.asList(movie));
    when(movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase("inc", "inc",
        pageable))
        .thenReturn(moviePage);
    when(movieMapper.mapToResponsePage(moviePage)).thenReturn(
        new PageImpl<>(Arrays.asList(movieResponse)));

    ResponseMessage<Page<MovieResponse>> response = movieService.searchMovies("inc", 0, 10, "title",
        "asc");

    assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
    assertThat(response.getReturnBody().getContent()).hasSize(1);
    assertThat(response.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Inception");

    verify(movieRepository, times(1))
        .findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase("inc", "inc", pageable);
  }

  @Test
  void searchMovies_WithoutQuery_ShouldReturnAllMovies() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
    when(pageableHelper.buildPageable(0, 10, "title", "asc")).thenReturn(pageable);

    Page<Movie> moviePage = new PageImpl<>(Arrays.asList(movie));
    when(movieRepository.findAll(pageable)).thenReturn(moviePage);
    when(movieMapper.mapToResponsePage(moviePage)).thenReturn(
        new PageImpl<>(Arrays.asList(movieResponse)));

    ResponseMessage<Page<MovieResponse>> response = movieService.searchMovies(null, 0, 10, "title",
        "asc");

    assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
    assertThat(response.getReturnBody().getContent()).hasSize(1);
    assertThat(response.getReturnBody().getContent().get(0).getTitle()).isEqualTo("Inception");

    verify(movieRepository, times(1)).findAll(pageable);
  }

  @Test
  void getMovieById_WhenMovieExists_ShouldReturnMovieResponse() {
    when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
    when(movieMapper.mapMovieToMovieResponse(movie)).thenReturn(movieResponse);

    ResponseMessage<MovieResponse> response = movieService.getMovieById(1L);

    assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
    assertThat(response.getReturnBody().getTitle()).isEqualTo("Inception");

    verify(movieRepository, times(1)).findById(1L);
  }

  @Test
  void getMovieById_WhenMovieDoesNotExist_ShouldThrowException() {
    when(movieRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(99L));

    verify(movieRepository, times(1)).findById(99L);
  }
}