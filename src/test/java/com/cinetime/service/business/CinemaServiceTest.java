package com.cinetime.service.business;

import com.cinetime.entity.business.*;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.mappers.HallMapper;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import com.cinetime.repository.business.ShowtimeRepository.HallMovieTimeRow;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.*;
import java.util.HashSet;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @InjectMocks
    private CinemaService cinemaService;

    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaMapper cinemaMapper;
    @Mock private CinemasHelper cinemasHelper;
    @Mock private UserRepository userRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private HallRepository hallRepository;
    @Mock private HallMapper hallMapper;
    @Mock private CityRepository cityRepository;
    @Mock private TicketRepository ticketRepository; // service ctor'unda var

    private Pageable pageable;

    @BeforeEach
    void setup() {
        pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
    }

    // ---------- listCinemas ----------
    @Test
    void listCinemas_ok_returnsPageWrappedInResponseMessage() {
        Long cityId = 1L;
        Boolean isSpecial = true;

        Cinema c1 = Cinema.builder().id(10L).name("A").slug("a").build();
        Cinema c2 = Cinema.builder().id(11L).name("B").slug("b").build();
        Page<Cinema> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(cinemaRepository.search(cityId, isSpecial, pageable)).thenReturn(page);

        CinemaSummaryResponse dto1 = CinemaSummaryResponse.builder().id(10L).name("A").build();
        CinemaSummaryResponse dto2 = CinemaSummaryResponse.builder().id(11L).name("B").build();

        when(cinemaMapper.toSummary(c1)).thenReturn(dto1);
        when(cinemaMapper.toSummary(c2)).thenReturn(dto2);

        ResponseMessage<Page<CinemaSummaryResponse>> resp =
                cinemaService.listCinemas(cityId, isSpecial, pageable);

        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(resp.getMessage()).isEqualTo(SuccessMessages.CINEMAS_LISTED);
        assertThat(resp.getReturnBody().getContent()).containsExactly(dto1, dto2);
    }

    // ---------- getCinemaById ----------
    @Test
    void getCinemaById_ok() {
        Long id = 42L;
        Cinema entity = Cinema.builder().id(id).name("Kanyon").slug("kanyon").build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(entity));

        CinemaSummaryResponse dto = CinemaSummaryResponse.builder().id(id).name("Kanyon").build();
        when(cinemaMapper.toSummary(entity)).thenReturn(dto);

        ResponseMessage<CinemaSummaryResponse> resp = cinemaService.getCinemaById(id);

        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(resp.getMessage()).isEqualTo(String.format(SuccessMessages.CINEMA_FETCHED, id));
        assertThat(resp.getReturnBody()).isEqualTo(dto);
    }

    @Test
    void getCinemaById_notFound_throws() {
        when(cinemaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cinemaService.getCinemaById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorMessages.CINEMA_NOT_FOUND);
    }

    // ---------- getById helper ----------
    @Test
    void getById_ok() {
        Long id = 5L;
        Cinema c = Cinema.builder().id(id).name("X").slug("x").build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(c));

        Cinema found = cinemaService.getById(id);
        assertThat(found).isSameAs(c);
    }

    @Test
    void getById_notFound_throws() {
        when(cinemaRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cinemaService.getById(123L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorMessages.CINEMA_NOT_FOUND);
    }

    // ---------- getAuthFavoritesByLogin ----------
    @Test
    void getAuthFavoritesByLogin_ok() {
        String login = "esra@example.com";
        User user = new User();
        user.setId(7L);

        when(userRepository.findByLoginProperty(login)).thenReturn(Optional.of(user));

        Cinema c1 = Cinema.builder().id(1L).name("A").slug("a").build();
        Cinema c2 = Cinema.builder().id(2L).name("B").slug("b").build();
        Page<Cinema> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(cinemaRepository.findFavoriteCinemasByUserId(7L, pageable)).thenReturn(page);

        CinemaSummaryResponse d1 = CinemaSummaryResponse.builder().id(1L).name("A").build();
        CinemaSummaryResponse d2 = CinemaSummaryResponse.builder().id(2L).name("B").build();
        when(cinemaMapper.toSummary(c1)).thenReturn(d1);
        when(cinemaMapper.toSummary(c2)).thenReturn(d2);

        ResponseMessage<Page<CinemaSummaryResponse>> resp =
                cinemaService.getAuthFavoritesByLogin(login, pageable);

        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(resp.getMessage()).isEqualTo(SuccessMessages.FAVORITES_LISTED);
        assertThat(resp.getReturnBody().getContent()).containsExactly(d1, d2);
    }

    @Test
    void getAuthFavoritesByLogin_userNotFound_throws() {
        when(userRepository.findByLoginProperty("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cinemaService.getAuthFavoritesByLogin("ghost", pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorMessages.NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD);
    }

    // ---------- getCinemaHallsWithShowtimes ----------
    @Test
    void getCinemaHallsWithShowtimes_ok_groupsByHallAndMovie() {
        Long cinemaId = 99L;
        when(cinemaRepository.existsById(cinemaId)).thenReturn(true);

        List<HallMovieTimeRow> rows = List.of(
                row(1L, "Hall-1", 100, false, 1000L, "Movie-A", LocalDate.now(), LocalTime.of(10, 0)),
                row(1L, "Hall-1", 100, false, 1000L, "Movie-A", LocalDate.now(), LocalTime.of(12, 0)),
                row(1L, "Hall-1", 100, false, 1001L, "Movie-B", LocalDate.now(), LocalTime.of(13, 30)),
                row(2L, "Hall-2", 80,  true, 1000L, "Movie-A", LocalDate.now(), LocalTime.of(11, 15))
        );
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(rows);

        List<HallWithShowtimesResponse> resp = cinemaService.getCinemaHallsWithShowtimes(cinemaId);

        assertThat(resp).hasSize(2);
        HallWithShowtimesResponse h1 = resp.get(0);
        assertThat(h1.getId()).isEqualTo(1L);
        assertThat(h1.getMovies()).hasSize(2);
        var timesA = h1.getMovies().get(0).getTimes();
        assertThat(timesA).isSorted();
    }

    @Test
    void getCinemaHallsWithShowtimes_cinemaNotFound_throws() {
        when(cinemaRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> cinemaService.getCinemaHallsWithShowtimes(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format(ErrorMessages.CINEMA_NOT_FOUND, 404L));
    }


    // ---------- getAllSpecialHalls ----------
    @Test
    void getAllSpecialHalls_ok() {
        Hall h1 = Hall.builder().id(1L).name("IMAX").seatCapacity(200).isSpecial(true).build();
        Hall h2 = Hall.builder().id(2L).name("4DX").seatCapacity(120).isSpecial(true).build();

        when(hallRepository.findByIsSpecialTrueOrderByNameAsc()).thenReturn(List.of(h1, h2));

        SpecialHallResponse r1 = SpecialHallResponse.builder().id(1L).name("IMAX").build();
        SpecialHallResponse r2 = SpecialHallResponse.builder().id(2L).name("4DX").build();
        when(hallMapper.toSpecial(h1)).thenReturn(r1);
        when(hallMapper.toSpecial(h2)).thenReturn(r2);

        ResponseMessage<List<SpecialHallResponse>> resp = cinemaService.getAllSpecialHalls();

        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(resp.getMessage()).isEqualTo(SuccessMessages.SPECIAL_HALLS_LISTED);
        assertThat(resp.getReturnBody()).containsExactly(r1, r2);
    }

    // ---------- createCinema ----------
    @Test
    void createCinema_ok_withCities() {
        CinemaCreateRequest req = new CinemaCreateRequest();
        req.setName("Zorlu");
        req.setSlug(null);
        req.setCityIds(Set.of(10L, 20L));

        when(cinemasHelper.slugify("Zorlu")).thenReturn("zorlu");
        when(cinemasHelper.ensureUniqueSlug("zorlu")).thenReturn("zorlu");

        City city10 = City.builder().id(10L).name("Istanbul").build();
        City city20 = City.builder().id(20L).name("Ankara").build();
        when(cityRepository.findAllById(Set.of(10L, 20L))).thenReturn(List.of(city10, city20));

        Cinema saved = Cinema.builder().id(1L).name("Zorlu").slug("zorlu")
                .cities(new LinkedHashSet<>(List.of(city10, city20))).build();
        when(cinemaRepository.save(any(Cinema.class))).thenReturn(saved);

        CinemaSummaryResponse dto = CinemaSummaryResponse.builder().id(1L).name("Zorlu").build();
        when(cinemaMapper.toSummary(saved)).thenReturn(dto);

        ResponseMessage<CinemaSummaryResponse> resp = cinemaService.createCinema(req);

        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.CREATED);
        assertThat(resp.getMessage()).isEqualTo(SuccessMessages.CINEMA_CREATED);
        assertThat(resp.getReturnBody()).isEqualTo(dto);

        ArgumentCaptor<Cinema> cap = ArgumentCaptor.forClass(Cinema.class);
        verify(cinemaRepository).save(cap.capture());
        assertThat(cap.getValue().getCities()).extracting(City::getId).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void createCinema_missingCity_throws() {
        CinemaCreateRequest req = new CinemaCreateRequest();
        req.setName("Zorlu");
        req.setCityIds(Set.of(10L, 99L));

        when(cinemasHelper.slugify("Zorlu")).thenReturn("zorlu");
        when(cinemasHelper.ensureUniqueSlug("zorlu")).thenReturn("zorlu");

        City city10 = City.builder().id(10L).name("Istanbul").build();
        when(cityRepository.findAllById(Set.of(10L, 99L))).thenReturn(List.of(city10));

        assertThatThrownBy(() -> cinemaService.createCinema(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorMessages.CITY_NOT_FOUND);
    }

    // ---------- update ----------
    @Test
    void update_ok_changesNameAndMakesUniqueSlug() {
        Long id = 5L;
        Cinema existing = Cinema.builder().id(id).name("Old").slug("old").build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        CinemaCreateRequest req = new CinemaCreateRequest();
        req.setName("New Name");
        req.setSlug(null);

        when(cinemasHelper.slugify("New Name")).thenReturn("new-name");
        when(cinemaRepository.existsBySlugIgnoreCaseAndIdNot("new-name", id)).thenReturn(true);
        when(cinemaRepository.existsBySlugIgnoreCaseAndIdNot("new-name-2", id)).thenReturn(false);

        Cinema saved = Cinema.builder().id(id).name("New Name").slug("new-name-2").build();
        when(cinemaRepository.save(existing)).thenReturn(saved);

        CinemaSummaryResponse dto = CinemaSummaryResponse.builder().id(id).name("New Name").build();
        when(cinemaMapper.toSummary(saved)).thenReturn(dto);

        ResponseMessage<CinemaSummaryResponse> resp = cinemaService.update(id, req);

        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(resp.getMessage()).isEqualTo(String.format(SuccessMessages.CINEMA_UPDATED, id));
        assertThat(resp.getReturnBody()).isEqualTo(dto);
        assertThat(existing.getSlug()).isEqualTo("new-name-2");
        assertThat(existing.getName()).isEqualTo("New Name");
    }

    @Test
    void update_clearCitiesWhenEmptySetProvided() {
        Long id = 6L;
        Set<City> current = new LinkedHashSet<>(List.of(City.builder().id(1L).name("X").build()));
        Cinema existing = Cinema.builder().id(id).name("C").slug("c").cities(current).build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        CinemaCreateRequest req = new CinemaCreateRequest();
        req.setCityIds(Collections.emptySet());

        when(cinemaRepository.save(existing)).thenReturn(existing);
        when(cinemaMapper.toSummary(existing))
                .thenReturn(CinemaSummaryResponse.builder().id(id).name("C").build());

        ResponseMessage<CinemaSummaryResponse> resp = cinemaService.update(id, req);

        assertThat(existing.getCities()).isEmpty();
        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.OK);
    }

    // ---------- delete ----------

    @Test
    void delete_ok_cascadePath() {
        Long id = 9L;

        Cinema c = Cinema.builder()
                .id(id)
                .name("Del")
                .slug("del")
                // ÖNEMLİ: Serviste clear() çağrılan set'leri builder’da boş ver
                .movies(new LinkedHashSet<>())
                .cities(new LinkedHashSet<>())
                .build();

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(c));

        ResponseMessage<Void> resp = cinemaService.delete(id);

        verify(cinemaRepository).delete(c);
        assertThat(resp.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(resp.getMessage()).isEqualTo(String.format(SuccessMessages.CINEMA_DELETED, id));
        assertThat(resp.getReturnBody()).isNull();
    }


    @Test
    void delete_notFound_throws() {
        when(cinemaRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.delete(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format(ErrorMessages.CINEMA_NOT_FOUND, 77L));
    }

    // ---------- helpers ----------
    private static HallMovieTimeRow row(
            Long hallId, String hallName, int seatCapacity, boolean isSpecial,
            Long movieId, String movieTitle,
            LocalDate date, LocalTime startTime
    ) {
        return new HallMovieTimeRow() {
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
