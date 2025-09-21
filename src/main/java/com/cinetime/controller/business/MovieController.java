package com.cinetime.controller.business;

import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.MovieService;
import io.swagger.v3.oas.annotations.Parameter;
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
    @PreAuthorize("permitAll()")
    @GetMapping("/id/{movieId}")
    @Transactional(readOnly = true)
    public ResponseMessage<MovieResponse> getMovie(
            @PathVariable Long movieId) {
        return movieService.getMovieById(movieId);
    }

    //M11
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/save")
    public ResponseMessage<MovieResponse> saveMovie(
            @RequestBody @Valid MovieRequest movieRequest
    ) {
        return movieService.saveMovie(movieRequest);
    }

    // M12 - Update Movie
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

    //M09
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/del/{movieId}")
    public ResponseMessage<MovieResponse> deleteMovie(
            @PathVariable Long movieId) {
        return movieService.deleteMovieById(movieId);
    }


}
