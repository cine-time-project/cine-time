package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CityMapper;
import com.cinetime.payload.mappers.ShowtimeMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.HallMovieShowtimesResponse;
import com.cinetime.payload.response.business.HallWithShowtimesResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.business.ShowtimeResponse;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.business.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;
    @Mock
    private HallService hallService;
    @Mock
    private MovieService movieService;
    @Mock
    private ShowtimeMapper showtimeMapper;
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private CityMapper cityMapper; // required for constructor
    @Mock
    private TicketRepository ticketRepository; // required for constructor + deleteShowtime

    @InjectMocks
    private ShowtimeService showtimeService;

    private ShowtimeRequest request;
    private Showtime showtime;
    private ShowtimeResponse response;
    private Hall hall;
    private Movie movie;
    private Cinema cinema;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        hall = new Hall();
        hall.setId(1L);
        hall.setName("IMAX");

        movie = new Movie();
        movie.setId(10L);
        movie.setTitle("Inception");

        cinema = new Cinema();
        cinema.setId(1L);

        request = ShowtimeRequest.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .hallId(1L)
                .movieId(10L)
                .build();

        showtime = Showtime.builder()
                .id(100L)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .hall(hall)
                .movie(movie)
                .build();

        response = ShowtimeResponse.builder()
                .id(100L)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .hallId(1L)
                .hallName("IMAX")
                .movieId(10L)
                .movieTitle("Inception")
                .build();
    }

    // ======================== CRUD TESTS ========================

    @Test
    void saveShowtime_Success() {
        when(hallService.findHallById(1L)).thenReturn(hall);
        when(movieService.findMovieById(10L)).thenReturn(movie);
        when(showtimeMapper.mapRequestToShowtime(request, hall, movie)).thenReturn(showtime);
        when(showtimeRepository.save(showtime)).thenReturn(showtime);
        when(showtimeMapper.mapShowtimeToResponse(showtime)).thenReturn(response);

        ResponseMessage<ShowtimeResponse> result = showtimeService.saveShowtime(request);

        assertEquals(HttpStatus.CREATED, result.getHttpStatus());
        assertEquals(SuccessMessages.SHOWTIME_CREATED, result.getMessage());
        assertEquals(response, result.getReturnBody());
    }

    @Test
    void findShowtimeById_Found() {
        when(showtimeRepository.findWithRefsById(100L)).thenReturn(Optional.of(showtime));

        Showtime result = showtimeService.findShowtimeById(100L);

        assertEquals(showtime, result);
    }

    @Test
    void findShowtimeById_NotFound() {
        when(showtimeRepository.findWithRefsById(200L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.findShowtimeById(200L));
    }

    @Test
    void deleteShowtimeById_Success() {
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(showtime));
        when(ticketRepository.existsByShowtime_Id(100L)).thenReturn(false);

        assertDoesNotThrow(() -> showtimeService.deleteShowtimeById(100L));

        verify(showtimeRepository, times(1)).delete(showtime);
    }

    @Test
    void getShowtimeById_Success() {
        when(showtimeRepository.findWithRefsById(100L)).thenReturn(Optional.of(showtime));
        when(showtimeMapper.mapShowtimeToResponse(showtime)).thenReturn(response);

        ResponseMessage<ShowtimeResponse> result = showtimeService.getShowtimeById(100L);

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.SHOWTIME_FOUND, result.getMessage());
        assertEquals(response, result.getReturnBody());
    }

    @Test
    void updateShowtimeById_Success() {
        ShowtimeRequest updateRequest = ShowtimeRequest.builder()
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .hallId(1L)
                .movieId(10L)
                .build();

        when(showtimeRepository.findWithRefsById(100L)).thenReturn(Optional.of(showtime));
        when(hallService.findHallById(1L)).thenReturn(hall);
        when(movieService.findMovieById(10L)).thenReturn(movie);

        // mapper is void, we manually update the entity inside
        doAnswer(invocation -> {
            Showtime s      = invocation.getArgument(0);
            ShowtimeRequest r = invocation.getArgument(1);
            Hall h          = invocation.getArgument(2);
            Movie m         = invocation.getArgument(3);

            s.setDate(r.getDate());
            s.setStartTime(r.getStartTime());
            s.setEndTime(r.getEndTime());
            s.setHall(h);
            s.setMovie(m);
            return null;
        }).when(showtimeMapper).updateShowtimeFromRequest(eq(showtime), eq(updateRequest), eq(hall), eq(movie));

        when(showtimeRepository.save(showtime)).thenReturn(showtime);
        when(showtimeMapper.mapShowtimeToResponse(showtime)).thenReturn(response);

        ResponseMessage<ShowtimeResponse> result = showtimeService.updateShowtimeById(100L, updateRequest);

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.SHOWTIME_UPDATED, result.getMessage());
        verify(showtimeRepository, times(1)).save(showtime);
    }

    // ======================== S01 ENDPOINT TESTS ========================

    @Test
    void getShowtimesByCinemaId_Success_WithMultipleHallsAndMovies() {
        Long cinemaId = 1L;
        LocalDate testDate = LocalDate.now().plusDays(1);

        List<ShowtimeRepository.HallMovieTimeRow> mockRows = createMockRows(testDate);

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(mockRows);

        ResponseMessage<List<HallWithShowtimesResponse>> result =
                showtimeService.getShowtimesByCinemaId(cinemaId);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.SHOWTIMES_FOUND_BY_CINEMA, result.getMessage());

        List<HallWithShowtimesResponse> halls = result.getReturnBody();
        assertEquals(2, halls.size());

        assertEquals("Hall-A", halls.get(0).getName());
        assertEquals("Hall-B", halls.get(1).getName());

        HallWithShowtimesResponse firstHall = halls.get(0);
        assertEquals(1L, firstHall.getId());
        assertEquals(100, firstHall.getSeatCapacity());
        assertFalse(firstHall.getIsSpecial());
        assertEquals(2, firstHall.getMovies().size());

        assertEquals("Movie-Alpha", firstHall.getMovies().get(0).getMovie().getTitle());
        assertEquals("Movie-Beta", firstHall.getMovies().get(1).getMovie().getTitle());

        HallMovieShowtimesResponse movieAlphaShowtimes = firstHall.getMovies().get(0);
        assertEquals(2, movieAlphaShowtimes.getTimes().size());
        assertEquals(LocalDateTime.of(testDate, LocalTime.of(14, 0)), movieAlphaShowtimes.getTimes().get(0));
        assertEquals(LocalDateTime.of(testDate, LocalTime.of(18, 0)), movieAlphaShowtimes.getTimes().get(1));

        HallWithShowtimesResponse secondHall = halls.get(1);
        assertEquals(2L, secondHall.getId());
        assertEquals(200, secondHall.getSeatCapacity());
        assertTrue(secondHall.getIsSpecial());
        assertEquals(1, secondHall.getMovies().size());
        assertEquals("Movie-Charlie", secondHall.getMovies().get(0).getMovie().getTitle());

        verify(cinemaRepository).findById(cinemaId);
        verify(showtimeRepository).findShowtimesByCinemaId(cinemaId);
    }

    @Test
    void getShowtimesByCinemaId_CinemaNotFound_ThrowsResourceNotFoundException() {
        Long cinemaId = 999L;
        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> showtimeService.getShowtimesByCinemaId(cinemaId)
        );

        assertEquals(String.format(ErrorMessages.CINEMA_NOT_FOUND, cinemaId), ex.getMessage());

        verify(cinemaRepository).findById(cinemaId);
        verifyNoInteractions(showtimeRepository);
    }

    @Test
    void getShowtimesByCinemaId_NoShowtimes_ThrowsResourceNotFoundException() {
        Long cinemaId = 1L;
        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(new ArrayList<>());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> showtimeService.getShowtimesByCinemaId(cinemaId)
        );

        assertEquals(ErrorMessages.SHOWTIMES_NOT_FOUND, ex.getMessage());

        verify(cinemaRepository).findById(cinemaId);
        verify(showtimeRepository).findShowtimesByCinemaId(cinemaId);
    }

    @Test
    void getShowtimesByCinemaId_SingleHallSingleMovie_Success() {
        Long cinemaId = 1L;
        LocalDate testDate = LocalDate.now().plusDays(1);

        List<ShowtimeRepository.HallMovieTimeRow> mockRows = List.of(
                createMockRow(1L, "Hall-Single", 150, true, 10L, "Movie-Single",
                        testDate, LocalTime.of(20, 30))
        );

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(mockRows);

        ResponseMessage<List<HallWithShowtimesResponse>> result =
                showtimeService.getShowtimesByCinemaId(cinemaId);

        List<HallWithShowtimesResponse> halls = result.getReturnBody();
        assertEquals(1, halls.size());

        HallWithShowtimesResponse hallResp = halls.get(0);
        assertEquals(1L, hallResp.getId());
        assertEquals("Hall-Single", hallResp.getName());
        assertEquals(150, hallResp.getSeatCapacity());
        assertTrue(hallResp.getIsSpecial());
        assertEquals(1, hallResp.getMovies().size());

        HallMovieShowtimesResponse movieShowtime = hallResp.getMovies().get(0);
        assertEquals(10L, movieShowtime.getMovie().getId());
        assertEquals("Movie-Single", movieShowtime.getMovie().getTitle());
        assertEquals(1, movieShowtime.getTimes().size());
        assertEquals(LocalDateTime.of(testDate, LocalTime.of(20, 30)), movieShowtime.getTimes().get(0));
    }

    @Test
    void getShowtimesByCinemaId_SameMovieInMultipleHalls_Success() {
        Long cinemaId = 1L;
        LocalDate testDate = LocalDate.now().plusDays(1);

        List<ShowtimeRepository.HallMovieTimeRow> mockRows = List.of(
                createMockRow(1L, "Hall-A", 100, false, 10L, "Popular-Movie",
                        testDate, LocalTime.of(14, 0)),
                createMockRow(2L, "Hall-B", 200, true, 10L, "Popular-Movie",
                        testDate, LocalTime.of(16, 30))
        );

        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(mockRows);

        ResponseMessage<List<HallWithShowtimesResponse>> result =
                showtimeService.getShowtimesByCinemaId(cinemaId);

        List<HallWithShowtimesResponse> halls = result.getReturnBody();
        assertEquals(2, halls.size());

        assertEquals("Popular-Movie", halls.get(0).getMovies().get(0).getMovie().getTitle());
        assertEquals("Popular-Movie", halls.get(1).getMovies().get(0).getMovie().getTitle());

        assertEquals(LocalDateTime.of(testDate, LocalTime.of(14, 0)),
                halls.get(0).getMovies().get(0).getTimes().get(0));
        assertEquals(LocalDateTime.of(testDate, LocalTime.of(16, 30)),
                halls.get(1).getMovies().get(0).getTimes().get(0));
    }

    // ======================== HELPER METHODS ========================

    private List<ShowtimeRepository.HallMovieTimeRow> createMockRows(LocalDate testDate) {
        return List.of(
                // Hall 1 - Movie 1 (multiple showtimes)
                createMockRow(1L, "Hall-A", 100, false, 10L, "Movie-Alpha",
                        testDate, LocalTime.of(14, 0)),
                createMockRow(1L, "Hall-A", 100, false, 10L, "Movie-Alpha",
                        testDate, LocalTime.of(18, 0)),
                // Hall 1 - Movie 2 (single showtime)
                createMockRow(1L, "Hall-A", 100, false, 20L, "Movie-Beta",
                        testDate, LocalTime.of(21, 30)),
                // Hall 2 - Movie 3 (single showtime)
                createMockRow(2L, "Hall-B", 200, true, 30L, "Movie-Charlie",
                        testDate, LocalTime.of(19, 15))
        );
    }

    private ShowtimeRepository.HallMovieTimeRow createMockRow(
            Long hallId, String hallName, Integer seatCapacity, Boolean isSpecial,
            Long movieId, String movieTitle, LocalDate date, LocalTime startTime) {

        return new ShowtimeRepository.HallMovieTimeRow() {
            @Override public Long getHallId() { return hallId; }
            @Override public String getHallName() { return hallName; }
            @Override public Integer getSeatCapacity() { return seatCapacity; }
            @Override public Boolean getIsSpecial() { return isSpecial; }
            @Override public Long getMovieId() { return movieId; }
            @Override public String getMovieTitle() { return movieTitle; }
            @Override public LocalDate getDate() { return date; }
            @Override public LocalTime getStartTime() { return startTime; }
        };
    }
}
