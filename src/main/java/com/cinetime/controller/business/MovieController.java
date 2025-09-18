package com.cinetime.controller.business;

import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


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
  @PreAuthorize("permitAll()")
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

  //M02
  @Operation(
      summary = "Get Movies by Cinema Slug",
      description = "Returns a paginated list of movies showing at a specific cinema identified by its slug"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
      @ApiResponse(responseCode = "404", description = "Cinema not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PreAuthorize("permitAll()")
  @GetMapping("/slug/{cinemaSlug}")
  public ResponseMessage<Page<CinemaMovieResponse>> findMoviesByCinemaSlug(
      @Parameter(description = "Cinema slug", required = true)
      @PathVariable String cinemaSlug,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @RequestParam(value = "sort", defaultValue = "title") String sort,
      @RequestParam(value = "type", defaultValue = "asc") String type) {
    return movieService.findMoviesByCinemaSlug(cinemaSlug, page, size, sort, type);
  }

  //M09
  @Operation(
      summary = "Get Movie By Id {M09}",
      description = "Returns the details of a movie by its ID. If the movie does not exist, a 404 error is returned."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the movie"),
      @ApiResponse(responseCode = "400", description = "Required type for ID (Long) not provided"),
      @ApiResponse(responseCode = "404", description = "Movie not found with the given ID"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public ResponseMessage<MovieResponse> getMovie(
      @PathVariable Long id) {
    return movieService.getMovieById(id);
  }

}
