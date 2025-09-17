package com.cinetime.controller.business;

import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

  private final MovieService movieService;

  //M01
  @Operation(
      summary = "Search Movies {M01}",
      description = "Returns a paginated list of movies matching the search query"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  @Transactional(readOnly = true)
  public ResponseMessage<Page<MovieResponse>> searchMovies(
      @RequestParam(required = false) String q,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @RequestParam(value = "sort", defaultValue = "title") String sort,
      @RequestParam(value = "type", defaultValue = "asc") String type) {

    return movieService.searchMovies(q, page, size, sort, type);

  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public ResponseMessage<MovieResponse> getMovie(
      @PathVariable Long id){
    return movieService.getMovieById(id);
  }

}
