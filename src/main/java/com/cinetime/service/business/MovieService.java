package com.cinetime.service.business;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final PageableHelper pageableHelper;
    private final MovieMapper movieMapper;
    private final CinemaService cinemaService;
    private final ImageService imageService;
    private final ShowtimeRepository showtimeRepository;


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
                .mapToCinemaResponsePage(movieRepository.findAllByCinemaSlugIgnoreCase(cinemaSlug, pageable));
        if (response.isEmpty()) {
            return ResponseMessage.<Page<CinemaMovieResponse>>builder()
                    .message(ErrorMessages.MOVIES_NOT_FOUND)
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
    public ResponseMessage<Page<MovieResponse>> findMoviesByHallName(
            String hallName, int page, int size, String sort, String type) {

        if (hallName == null || hallName.trim().isEmpty()) {
            throw new IllegalArgumentException("Hall name cannot be null or empty");
        }
        Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);
        Page<Movie> movies = movieRepository.findAllByHallIgnoreCase(hallName, pageable);
        Page<MovieResponse> response = movieMapper.mapToResponsePage(movies);
        if (response == null || response.isEmpty()) {
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(ErrorMessages.MOVIES_NOT_FOUND)
                    .build();
        }
        return ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.MOVIE_FOUND)
                .returnBody(response)
                .build();
    }

    //M04
    public ResponseMessage<Page<MovieResponse>> getMoviesInTheatres(
            LocalDate date, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);
        if (date != null) {
            Page<Showtime> moveisInShow;
            if (date.isBefore(LocalDate.now())) {
                // Past date → fallback to today and only upcoming showtimes
                moveisInShow = showtimeRepository.findByDateAndStartTimeAfter(LocalDate.now(), LocalTime.now(), pageable);
            } else {
                // Today or future date → just use the provided date
                moveisInShow = showtimeRepository.findByDate(date, pageable);
            }
            if (moveisInShow.isEmpty()) {
                return ResponseMessage.<Page<MovieResponse>>builder()
                        .message(ErrorMessages.MOVIES_NOT_FOUND_ON_DATE + " " + date)
                        .httpStatus(HttpStatus.OK)
                        .returnBody(Page.empty(pageable))
                        .build();
            }
            Page<MovieResponse> movies = moveisInShow.map(s -> movieMapper.mapMovieToMovieResponse(s.getMovie()));
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .message(SuccessMessages.MOVIES_FOUND_ON_DATE + " " + date)
                    .httpStatus(HttpStatus.OK)
                    .returnBody(movies)
                    .build();
        }
        Page<Movie> movies = movieRepository.findByStatus(MovieStatus.IN_THEATERS, pageable);
        if (movies.isEmpty()) {
            return ResponseMessage.<Page<MovieResponse>>builder()
                    .message(ErrorMessages.MOVIES_NOT_IN_THEATRES)
                    .httpStatus(HttpStatus.OK)
                    .returnBody(Page.empty(pageable))
                    .build();
        }
        return ResponseMessage.<Page<MovieResponse>>builder()
                .message(SuccessMessages.MOVIES_FOUND_IN_THEATRES)
                .httpStatus(HttpStatus.OK)
                .returnBody(movieMapper.mapToResponsePage(movies))
                .build();
    }


    //Reusable Method to find a Movie by id. If it doesn't exist, throws exception
    private Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                        ErrorMessages.MOVIE_NOT_FOUND_BY_ID, id)));
    }

    //MO9
    @Transactional(readOnly = true)
    public ResponseMessage<MovieResponse> getMovieById(Long id) {
        Movie movie = findMovieById(id);
        return ResponseMessage.<MovieResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(String.format(SuccessMessages.MOVIE_WITH_ID_FOUND, id))
                .returnBody(movieMapper.mapMovieToMovieResponse(movie))
                .build();
    }

    //M11
    public ResponseMessage<MovieResponse> saveMovie(MovieRequest movieRequest) {
        Movie movie = movieMapper.mapMovieRequestToMovie(movieRequest);
        if (movieRequest.getCinemaIds() != null && !movieRequest.getCinemaIds().isEmpty()) {
            movie.setCinemas(cinemaService.getAllByIdIn(movieRequest.getCinemaIds()));
        }
        if (movieRequest.getImageIds() != null && !movieRequest.getImageIds().isEmpty()) {
            movie.setImages(imageService.getAllByIdIn(movieRequest.getImageIds()));
        }
        Movie savedMovie = movieRepository.save(movie);
        return ResponseMessage.<MovieResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message(SuccessMessages.MOVIE_CREATE)
                .returnBody(movieMapper.mapMovieToMovieResponse(savedMovie))
                .build();
    }

    //M12

    /**
     * M12 - Update Movie
     * This service method updates an existing movie entity.
     * It handles:
     * - Primitive fields (title, slug, summary, releaseDate, duration, rating, specialHalls, director)
     * - ElementCollection fields (cast, formats, genre)
     * - ManyToMany relationship (cinemas)
     * - OneToMany relationship (images)
     * Null-safe behavior:
     * - If movieRequest.getCinemaIds() or movieRequest.getImageIds() is null, the existing relationships are preserved.
     * - If an empty Set is provided, the relationships are completely removed.
     *
     * @param movieRequest DTO containing updated movie information
     * @param movieId      ID of the movie to be updated
     * @return ResponseMessage containing the updated MovieResponse
     */
    @Transactional
    public ResponseMessage<MovieResponse> updateMovie(MovieRequest movieRequest, Long movieId) {
        // 1️⃣ Retrieve the existing movie entity from the DB
        Movie movie = findMovieById(movieId);

        // 2️⃣ Update primitive and ElementCollection fields via the mapper
        // (title, slug, summary, releaseDate, duration, rating, specialHalls, director, cast, formats, genre, status)
        movieMapper.updateMovieFromRequest(movieRequest, movie);

        // 3️⃣ Update cinemas (ManyToMany)
        // Null check: if null, existing collection remains unchanged
        // Empty Set: all relations removed
        if (movieRequest.getCinemaIds() != null) {
            movie.setCinemas(cinemaService.getAllByIdIn(movieRequest.getCinemaIds()));
        }

        // 4️⃣ Update images (OneToMany)
        if (movieRequest.getImageIds() != null) {
            movie.setImages(imageService.getAllByIdIn(movieRequest.getImageIds()));
        }

        // 5️⃣ Save the updated entity to the DB
        Movie updatedMovie = movieRepository.save(movie);

        // 6️⃣ Build and return the response
        return ResponseMessage.<MovieResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.MOVIE_UPDATE)
                .returnBody(movieMapper.mapMovieToMovieResponse(updatedMovie))
                .build();
    }

    /**
     * Deletes a movie by its ID.
     * <p>
     * - If the movie does not exist, throws ResourceNotFoundException.
     * - All related data (images, element collections, join table records) are
     * automatically removed due to JPA cascade/orphanRemoval configuration.
     * - Returns the deleted movie details in the response body.
     *
     * @param movieId ID of the movie to delete
     * @return ResponseMessage containing details of the deleted movie
     */
    @Transactional
    public ResponseMessage<MovieResponse> deleteMovieById(Long movieId) {
        // 1️⃣ Find movie or throw
        Movie movie = findMovieById(movieId);

        // 2️⃣ Map before delete (to avoid detached entity / lazy issues)
        MovieResponse deletedMovieResponse = movieMapper.mapMovieToMovieResponse(movie);

        // 3️⃣ Delete from DB
        movieRepository.delete(movie);

        // 4️⃣ Build response
        return ResponseMessage.<MovieResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.MOVIE_DELETE)
                .returnBody(deletedMovieResponse)
                .build();
    }

}
