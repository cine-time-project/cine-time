package com.cinetime.service.business;

import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
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
        .message(SuccessMessages.MOVIES_FOUND)
        .httpStatus(HttpStatus.OK)
        .returnBody(movieMapper.mapToResponsePage(movies))
        .build();

  }

  //Method to find a Movie by Id. If it doesn't exists, throws exception
  private Movie findMovieById(Long id) {
    return movieRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(
            ErrorMessages.NOT_FOUND_MOVIE_BY_ID, id)));
  }

  public ResponseMessage<MovieResponse> getMovieById(Long id) {
    Movie movie = findMovieById(id);
    return ResponseMessage.<MovieResponse>builder()
        .httpStatus(HttpStatus.OK)
        .message(String.format(SuccessMessages.MOVIE_FOUND_BY_ID, id))
        .returnBody(movieMapper.mapMovieToMovieResponse(movie))
        .build();
  }
}
