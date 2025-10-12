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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    @PreAuthorize("permitAll()")
    @GetMapping("/by-cinema-date")
    public ResponseEntity<ResponseMessage<List<MovieResponse>>> getMoviesByCinemaAndDate(
            @RequestParam Long cinemaId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate date
    ) {
        ResponseMessage<List<MovieResponse>> response = movieService.findByCinemaAndDate(cinemaId, date);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }


    //M01
    @PreAuthorize("permitAll()")
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseMessage<Page<MovieResponse>>> searchMovies(
            @RequestParam(required = false) String q,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC)
            Pageable pageable) {

        ResponseMessage<Page<MovieResponse>> response = movieService.searchMovies(q, pageable);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    //M02
    @PreAuthorize("permitAll()")
    @GetMapping("/slug/{cinemaSlug}")
    public ResponseEntity<ResponseMessage<Page<CinemaMovieResponse>>> findMoviesByCinemaSlug(
            @Parameter(description = "Cinema slug", required = true)
            @PathVariable String cinemaSlug,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC)
            Pageable pageable) {

        ResponseMessage<Page<CinemaMovieResponse>> response =
                movieService.findMoviesByCinemaSlug(cinemaSlug, pageable);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    //M03
    @PreAuthorize("permitAll()")
    @GetMapping("/hall/{hallName}")
    public ResponseEntity<ResponseMessage<Page<MovieResponse>>> findMoviesByHallName(
            @Parameter(description = "Hall name (e.g., imax, vip, goldclass)", required = true)
            @PathVariable String hallName,
            @PageableDefault(page = 0, size = 10, sort = "releaseDate", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        ResponseMessage<Page<MovieResponse>> response =
                movieService.findMoviesByHallName(hallName, pageable);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }


    //M04
    @PreAuthorize("permitAll()")
    @GetMapping("/in-theatres")
    public ResponseEntity<ResponseMessage<Page<MovieResponse>>> getMoviesInTheatres(
            @Parameter(description = "Optional search date (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(page = 0, size = 10, sort = "releaseDate", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(date, pageable);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }


    //M05
    @PreAuthorize("permitAll()")
    @GetMapping("/coming-soon")
    public ResponseEntity<ResponseMessage<Page<MovieResponse>>> getComingSoonMovies(
            @Parameter(description = "Optional release date (yyyy-MM-dd) to filter")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(page = 0, size = 10, sort = "releaseDate", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        ResponseMessage<Page<MovieResponse>> response =
                movieService.getComingSoonMovies(date, pageable);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/status")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseMessage<Page<MovieResponse>>> fetchMoviesByStatus(
            @RequestParam(required = true) String status,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC)
            Pageable pageable) {

        ResponseMessage<Page<MovieResponse>> response = movieService.getMoviesByStatus(status, pageable);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }


    //M08
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @GetMapping("/admin")
    public ResponseEntity<ResponseMessage<Page<MovieResponse>>> searchAuthorizedMovies(
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        ResponseMessage<Page<MovieResponse>> response = movieService.searchAuthorizedMovies(q, pageable);
        return ResponseEntity.ok(response);
    }


    //M09 - Get Movie By ID
    @PreAuthorize("permitAll()")
    @GetMapping("/id/{movieId}")
    @Transactional(readOnly = true)
    public ResponseMessage<MovieResponse> getMovie(
            @PathVariable Long movieId) {
        return movieService.getMovieById(movieId);
    }

    //M11 - Save Movie
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

    //M13 - Delete Movie
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/del/{movieId}")
    public ResponseMessage<MovieResponse> deleteMovie(
            @PathVariable Long movieId) {
        return movieService.deleteMovieById(movieId);
    }

    //M15
    @PreAuthorize("permitAll()")
    @GetMapping("/genre")
    @Transactional(readOnly = true)
    public ResponseMessage<Page<MovieResponse>> getMoviesByGenre(
            @RequestParam(required = false) String genre,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        return movieService.getMovieByGenre(genre, pageable);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/genre-list")
    @Transactional(readOnly = true)
    public ResponseMessage<List<String>> getMoviesGenres() {
        return movieService.getGenres();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/filter")
    @Transactional(readOnly = true)
    public ResponseMessage<Page<MovieResponse>> filterMovies(
            @RequestParam(required = false) List<String> genre,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String releaseDate,
            @RequestParam(required = false) String specialHalls,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return movieService.filterMovies(genre, status, minRating, maxRating, releaseDate, specialHalls, pageable);
    }


}
