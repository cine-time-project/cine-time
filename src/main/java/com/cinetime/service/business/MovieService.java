package com.cinetime.service.business;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.exception.BadRequestException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.MovieRequest;
import com.cinetime.payload.response.business.CinemaMovieResponse;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.service.helper.MovieServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final int MAX_LENGTH_FOR_SLUG = 50;

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final CinemaService cinemaService;
    private final ImageService imageService;
    private final MovieServiceHelper movieServiceHelper;

    /**
     * Retrieves all movies for a given cinema on a specific date.
     *
     * @param cinemaId ID of the cinema
     * @param date     Date to filter movies
     * @return ResponseMessage with list of MovieResponse
     */
    @Transactional(readOnly = true)
    public ResponseMessage<List<MovieResponse>> findByCinemaAndDate(Long cinemaId, LocalDate date) {
        List<Movie> movies = movieRepository.findByCinemaAndDate(cinemaId, date);
        List<MovieResponse> movieResponses = movies.stream()
                .map(movieMapper::mapMovieToMovieResponse).toList();
        return ResponseMessage.<List<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Movies listed")
                .returnBody(movieResponses)
                .build();
    }


    /**
     * M01 - Search movies by title or summary (paginated).
     * If search keyword is empty, returns all movies paginated.
     *
     * @param q        search keyword (optional)
     * @param pageable pagination info
     * @return ResponseMessage containing a page of MovieResponse
     */
    public ResponseMessage<Page<MovieResponse>> searchMovies(String q, Pageable pageable) {
        Page<Movie> movies;
        if (q != null && !q.trim().isEmpty()) {
            String keyword = q.trim();
            movies = movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
                    keyword, keyword, pageable);
        } else {
            movies = movieRepository.findAll(pageable);
        }
        if (movies.isEmpty()) {
            return movieServiceHelper.buildEmptyPageResponse(
                    pageable, ErrorMessages.MOVIES_NOT_FOUND, HttpStatus.OK);
        }
        return ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.MOVIES_LISTED)
                .returnBody(movieMapper.mapToResponsePage(movies))
                .build();
    }

    /**
     * M02 - Fetch movies for a specific cinema identified by its slug.
     *
     * @param cinemaSlug slug of the cinema
     * @param pageable   pagination info
     * @return ResponseMessage with a page of CinemaMovieResponse
     */
    public ResponseMessage<Page<CinemaMovieResponse>> findMoviesByCinemaSlug(
            String cinemaSlug, Pageable pageable) {

        if (cinemaSlug == null || cinemaSlug.trim().isEmpty()) {
            throw new IllegalArgumentException("Cinema slug cannot be null or empty");
        }
        Page<Movie> movies = movieRepository.findAllByCinemaSlugIgnoreCase(cinemaSlug, pageable);
        if (movies.isEmpty()) {
            return movieServiceHelper.buildEmptyPageResponse(
                    pageable, ErrorMessages.MOVIES_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        Page<CinemaMovieResponse> responsePage =
                movies.map(movieMapper::mapMovieToCinemaMovieResponse);
        return ResponseMessage.<Page<CinemaMovieResponse>>builder()
                .returnBody(responsePage)
                .message(String.format(SuccessMessages.MOVIE_WITH_SLUG_FOUND, cinemaSlug))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    /**
     * M03 - Fetch movies that are showing in a specific hall.
     *
     * @param hallName hall name
     * @param pageable pagination info
     * @return ResponseMessage with a page of MovieResponse
     */
    public ResponseMessage<Page<MovieResponse>> findMoviesByHallName(String hallName, Pageable pageable) {
        if (hallName == null || hallName.trim().isEmpty()) {
            throw new IllegalArgumentException("Hall name cannot be null or empty");
        }
        Page<Movie> movies = movieRepository.findAllByHallIgnoreCase(hallName, pageable);
        if (movies.isEmpty()) {
            return movieServiceHelper.buildEmptyPageResponse(
                    pageable, ErrorMessages.MOVIES_NOT_FOUND_IN_HALL, HttpStatus.NOT_FOUND);
        }
        Page<MovieResponse> responsePage = movies.map(movieMapper::mapMovieToMovieResponse);
        return ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(String.format(SuccessMessages.MOVIES_FOUND_IN_HALL, hallName))
                .returnBody(responsePage)
                .build();
    }


    /**
     * M04 - Get movies currently in theatres.
     * Can filter by date or get all currently running movies if date is null.
     *
     * @param date     optional date filter
     * @param pageable pagination info
     * @return ResponseMessage with a page of MovieResponse
     */
    public ResponseMessage<Page<MovieResponse>> getMoviesInTheaters(LocalDate date, Pageable pageable) {
        if (date != null) {
            return movieServiceHelper.getMoviesByDate(date, pageable);
        }
        return movieServiceHelper.getCurrentlyInTheatres(pageable);
    }


    //M05 - Get movies coming soon.

    @Transactional(readOnly = true)
    public ResponseMessage<Page<MovieResponse>> getComingSoonMovies(Pageable pageable) {
        // Pageable-only çağrılar buraya gelecek
        return getComingSoonMovies(null, pageable);
    }

    @Transactional(readOnly = true)
    public ResponseMessage<Page<MovieResponse>> getComingSoonMovies(LocalDate from, Pageable pageable) {
        Page<Movie> page = (from == null)
                ? movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable)
                : movieRepository.findByStatusAndReleaseDateGreaterThanEqual(MovieStatus.COMING_SOON, from, pageable);

        Page<MovieResponse> body = movieMapper.mapToResponsePage(page);

        return ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(page.isEmpty() ? "No movies" : "Movies found")
                .returnBody(body)
                .build();
    }


    /**
     * M08 - Search movies (authorized).
     * Works similar to searchMovies but may have additional security/authorization in future.
     *
     * @param q        search keyword
     * @param pageable pagination info
     * @return ResponseMessage with a page of MovieResponse
     */
    public ResponseMessage<Page<MovieResponse>> searchAuthorizedMovies(String q, Pageable pageable) {
        Page<Movie> movies;
        if (q != null && !q.trim().isEmpty()) {
            String keyword = q.trim();
            movies = movieRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
                    keyword, keyword, pageable);
        } else {
            movies = movieRepository.findAll(pageable);
        }
        if (movies.isEmpty()) {
            return movieServiceHelper.buildEmptyPageResponse(
                    pageable, ErrorMessages.MOVIES_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.MOVIES_LISTED)
                .returnBody(movieMapper.mapToResponsePage(movies))
                .build();
    }


    /**
     * Find a movie by its ID.
     * Throws ResourceNotFoundException if not found.
     *
     * @param id movie ID
     * @return Movie entity
     */
    public Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                        ErrorMessages.MOVIE_NOT_FOUND_BY_ID, id)));
    }

    /**
     * M09 - Get a movie by its ID.
     *
     * @param id movie ID
     * @return ResponseMessage containing the movie
     */
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

    /**
     * M11 - Save Movie
     * <p>
     * Creates and persists a new {@link Movie} entity based on the provided request.
     * Handles:
     * <ul>
     *   <li>Mapping request data to entity</li>
     *   <li>Generating and ensuring a unique slug</li>
     *   <li>Setting related cinemas and images if provided</li>
     * </ul>
     *
     * @param movieRequest DTO with movie creation data
     * @return ResponseMessage containing the created MovieResponse
     */
    @Transactional
    public ResponseMessage<MovieResponse> saveMovie(MovieRequest movieRequest) {
        Movie movie = movieMapper.mapMovieRequestToMovie(movieRequest);

        // Generate unique slug
        String uniqueSlug = movieServiceHelper.generateUniqueSlug(movieRequest.getTitle(), movieRequest.getSlug(), MAX_LENGTH_FOR_SLUG, null);
        movie.setSlug(uniqueSlug);

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
     * Updates an existing movie entity with data from the provided request.
     * <p>
     * Handles:
     * <ul>
     *   <li>Primitive and collection fields (title, summary, releaseDate, etc.)</li>
     *   <li>Slug regeneration and uniqueness check</li>
     *   <li>Many-to-Many cinemas relation (replace if provided)</li>
     *   <li>One-to-Many images relation (replace if provided)</li>
     * </ul>
     * Null handling:
     * <ul>
     *   <li>If cinemaIds or imageIds are null → keep existing relations</li>
     *   <li>If empty set → clear relations</li>
     * </ul>
     *
     * @param movieRequest DTO containing updated movie information
     * @param movieId      ID of the movie to update
     * @return ResponseMessage with the updated movie response
     */
    @Transactional
    public ResponseMessage<MovieResponse> updateMovie(MovieRequest movieRequest, Long movieId) {
        // 1️⃣ Retrieve the existing movie entity from the DB
        Movie movie = findMovieById(movieId);

        // 2️⃣ Update primitive and ElementCollection fields via the mapper
        // (title, slug, summary, releaseDate, duration, rating, specialHalls, director, cast, formats, genre, status)

        // Regenerate slug if changed
        if (!movie.getSlug().equals(movieRequest.getSlug())) {
            String uniqueSlug = movieServiceHelper.generateUniqueSlug(movieRequest.getTitle(), movieRequest.getSlug(), MAX_LENGTH_FOR_SLUG, movieId);
            movieRequest.setSlug(uniqueSlug);
        } else if (!movie.getTitle().equals(movieRequest.getTitle())) {
            //if Slugs are equal but the title has been changed, sending null Slug in order to produce a new slug from title.
            String uniqueSlug = movieServiceHelper.generateUniqueSlug(movieRequest.getTitle(), null, MAX_LENGTH_FOR_SLUG, movieId);
            movieRequest.setSlug(uniqueSlug);
        }
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

    /**
     * Fetches movies filtered by genre with pagination and sorting.
     * - If genre is provided, performs a case-insensitive contains search.
     * - If genre is null/blank, returns all movies.
     * - Throws ResourceNotFoundException if no movies are found.
     *
     * @param genre    optional genre filter
     * @param pageable pagination and sorting info
     * @return paginated list of MovieResponse wrapped in ResponseMessage
     */
    public ResponseMessage<Page<MovieResponse>> getMovieByGenre(String genre, Pageable pageable) {
        Page<Movie> movies;
        if (genre != null && !genre.trim().isEmpty()) {
            movies = movieRepository.findAllByGenreIgnoreCaseContaining(genre.trim(), pageable);
        } else {
            movies = movieRepository.findAll(pageable);
        }
        if (movies.isEmpty()) throw new ResourceNotFoundException(ErrorMessages.MOVIES_NOT_FOUND);
        return ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.MOVIES_FOUND)
                .returnBody(movieMapper.mapToResponsePage(movies))
                .build();
    }

    /**
     * Fetch movies by their status (IN_THEATERS, COMING_SOON, PRESALE).
     * Throws BadRequestException for invalid status.
     *
     * @param status   status as String
     * @param pageable pagination info
     * @return ResponseMessage with a page of MovieResponse
     */
    @Transactional
    public ResponseMessage<Page<MovieResponse>> getMoviesByStatus(String status, Pageable pageable) {
        MovieStatus movieStatus;

        if (status.equalsIgnoreCase(MovieStatus.IN_THEATERS.toString())) {
            movieStatus = MovieStatus.IN_THEATERS;
        } else if (status.equalsIgnoreCase(MovieStatus.COMING_SOON.toString())) {
            movieStatus = MovieStatus.COMING_SOON;
        } else if (status.equalsIgnoreCase(MovieStatus.PRESALE.toString())) {
            movieStatus = MovieStatus.PRESALE;
        } else {
            throw new BadRequestException(String.format(ErrorMessages.INVALID_STATUS, status));
        }

        Page<Movie> movies = movieRepository.findByStatus(movieStatus, pageable);

        return ResponseMessage.<Page<MovieResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message((movies.isEmpty()) ? ErrorMessages.MOVIES_NOT_FOUND : SuccessMessages.MOVIES_FOUND)
                .returnBody((movies.isEmpty()) ? Page.empty() : movieMapper.mapToResponsePage(movies))
                .build();
    }


}
