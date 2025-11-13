package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Movie;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.mappers.HallMapper;
import com.cinetime.payload.mappers.MovieMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.CinemaCreateRequest;
import com.cinetime.payload.response.business.*;
import com.cinetime.repository.business.*;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the CinemaService class.
 * Uses Mockito to isolate business logic from repository or mapping layers.
 * Verifies correctness of behavior, responses, and exceptions.
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CinemaServiceTest {

    // --- Mocked dependencies ---
    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaMapper cinemaMapper;
    @Mock private CinemasHelper cinemasHelper;
    @Mock private UserRepository userRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private HallRepository hallRepository;
    @Mock private HallMapper hallMapper;
    @Mock private CityRepository cityRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private MovieMapper movieMapper;
    @Mock private MovieRepository movieRepository;

    // --- System under test ---
    @InjectMocks
    private CinemaService cinemaService;

    private Cinema cinema;
    private City city;

    @BeforeEach
    void setUp() {
        city = City.builder().id(1L).name("Istanbul").build();
        city = City.builder().id(1L).name("Istanbul").build();

        cinema = Cinema.builder()
                .id(10L)
                .name("CineTime Mall")
                .slug("cinetime-mall")
                .city(city)
                .movies(new LinkedHashSet<>())
                .halls(new LinkedHashSet<>())
                .favorites(new LinkedHashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ===========================================================
    // Basic CRUD-like service methods
    // ===========================================================

    @Test
    void getCinemaById_ShouldReturnCinema_WhenExists() {
        // Arrange
        when(cinemaRepository.findById(10L)).thenReturn(Optional.of(cinema));
        CinemaSummaryResponse mapped =
                CinemaSummaryResponse.builder().id(10L).name("CineTime Mall").build();
        when(cinemaMapper.toSummary(cinema)).thenReturn(mapped);

        // Act
        var response = cinemaService.getCinemaById(10L);

        // Assert
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getReturnBody().getId()).isEqualTo(10L);
        verify(cinemaRepository).findById(10L);
    }

    @Test
    void getCinemaById_ShouldThrowException_WhenNotFound() {
        when(cinemaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cinemaService.getCinemaById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorMessages.CINEMA_NOT_FOUND);
    }

    @Test
    void createCinema_ShouldCreateSuccessfully() {
        // Arrange
        CinemaCreateRequest request = new CinemaCreateRequest();
        request.setName("Test Cinema");
        request.setCityId(1L);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(cinemasHelper.slugify("Test Cinema")).thenReturn("test-cinema");
        when(cinemasHelper.ensureUniqueSlug("test-cinema")).thenReturn("test-cinema");
        when(cinemaRepository.save(any(Cinema.class))).thenReturn(cinema);
        when(cinemaMapper.toSummary(cinema))
                .thenReturn(CinemaSummaryResponse.builder().id(10L).name("CineTime Mall").build());

        // Act
        var result = cinemaService.createCinema(request);

        // Assert
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.CINEMA_CREATED);
        verify(cinemaRepository).save(any(Cinema.class));
    }

    @Test
    void createCinema_ShouldThrow_WhenCityNotFound() {
        CinemaCreateRequest request = new CinemaCreateRequest();
        request.setName("Cinema X");
        request.setCityId(999L);
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.createCinema(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorMessages.CITY_NOT_FOUND);
    }

    @Test
    void delete_ShouldDeleteCinema_WhenExists() {
        when(cinemaRepository.findById(10L)).thenReturn(Optional.of(cinema));

        var response = cinemaService.deleteMultiple(List.of(10L));

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
        // service formats the message with ids.size() → 1
        assertThat(response.getMessage())
                .isEqualTo(String.format(SuccessMessages.CINEMA_DELETED, 1));

        verify(cinemaRepository).delete(cinema);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(cinemaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.deleteMultiple(List.of(404L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format(ErrorMessages.CINEMA_NOT_FOUND, 404L));
    }

    @Test
    void listCinemas_ShouldReturnPage_WhenCityExists() {
        var pageable = PageRequest.of(0, 10);
        when(cityRepository.findByNameIgnoreCase("Istanbul")).thenReturn(Optional.of(city));
        when(cinemaRepository.search(eq(1L), anyBoolean(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(cinema)));
        when(cinemaMapper.toSummary(cinema))
                .thenReturn(CinemaSummaryResponse.builder().id(10L).name("CineTime Mall").build());

        var response = cinemaService.listCinemas(1L, "Istanbul", false, pageable);

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getReturnBody().getContent()).hasSize(1);
        verify(cinemaRepository).search(1L, false, pageable);
    }

    @Test
    void getAllSpecialHalls_ShouldReturnList() {
        lenient().when(hallRepository.findByIsSpecialTrueOrderByNameAsc()).thenReturn(List.of());
        lenient().when(hallMapper.toSpecial(any()))
                .thenReturn(SpecialHallResponse.builder()
                        .id(1L)
                        .hallName("IMAX Hall")   // <-- not name()
                        .build());

        var result = cinemaService.getAllSpecialHalls();

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.SPECIAL_HALLS_LISTED);
    }

    // ===========================================================
    // getMoviesWithShowtimesByCinema()
    // ===========================================================

    @Test
    void getMoviesWithShowtimesByCinema_ShouldReturnMoviesGroupedById() {
        Long cinemaId = 10L;
        LocalDate today = LocalDate.now();

        // Prepare mock rows (as projection results)
        ShowtimeRepository.HallMovieTimeRow row1 = mock(ShowtimeRepository.HallMovieTimeRow.class);
        ShowtimeRepository.HallMovieTimeRow row2 = mock(ShowtimeRepository.HallMovieTimeRow.class);

        when(row1.getMovieId()).thenReturn(101L);
        when(row2.getMovieId()).thenReturn(101L);
        when(row1.getDate()).thenReturn(today);
        when(row2.getDate()).thenReturn(today.plusDays(1));
        when(row1.getStartTime()).thenReturn(LocalTime.of(14, 0));
        when(row2.getStartTime()).thenReturn(LocalTime.of(16, 30));

        List<ShowtimeRepository.HallMovieTimeRow> rows = List.of(row1, row2);
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(rows);

        Movie movie = Movie.builder().id(101L).title("Interstellar").build();
        when(movieRepository.findById(101L)).thenReturn(Optional.of(movie));
        when(movieMapper.mapMovieToCinemaMovieResponse(movie))
                .thenReturn(CinemaMovieResponse.builder().id(101L).title("Interstellar").build());

        // Act
        var result = cinemaService.getMoviesWithShowtimesByCinema(cinemaId, today);

        // Assert
        assertThat(result).hasSize(1);
        var entry = result.get(0); // get first element
        assertThat(entry.getMovie().getTitle()).isEqualTo("Interstellar");
        assertThat(entry.getShowtimes()).hasSize(2);
        verify(movieRepository, times(1)).findById(101L);
    }

    @Test
    void getMoviesWithShowtimesByCinema_ShouldThrow_WhenMovieMissing() {
        Long cinemaId = 10L;

        // --- Arrange (Given) ---
        ShowtimeRepository.HallMovieTimeRow row = mock(ShowtimeRepository.HallMovieTimeRow.class);

        lenient().when(row.getMovieId()).thenReturn(999L);
        lenient().when(row.getDate()).thenReturn(LocalDate.now());
        lenient().when(row.getStartTime()).thenReturn(LocalTime.NOON);
        lenient().when(showtimeRepository.findShowtimesByCinemaId(cinemaId))
                .thenReturn(List.of(row));
        lenient().when(movieRepository.findById(999L))
                .thenReturn(Optional.empty());

        // --- Act & Assert ---
        assertThatThrownBy(() -> cinemaService.getMoviesWithShowtimesByCinema(cinemaId, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found");
    }


    // ===========================================================
    // getCinemaHallsWithShowtimes()
    // ===========================================================

    @Test
    void getCinemaHallsWithShowtimes_ShouldGroupShowtimesByHallAndMovie() {
        Long cinemaId = 10L;
        when(cinemaRepository.existsById(cinemaId)).thenReturn(true);

        // Mock projection row with hall and movie info
        ShowtimeRepository.HallMovieTimeRow row = mock(ShowtimeRepository.HallMovieTimeRow.class);
        when(row.getHallId()).thenReturn(5L);
        when(row.getHallName()).thenReturn("Main Hall");
        when(row.getSeatCapacity()).thenReturn(120);
        when(row.getIsSpecial()).thenReturn(false);
        when(row.getMovieId()).thenReturn(77L);
        when(row.getMovieTitle()).thenReturn("Inception");
        when(row.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(row.getStartTime()).thenReturn(LocalTime.of(13, 0));

        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(List.of(row));

        // Act
        var halls = cinemaService.getCinemaHallsWithShowtimes(cinemaId);

        // Assert
        assertThat(halls).hasSize(1);
        var hall = halls.get(0);
        assertThat(hall.getName()).isEqualTo("Main Hall");
        assertThat(hall.getMovies()).hasSize(1);
        assertThat(hall.getMovies().get(0).getMovie().getTitle()).isEqualTo("Inception");
        assertThat(hall.getMovies().get(0).getTimes()).hasSize(1);
    }

    @Test
    void getCinemaHallsWithShowtimes_ShouldThrow_WhenCinemaDoesNotExist() {
        when(cinemaRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> cinemaService.getCinemaHallsWithShowtimes(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format(ErrorMessages.CINEMA_NOT_FOUND, 999L));

    }
}
