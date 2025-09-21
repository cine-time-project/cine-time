package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.Hall;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.HallMapper;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.HallRequest;
import com.cinetime.payload.response.business.HallResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.business.HallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HallServiceTest {

    @Mock
    private HallRepository hallRepository;
    @Mock
    private CinemaService cinemaService;
    @Mock
    private HallMapper hallMapper;

    @InjectMocks
    private HallService hallService;

    private Hall hall;
    private HallRequest hallRequest;
    private HallResponse hallResponse;
    private Cinema cinema;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinetech");

        hallRequest = HallRequest.builder()
                .name("IMAX")
                .seatCapacity(200)
                .isSpecial(true)
                .cinemaId(1L)
                .build();

        hall = Hall.builder()
                .id(100L)
                .name("IMAX")
                .seatCapacity(200)
                .isSpecial(true)
                .cinema(cinema)
                .build();

        hallResponse = HallResponse.builder()
                .id(100L)
                .name("IMAX")
                .seatCapacity(200)
                .isSpecial(true)
                .cinemaId(1L)
                .cinemaName("Cinetech")
                .build();
    }

    @Test
    void saveHall_Success() {
        when(cinemaService.getById(1L)).thenReturn(cinema);
        when(hallMapper.mapRequestToHall(hallRequest, cinema)).thenReturn(hall);
        when(hallRepository.save(hall)).thenReturn(hall);
        when(hallMapper.mapHallToResponse(hall)).thenReturn(hallResponse);

        ResponseMessage<HallResponse> result = hallService.saveHall(hallRequest);

        assertEquals(HttpStatus.CREATED, result.getHttpStatus());
        assertEquals(SuccessMessages.HALL_CREATED, result.getMessage());
        assertEquals(hallResponse, result.getReturnBody());
    }

    @Test
    void findHallById_Found() {
        when(hallRepository.findById(100L)).thenReturn(Optional.of(hall));

        Hall result = hallService.findHallById(100L);

        assertEquals(hall, result);
    }

    @Test
    void findHallById_NotFound() {
        when(hallRepository.findById(200L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hallService.findHallById(200L));
    }

    @Test
    void getHallById_Success() {
        when(hallRepository.findById(100L)).thenReturn(Optional.of(hall));
        when(hallMapper.mapHallToResponse(hall)).thenReturn(hallResponse);

        ResponseMessage<HallResponse> result = hallService.getHallById(100L);

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.HALL_FOUND, result.getMessage());
        assertEquals(hallResponse, result.getReturnBody());
    }

    @Test
    void getAllHalls_Success() {
        Page<Hall> hallPage = new PageImpl<>(List.of(hall));
        Page<HallResponse> hallResponsePage = new PageImpl<>(List.of(hallResponse));

        when(hallRepository.findAll(any(PageRequest.class))).thenReturn(hallPage);
        when(hallMapper.mapToResponsePage(hallPage)).thenReturn(hallResponsePage);

        ResponseMessage<Page<HallResponse>> result = hallService.getAllHalls(PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.HALLS_FOUND, result.getMessage());
        assertEquals(hallResponsePage, result.getReturnBody());
    }

    @Test
    void getAllHalls_NotFound() {
        Page<Hall> emptyPage = Page.empty();
        when(hallRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        assertThrows(ResourceNotFoundException.class, () -> hallService.getAllHalls(PageRequest.of(0, 10)));
    }

    @Test
    void deleteHallById_Success() {
        when(hallRepository.findById(100L)).thenReturn(Optional.of(hall));
        when(hallMapper.mapHallToResponse(hall)).thenReturn(hallResponse);

        ResponseMessage<HallResponse> result = hallService.deleteHallById(100L);

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.HALL_DELETED, result.getMessage());
        assertEquals(hallResponse, result.getReturnBody());
        verify(hallRepository, times(1)).delete(hall);
    }

    @Test
    void updateHall_Success() {
        HallRequest updateRequest = HallRequest.builder()
                .name("Dolby Atmos")
                .seatCapacity(250)
                .isSpecial(false)
                .cinemaId(1L)
                .build();

        when(hallRepository.findById(100L)).thenReturn(Optional.of(hall));
        when(cinemaService.getById(1L)).thenReturn(cinema);
        when(hallRepository.save(hall)).thenReturn(hall);
        when(hallMapper.mapHallToResponse(hall)).thenReturn(hallResponse);

        ResponseMessage<HallResponse> result = hallService.updateHall(updateRequest, 100L);

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.HALL_UPDATED, result.getMessage());
        assertEquals(hallResponse, result.getReturnBody());
        verify(hallRepository, times(1)).save(hall);
    }
}
