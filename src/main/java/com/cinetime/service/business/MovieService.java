package com.cinetime.service.business;

import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

  private final MovieRepository movieRepository;
  private final PageableHelper pageableHelper;
  private final MovieMapper movieMapper;


  //M01
  public ResponseMessage<Page<MovieResponse>> searchMovies(String q, int page, int size,
      String sort, String type) {

    Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);
    Page<Movie> movies;
    if (q != null && !q.trim().isEmpty()) {
      String keyword = q.trim();
      movies = movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(keyword,
          keyword, pageable);
    } else {
      movies = movieRepository.findAll(pageable);
    }
    return ResponseMessage.<Page<MovieResponse>>builder()
        .message("Movies have been found successfully")
        .httpStatus(HttpStatus.OK)
        .returnBody(movieMapper.mapToResponsePage(movies))
        .build();

  }

  // M02
  public ResponseMessage<Page<CinemaMovieResponse>> findMoviesByCinemaSlug(
      String cinemaSlug, int page, int size, String sort, String type) {

    if (cinemaSlug == null || cinemaSlug.trim().isEmpty()) {
      throw new IllegalArgumentException("Cinema slug cannot be null or empty");
    }

    Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);
    Page<CinemaMovieResponse> response = movieMapper
        .mapToCinemaResponsePage(movieRepository.findAllBySlugIgnoreCase(cinemaSlug, pageable));

    if (response.isEmpty()) {
      return ResponseMessage.<Page<CinemaMovieResponse>>builder()
          .message(ErrorMessages.MOVIE_NOT_FOUND)
          .httpStatus(HttpStatus.NOT_FOUND)
          .build();
    }
    return ResponseMessage.<Page<CinemaMovieResponse>>builder()
        .returnBody(response)
        .message(String.format(SuccessMessages.MOVIE_WITH_SLUG_FOUND, cinemaSlug))
        .httpStatus(HttpStatus.OK)
        .build();
  }

  //M03
  public ResponseMessage<Page<MovieResponse>> findMoviesByHallName(String hallName, int page, int size, String sort, String type) {

    Pageable pageable = pageableHelper.buildPageable(page,size,sort,type);
    Page<Movie> movies = movieRepository.findAllByHallIgnoreCase(hallName,pageable);
    Page<MovieResponse> response= movieMapper.mapToResponsePage(movies);

    if(response.isEmpty()){
      return ResponseMessage.<Page<MovieResponse>>builder()
              .message(String.format(ErrorMessages.MOVIES_NOT_FOUND,hallName))
              .httpStatus(HttpStatus.NOT_FOUND)
              .build();
    }
    return ResponseMessage.<Page<MovieResponse>>builder()
            .returnBody(response)
            .message(String.format(SuccessMessages.MOVIE_FOUND,hallName))
            .httpStatus(HttpStatus.OK)
            .build();

  }


  //a Reusable Method to find a Movie by id. If it doesn't exist, throws exception
  private Movie findMovieById(Long id) {
    return movieRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(
            ErrorMessages.MOVIE_NOT_FOUND_BY_ID, id)));
  }

  //MO9
  public ResponseMessage<MovieResponse> getMovieById(Long id) {
    Movie movie = findMovieById(id);
    return ResponseMessage.<MovieResponse>builder()
        .httpStatus(HttpStatus.OK)
        .message(String.format(SuccessMessages.MOVIE_WITH_ID_FOUND, id))
        .returnBody(movieMapper.mapMovieToMovieResponse(movie))
        .build();
  }


}
