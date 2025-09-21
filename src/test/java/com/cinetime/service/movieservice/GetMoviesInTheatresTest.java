package com.cinetime.service.movieservice;

import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.enums.MovieStatus;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.MovieResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.MovieRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.service.business.MovieService;
import com.cinetime.service.helper.PageableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for MovieService.getMoviesInTheatres()")
class GetMoviesInTheatresTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private PageableHelper pageableHelper;

    @InjectMocks
    private MovieService movieService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10, Sort.by("releaseDate").ascending());
    }

    @Test
    @DisplayName("Should return movies in theatres when no date is provided and movies exist")
    void testNoDate_MoviesFound() {
        Movie movie = new Movie();
        movie.setId(1L);

        Page<Movie> moviePage = new PageImpl<>(List.of(movie));

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, pageable)).thenReturn(moviePage);
        when(movieMapper.mapToResponsePage(moviePage))
                .thenReturn(new PageImpl<>(List.of(MovieResponse.builder().id(1L).title("Inception").build())));

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(null, 0, 10, "releaseDate", "asc");

        assertEquals(SuccessMessages.MOVIES_FOUND_IN_THEATRES, response.getMessage());
        assertFalse(response.getReturnBody().isEmpty());
    }

    @Test
    @DisplayName("Should return error message when no date is provided and no movies exist")
    void testNoDate_NoMovies() {
        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, pageable)).thenReturn(Page.empty(pageable));

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(null, 0, 10, "releaseDate", "asc");

        assertEquals(ErrorMessages.MOVIES_NOT_IN_THEATRES, response.getMessage());
        assertTrue(response.getReturnBody().isEmpty());
    }

    @Test
    @DisplayName("Should return movies when a future date is provided and showtimes exist")
    void testFutureDate_ShowtimesFound() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Showtime showtime = new Showtime();
        showtime.setMovie(new Movie());

        Page<Showtime> showtimePage = new PageImpl<>(List.of(showtime));

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(showtimeRepository.findByDate(futureDate, pageable)).thenReturn(showtimePage);
        when(movieMapper.mapMovieToMovieResponse(any(Movie.class)))
                .thenReturn(MovieResponse.builder().id(10L).title("Dune 2").build());

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(futureDate, 0, 10, "releaseDate", "asc");

        assertTrue(response.getMessage().contains(SuccessMessages.MOVIES_FOUND_ON_DATE));
        assertFalse(response.getReturnBody().isEmpty());
    }

    @Test
    @DisplayName("Should return error message when a future date is provided and no showtimes exist")
    void testFutureDate_NoShowtimes() {
        LocalDate futureDate = LocalDate.now().plusDays(2);

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(showtimeRepository.findByDate(futureDate, pageable)).thenReturn(Page.empty(pageable));

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(futureDate, 0, 10, "releaseDate", "asc");

        assertTrue(response.getMessage().contains(ErrorMessages.MOVIES_NOT_FOUND_ON_DATE));
        assertTrue(response.getReturnBody().isEmpty());
    }

    @Test
    @DisplayName("Should return movies when today's date is provided and showtimes exist")
    void testTodayDate_ShowtimesFound() {
        LocalDate today = LocalDate.now();
        Showtime showtime = new Showtime();
        showtime.setMovie(new Movie());

        Page<Showtime> showtimePage = new PageImpl<>(List.of(showtime));

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(showtimeRepository.findByDate(today, pageable)).thenReturn(showtimePage);
        when(movieMapper.mapMovieToMovieResponse(any(Movie.class)))
                .thenReturn(MovieResponse.builder().id(20L).title("Avatar 2").build());

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(today, 0, 10, "releaseDate", "asc");

        assertTrue(response.getMessage().contains(SuccessMessages.MOVIES_FOUND_ON_DATE));
        assertFalse(response.getReturnBody().isEmpty());
    }

    @Test
    @DisplayName("Should return error message when today's date is provided but no showtimes exist")
    void testTodayDate_NoShowtimes() {
        LocalDate today = LocalDate.now();

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(showtimeRepository.findByDate(today, pageable)).thenReturn(Page.empty(pageable));

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(today, 0, 10, "releaseDate", "asc");

        assertTrue(response.getMessage().contains(ErrorMessages.MOVIES_NOT_FOUND_ON_DATE));
        assertTrue(response.getReturnBody().isEmpty());
    }

    @Test
    @DisplayName("Should fallback to today when a past date is provided")
    void testPastDate_FallbackToToday() {
        LocalDate pastDate = LocalDate.now().minusDays(5);
        Showtime showtime = new Showtime();
        showtime.setMovie(new Movie());

        Page<Showtime> showtimePage = new PageImpl<>(List.of(showtime));

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(showtimeRepository.findByDateAndStartTimeAfter(eq(LocalDate.now()), any(LocalTime.class), eq(pageable)))
                .thenReturn(showtimePage);
        when(movieMapper.mapMovieToMovieResponse(any(Movie.class)))
                .thenReturn(MovieResponse.builder().id(30L).title("Batman").build());

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(pastDate, 0, 10, "releaseDate", "asc");

        assertTrue(response.getMessage().contains(SuccessMessages.MOVIES_FOUND_ON_DATE));
        assertFalse(response.getReturnBody().isEmpty());
    }

    @Test
    @DisplayName("Should build correct pageable when custom sort and type are provided")
    void testCustomPagingAndSorting() {
        Pageable customPageable = PageRequest.of(2, 5, Sort.by("title").descending());

        when(pageableHelper.buildPageable(2, 5, "title", "desc")).thenReturn(customPageable);
        when(movieRepository.findByStatus(MovieStatus.IN_THEATERS, customPageable)).thenReturn(Page.empty(customPageable));

        movieService.getMoviesInTheatres(null, 2, 5, "title", "desc");

        verify(pageableHelper).buildPageable(2, 5, "title", "desc");
        verify(movieRepository).findByStatus(MovieStatus.IN_THEATERS, customPageable);
    }

    @Test
    @DisplayName("Mapper should return null when a null Movie is provided")
    void testMovieMapper_NullMovie() {
        assertNull(movieMapper.mapMovieToMovieResponse(null));
    }

    @Test
    @DisplayName("Should return duplicate MovieResponse objects if multiple showtimes exist for the same movie")
    void testDuplicateShowtimes() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        Movie movie = new Movie();
        movie.setId(99L);

        Showtime showtime1 = new Showtime();
        showtime1.setMovie(movie);
        Showtime showtime2 = new Showtime();
        showtime2.setMovie(movie);

        Page<Showtime> showtimePage = new PageImpl<>(List.of(showtime1, showtime2));

        when(pageableHelper.buildPageable(0, 10, "releaseDate", "asc")).thenReturn(pageable);
        when(showtimeRepository.findByDate(futureDate, pageable)).thenReturn(showtimePage);
        when(movieMapper.mapMovieToMovieResponse(movie))
                .thenReturn(MovieResponse.builder().id(99L).title("Matrix").build());

        ResponseMessage<Page<MovieResponse>> response =
                movieService.getMoviesInTheatres(futureDate, 0, 10, "releaseDate", "asc");

        assertEquals(2, response.getReturnBody().getContent().size());
    }
}
