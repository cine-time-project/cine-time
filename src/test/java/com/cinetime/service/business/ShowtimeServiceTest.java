package com.cinetime.service.business;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.ShowtimeMapper;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.business.ShowtimeResponse;
import com.cinetime.repository.business.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private ShowtimeService showtimeService;

    private ShowtimeRequest request;
    private Showtime showtime;
    private ShowtimeResponse response;
    private Hall hall;
    private Movie movie;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        hall = new Hall();
        hall.setId(1L);
        hall.setName("IMAX");

        movie = new Movie();
        movie.setId(10L);
        movie.setTitle("Inception");

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
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(showtime));

        Showtime result = showtimeService.findShowtimeById(100L);

        assertEquals(showtime, result);
    }

    @Test
    void findShowtimeById_NotFound() {
        when(showtimeRepository.findById(200L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.findShowtimeById(200L));
    }

    @Test
    void deleteShowtimeById_Success() {
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(showtime));
        when(showtimeMapper.mapShowtimeToResponse(showtime)).thenReturn(response);

        ResponseMessage<ShowtimeResponse> result = showtimeService.deleteShowtimeById(100L);

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.SHOWTIME_DELETED, result.getMessage());
        verify(showtimeRepository, times(1)).delete(showtime);
    }

    @Test
    void getShowtimeById_Success() {
        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(showtime));
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

        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(showtime));
        when(hallService.findHallById(1L)).thenReturn(hall);
        when(movieService.findMovieById(10L)).thenReturn(movie);

        // mapper update metodu void olduğundan sadece doNothing
        doAnswer(invocation -> {
            Showtime s = invocation.getArgument(0);
            s.setDate(updateRequest.getDate());
            s.setStartTime(updateRequest.getStartTime());
            s.setEndTime(updateRequest.getEndTime());
            return null;
        }).when(showtimeMapper).updateShowtimeFromRequest(eq(showtime), eq(updateRequest), eq(hall), eq(movie));

        when(showtimeRepository.save(showtime)).thenReturn(showtime);
        when(showtimeMapper.mapShowtimeToResponse(showtime)).thenReturn(response);

        ResponseMessage<ShowtimeResponse> result = showtimeService.updateShowtimeById(100L, updateRequest);

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.SHOWTIME_UPDATED, result.getMessage());
        verify(showtimeRepository, times(1)).save(showtime);
    }
}
