package com.cinetime.controller.business;

import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


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
    @GetMapping("/search")
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
            @ApiResponse(responseCode = "404", description = "No movies found for given cinema slug"),
            @ApiResponse(responseCode = "400", description = "Invalid cinema slug"), // optional
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

    //M03
    @Operation(
            summary = "Get Movies by Hall {M03}",
            description = "Returns a paginated list of movies showing in a specific hall (e.g., imax, vip, goldclass)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "404", description = "No movies found for the given hall"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("permitAll()")
    @GetMapping("/hall/{hallName}")
    public ResponseMessage<Page<MovieResponse>> findMoviesByHallName(
            @PathVariable String hallName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "releaseDate") String sort,
            @RequestParam(value = "type", defaultValue = "asc") String type
    ) {
        return movieService.findMoviesByHallName(hallName, page, size, sort, type);
    }

    //M04
    @Operation(
            summary = "Get Movies In Theatres",
            description = "Returns a paginated list of movies currently in theatres or for a specific date if provided"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("permitAll()")
    @GetMapping("/in-theatres")
    public ResponseMessage<Page<MovieResponse>> getMoviesInTheatres(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "asc") String type) {
        return movieService.getMoviesInTheatres(date, page, size, sort, type);
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
    @PreAuthorize("permitAll()")
    @GetMapping("/id/{movieId}")
    @Transactional(readOnly = true)
    public ResponseMessage<MovieResponse> getMovie(
            @PathVariable Long movieId) {
        return movieService.getMovieById(movieId);
    }

    //M11
    @Operation(
            summary = "Save Movie {M11}",
            description = "Creates and saves a new movie with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new movie"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Forbidden access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/save")
    public ResponseMessage<MovieResponse> saveMovie(
            @RequestBody @Valid MovieRequest movieRequest
    ) {
        return movieService.saveMovie(movieRequest);
    }

    // M12 - Update Movie
    @Operation(
            summary = "Update Movie {M12}",
            description = "Updates an existing movie. Handles primitive fields, ElementCollection (cast, formats, genre), " +
                    "ManyToMany (cinemas), and OneToMany (images). Null-safe updates."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movie successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "404", description = "Movie not found with the provided ID"),
            @ApiResponse(responseCode = "403", description = "Forbidden access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/update/{movieId}")
    public ResponseMessage<MovieResponse> updateMovie(
            @Parameter(description = "DTO containing updated movie information", required = true)
            @RequestBody @Valid MovieRequest movieRequest,
            @Parameter(description = "ID of the movie to be updated", required = true)
            @PathVariable Long movieId
    ) {
        return movieService.updateMovie(movieRequest, movieId);
    }



}
