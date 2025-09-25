package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.City;
import com.cinetime.entity.business.Hall;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.mappers.HallMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.CinemaCreateRequest;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.payload.response.business.HallWithShowtimesResponse;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.repository.business.*;
import com.cinetime.repository.business.ShowtimeRepository.HallMovieTimeRow;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaMapper cinemaMapper;
    @Mock private CinemasHelper cinemasHelper;
    @Mock private UserRepository userRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private CityRepository cityRepository;
    @Mock private HallRepository hallRepository;
    @Mock private HallMapper hallMapper;
    @Mock private TicketRepository ticketRepository;

    @InjectMocks private CinemaService cinemaService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 5);
    }

    // -------- SEARCH TESTS --------

    @Test
    void searchCinemas_cityOnly_callsRepoOverloadWithoutSpecialHall() {
        Long cityId = 99L;

        var e1 = mock(Cinema.class);
        var e2 = mock(Cinema.class);
        var pageEntities = new PageImpl<>(List.of(e1, e2), pageable, 2);

        when(cinemaRepository.search(eq(cityId), eq(pageable))).thenReturn(pageEntities);
        when(cinemaMapper.toSummary(e1)).thenReturn(CinemaSummaryResponse.builder().id(1L).name("A").build());
        when(cinemaMapper.toSummary(e2)).thenReturn(CinemaSummaryResponse.builder().id(2L).name("B").build());

        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, pageable);

        verify(cinemasHelper).validateCityIfProvided(cityId);
        verify(cinemaRepository).search(cityId, pageable);
        verify(cinemaMapper, times(2)).toSummary(any());
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.map(CinemaSummaryResponse::getName).getContent()).containsExactly("A", "B");
    }

    @Test
    void searchCinemas_cityAndSpecialHall_true_case() {
        Long cityId = 10L;
        String specialHall = "special";
        when(cinemasHelper.parseSpecialHall(specialHall)).thenReturn(Boolean.TRUE);

        var e = mock(Cinema.class);
        var pageEntities = new PageImpl<>(List.of(e), pageable, 1);
        when(cinemaRepository.search(eq(cityId), eq(Boolean.TRUE), eq(pageable))).thenReturn(pageEntities);
        when(cinemaMapper.toSummary(e)).thenReturn(CinemaSummaryResponse.builder().id(11L).name("OnlySpecial").build());

        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, specialHall, pageable);

        verify(cinemasHelper).validateCityIfProvided(cityId);
        verify(cinemasHelper).parseSpecialHall(specialHall);
        verify(cinemaRepository).search(cityId, true, pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("OnlySpecial");
    }

    @Test
    void searchCinemas_cityAndSpecialHall_null_flowsWithNullToRepo() {
        Long cityId = 7L;
        when(cinemasHelper.parseSpecialHall(null)).thenReturn(null);

        var e = mock(Cinema.class);
        var pageEntities = new PageImpl<>(List.of(e), pageable, 1);
        when(cinemaRepository.search(eq(cityId), isNull(Boolean.class), eq(pageable))).thenReturn(pageEntities);
        when(cinemaMapper.toSummary(e)).thenReturn(CinemaSummaryResponse.builder().id(70L).name("NullFlow").build());

        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, (String) null, pageable);

        verify(cinemasHelper).validateCityIfProvided(cityId);
        verify(cinemasHelper).parseSpecialHall(null);
        verify(cinemaRepository).search(cityId, null, pageable);
        assertThat(result.getContent().get(0).getName()).isEqualTo("NullFlow");
    }

    @Test
    void searchCinemas_cityValidationFails_bubblesUp() {
        Long cityId = 123L;
        doThrow(new RuntimeException("city invalid"))
                .when(cinemasHelper).validateCityIfProvided(cityId);

        assertThatThrownBy(() -> cinemaService.searchCinemas(cityId, pageable))
                .hasMessageContaining("city invalid");

        verifyNoInteractions(cinemaRepository);
        verify(cinemaMapper, never()).toSummary(any());
    }

    // -------- GET BY ID --------

    @Test
    void getCinemaById_found_returnsSummary() {
        Long id = 11L;
        var entity = mock(Cinema.class);
        var dto = CinemaSummaryResponse.builder().id(id).name("CineTime Besiktas").build();

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(cinemaMapper.toSummary(entity)).thenReturn(dto);

        var result = cinemaService.getCinemaById(id);

        verify(cinemaRepository).findById(id);
        verify(cinemaMapper).toSummary(entity);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("CineTime Besiktas");
    }

    @Test
    void getCinemaById_notFound_throwsNotFound() {
        Long id = 1L;
        when(cinemaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.getCinemaById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.CINEMA_NOT_FOUND);

        verify(cinemaRepository).findById(id);
        verifyNoInteractions(cinemaMapper);
    }

    // -------- FAVORITES BY LOGIN --------

    @Test
    void getAuthFavoritesByLogin_userFound_returnsList() {
        String login = "member1@example.com";
        var user = mock(User.class);
        when(user.getId()).thenReturn(5L);
        when(userRepository.findByLoginProperty(login)).thenReturn(Optional.of(user));

        var e1 = mock(Cinema.class);
        var e2 = mock(Cinema.class);
        var pageEntities = new PageImpl<>(List.of(e1, e2), pageable, 2);

        when(cinemaRepository.findFavoriteCinemasByUserId(5L, pageable)).thenReturn(pageEntities);
        when(cinemaMapper.toSummary(e1)).thenReturn(CinemaSummaryResponse.builder().id(11L).name("A").build());
        when(cinemaMapper.toSummary(e2)).thenReturn(CinemaSummaryResponse.builder().id(12L).name("B").build());

        var list = cinemaService.getAuthFavoritesByLogin(login, pageable);

        verify(userRepository).findByLoginProperty(login);
        verify(cinemaRepository).findFavoriteCinemasByUserId(5L, pageable);
        verify(cinemaMapper, times(2)).toSummary(any());

        assertThat(list).extracting(CinemaSummaryResponse::getName).containsExactly("A", "B");
    }

    @Test
    void getAuthFavoritesByLogin_userNotFound_throwsNotFound() {
        String login = "missing@example.com";
        when(userRepository.findByLoginProperty(login)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.getAuthFavoritesByLogin(login, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ErrorMessages.NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD);

        verify(userRepository).findByLoginProperty(login);
        verifyNoInteractions(cinemaRepository);
        verifyNoInteractions(cinemaMapper);
    }

    // -------- SHOWTIMES GROUPING --------

    private static class Row implements HallMovieTimeRow {
        private final Long hallId;
        private final String hallName;
        private final Integer seatCapacity;
        private final Boolean isSpecial;
        private final Long movieId;
        private final String movieTitle;
        private final LocalDate date;
        private final LocalTime startTime;
        Row(Long hallId, String hallName, Integer seatCapacity, Boolean isSpecial,
            Long movieId, String movieTitle, LocalDate date, LocalTime startTime) {
            this.hallId = hallId; this.hallName = hallName; this.seatCapacity = seatCapacity;
            this.isSpecial = isSpecial; this.movieId = movieId; this.movieTitle = movieTitle;
            this.date = date; this.startTime = startTime;
        }
        public Long getHallId() { return hallId; }
        public String getHallName() { return hallName; }
        public Integer getSeatCapacity() { return seatCapacity; }
        public Boolean getIsSpecial() { return isSpecial; }
        public Long getMovieId() { return movieId; }
        public String getMovieTitle() { return movieTitle; }
        public LocalDate getDate() { return date; }
        public LocalTime getStartTime() { return startTime; }
    }

    @Test
    void getCinemaHallsWithShowtimes_whenCinemaNotExists_throwsNotFound() {
        Long cinemaId = 999L;
        when(cinemaRepository.existsById(cinemaId)).thenReturn(false);

        assertThatThrownBy(() -> cinemaService.getCinemaHallsWithShowtimes(cinemaId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cinemaRepository).existsById(cinemaId);
        verifyNoInteractions(showtimeRepository);
    }

    @Test
    void getCinemaHallsWithShowtimes_whenNoRows_returnsEmptyList() {
        Long cinemaId = 10L;
        when(cinemaRepository.existsById(cinemaId)).thenReturn(true);
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(List.of());

        var result = cinemaService.getCinemaHallsWithShowtimes(cinemaId);
        assertThat(result).isEmpty();

        verify(cinemaRepository).existsById(cinemaId);
        verify(showtimeRepository).findShowtimesByCinemaId(cinemaId);
    }

    @Test
    void getCinemaHallsWithShowtimes_groupsByHallAndMovie_andSortsTimes() {
        Long cinemaId = 10L;
        when(cinemaRepository.existsById(cinemaId)).thenReturn(true);

        LocalDate today = LocalDate.now();
        HallMovieTimeRow r1 = new Row(100L, "Salon 1", 120, true, 4L, "Inception", today, LocalTime.of(20,30));
        HallMovieTimeRow r2 = new Row(100L, "Salon 1", 120, true, 4L, "Inception", today, LocalTime.of(18, 0));
        HallMovieTimeRow r3 = new Row(100L, "Salon 1", 120, true, 5L, "Dune",      today, LocalTime.of(21, 0));
        HallMovieTimeRow r4 = new Row(101L, "Salon 2",  90, false, 4L, "Inception", today, LocalTime.of(19,15));

        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(List.of(r1, r2, r3, r4));

        var result = cinemaService.getCinemaHallsWithShowtimes(cinemaId);

        assertThat(result).hasSize(2);
        var h100 = result.stream().filter(h -> h.getId().equals(100L)).findFirst().orElseThrow();
        assertThat(h100.getName()).isEqualTo("Salon 1");
        assertThat(h100.getSeatCapacity()).isEqualTo(120);
        assertThat(h100.getIsSpecial()).isTrue();
        assertThat(h100.getMovies()).hasSize(2);

        var inception = h100.getMovies().stream().filter(g -> g.getMovie().getId().equals(4L)).findFirst().orElseThrow();
        assertThat(inception.getTimes())
                .containsExactly(LocalDateTime.of(today, LocalTime.of(18, 0)),
                        LocalDateTime.of(today, LocalTime.of(20, 30)));

        var dune = h100.getMovies().stream().filter(g -> g.getMovie().getId().equals(5L)).findFirst().orElseThrow();
        assertThat(dune.getTimes()).containsExactly(LocalDateTime.of(today, LocalTime.of(21, 0)));

        var h101 = result.stream().filter(h -> h.getId().equals(101L)).findFirst().orElseThrow();
        assertThat(h101.getName()).isEqualTo("Salon 2");
        assertThat(h101.getMovies()).hasSize(1);
        assertThat(h101.getMovies().get(0).getTimes())
                .containsExactly(LocalDateTime.of(today, LocalTime.of(19, 15)));

        verify(cinemaRepository).existsById(cinemaId);
        verify(showtimeRepository).findShowtimesByCinemaId(cinemaId);
    }

    // -------- SPECIAL HALLS --------

    @Test
    void getAllSpecialHalls_whenExists_mapsAndReturnsList() {
        var cinema = new Cinema(); cinema.setId(11L); cinema.setName("CineTime Beşiktaş");

        var h1 = Hall.builder().id(100L).name("IMAX").seatCapacity(200).isSpecial(true).cinema(cinema).build();
        var h2 = Hall.builder().id(101L).name("4DX").seatCapacity(160).isSpecial(true).cinema(cinema).build();
        when(hallRepository.findByIsSpecialTrueOrderByNameAsc()).thenReturn(List.of(h1, h2));

        var dto1 = SpecialHallResponse.builder().id(100L).name("IMAX").seatCapacity(200).cinemaId(11L).cinemaName("CineTime Beşiktaş").build();
        var dto2 = SpecialHallResponse.builder().id(101L).name("4DX").seatCapacity(160).cinemaId(11L).cinemaName("CineTime Beşiktaş").build();

        when(hallMapper.toSpecial(h1)).thenReturn(dto1);
        when(hallMapper.toSpecial(h2)).thenReturn(dto2);

        var result = cinemaService.getAllSpecialHalls();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SpecialHallResponse::getName).containsExactly("IMAX", "4DX");

        verify(hallRepository).findByIsSpecialTrueOrderByNameAsc();
        verify(hallMapper).toSpecial(h1);
        verify(hallMapper).toSpecial(h2);
        verifyNoMoreInteractions(hallMapper);
    }

    // -------- UPDATE TESTS --------

    @Test
    void update_whenCinemaNotFound_throwsNotFound() {
        Long id = 999L;
        when(cinemaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.update(id, CinemaCreateRequest.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cinemaRepository).findById(id);
        verifyNoMoreInteractions(cinemaRepository);
    }

    @Test
    void update_whenNameChanges_slugRegeneratedAndUniqByExcludingSelf() {
        Long id = 10L;
        Cinema existing = Cinema.builder()
                .id(id).name("Old Name").slug("old-name")
                .cities(new LinkedHashSet<>())
                .build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        var req = CinemaCreateRequest.builder().name("New Name").build();

        when(cinemasHelper.slugify("New Name")).thenReturn("new-name");
        when(cinemaRepository.existsBySlugIgnoreCaseAndIdNot("new-name", id)).thenReturn(true);
        when(cinemaRepository.existsBySlugIgnoreCaseAndIdNot("new-name-2", id)).thenReturn(false);

        ArgumentCaptor<Cinema> cap = ArgumentCaptor.forClass(Cinema.class);
        when(cinemaRepository.save(cap.capture())).thenAnswer(inv -> cap.getValue());
        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.update(id, req);

        Cinema saved = cap.getValue();
        assertEquals("New Name", saved.getName());
        assertEquals("new-name-2", saved.getSlug());
        verify(cinemaRepository).existsBySlugIgnoreCaseAndIdNot("new-name", id);
        verify(cinemaRepository).existsBySlugIgnoreCaseAndIdNot("new-name-2", id);
    }

    @Test
    void update_whenSlugProvided_usedAsBaseAndUniqChecked() {
        Long id = 11L;
        Cinema existing = Cinema.builder()
                .id(id).name("Any").slug("any").cities(new LinkedHashSet<>())
                .build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        var req = CinemaCreateRequest.builder().slug(" CineStar  ").build();

        when(cinemasHelper.slugify(" CineStar  ")).thenReturn("cinestar");
        when(cinemaRepository.existsBySlugIgnoreCaseAndIdNot("cinestar", id)).thenReturn(false);

        ArgumentCaptor<Cinema> cap = ArgumentCaptor.forClass(Cinema.class);
        when(cinemaRepository.save(cap.capture())).thenAnswer(inv -> cap.getValue());
        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.update(id, req);

        assertEquals("cinestar", cap.getValue().getSlug());
        assertEquals("Any", cap.getValue().getName()); // name değişmedi
    }

    @Test
    void update_whenCityIdsNull_doesNotTouchCities() {
        Long id = 12L;
        City c1 = City.builder().id(1L).name("Istanbul").build();
        City c2 = City.builder().id(2L).name("Ankara").build();
        Cinema existing = Cinema.builder()
                .id(id).name("X").slug("x")
                .cities(new LinkedHashSet<>(List.of(c1, c2)))
                .build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        var req = CinemaCreateRequest.builder().name("X2").cityIds(null).build();

        when(cinemasHelper.slugify("X2")).thenReturn("x2");
        when(cinemaRepository.existsBySlugIgnoreCaseAndIdNot("x2", id)).thenReturn(false);

        ArgumentCaptor<Cinema> cap = ArgumentCaptor.forClass(Cinema.class);
        when(cinemaRepository.save(cap.capture())).thenAnswer(inv -> cap.getValue());
        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.update(id, req);

        assertThat(cap.getValue().getCities()).extracting(City::getId).containsExactlyInAnyOrder(1L, 2L);
        verify(cityRepository, never()).findAllById(any());
    }

    @Test
    void update_whenCityIdsEmpty_clearsAllLinks() {
        Long id = 13L;
        City c1 = City.builder().id(1L).name("Istanbul").build();
        City c2 = City.builder().id(2L).name("Ankara").build();
        Cinema existing = Cinema.builder()
                .id(id).name("Y").slug("y")
                .cities(new LinkedHashSet<>(List.of(c1, c2)))
                .build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        var req = CinemaCreateRequest.builder().cityIds(Set.of()).build();

        ArgumentCaptor<Cinema> cap = ArgumentCaptor.forClass(Cinema.class);
        when(cinemaRepository.save(cap.capture())).thenAnswer(inv -> cap.getValue());
        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.update(id, req);

        assertThat(cap.getValue().getCities()).isEmpty();
        verify(cityRepository, never()).findAllById(any());
    }

    @Test
    void update_whenCityIdsProvided_replacesWithFound_clearThenAddAll() {
        Long id = 14L;
        City old = City.builder().id(1L).name("Old").build();
        Cinema existing = Cinema.builder()
                .id(id).name("Z").slug("z")
                .cities(new LinkedHashSet<>(List.of(old)))
                .build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        var req = CinemaCreateRequest.builder().cityIds(Set.of(2L, 3L)).build();

        City c2 = City.builder().id(2L).name("A").build();
        City c3 = City.builder().id(3L).name("B").build();
        when(cityRepository.findAllById(any())).thenReturn(List.of(c2, c3));

        ArgumentCaptor<Cinema> cap = ArgumentCaptor.forClass(Cinema.class);
        when(cinemaRepository.save(cap.capture())).thenAnswer(inv -> cap.getValue());
        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.update(id, req);

        assertThat(cap.getValue().getCities()).extracting(City::getId).containsExactlyInAnyOrder(2L, 3L);
        assertThat(cap.getValue().getCities()).extracting(City::getId).doesNotContain(1L);
    }

    @Test
    void update_whenAnyRequestedCityMissing_throws404_andDoesNotSave() {
        Long id = 15L;
        Cinema existing = Cinema.builder()
                .id(id).name("K").slug("k")
                .cities(new LinkedHashSet<>())
                .build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        var req = CinemaCreateRequest.builder().cityIds(Set.of(5L, 6L)).build();

        City only5 = City.builder().id(5L).name("Only5").build();
        when(cityRepository.findAllById(new LinkedHashSet<>(Set.of(5L, 6L))))
                .thenReturn(List.of(only5));

        assertThatThrownBy(() -> cinemaService.update(id, req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cinemaRepository, never()).save(any());
    }

    // -------- CREATE TESTS (taşan kodlar toparlandı) --------

    @Test
    void create_shouldGenerateSlugFromName_andPersist_withCities() {
        var req = CinemaCreateRequest.builder()
                .name("CineStar Adanaaa")
                .cityIds(Set.of(1L))
                .build();

        when(cinemasHelper.slugify("CineStar Adanaaa")).thenReturn("cinestar-adanaaa");
        when(cinemasHelper.ensureUniqueSlug("cinestar-adanaaa")).thenReturn("cinestar-adanaaa");

        var city = City.builder().id(1L).name("Istanbul").build();
        when(cityRepository.findAllById(anySet())).thenReturn(List.of(city));

        ArgumentCaptor<Cinema> savedCap = ArgumentCaptor.forClass(Cinema.class);
        when(cinemaRepository.save(savedCap.capture()))
                .thenAnswer(inv -> { Cinema c = savedCap.getValue(); c.setId(101L); return c; });

        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.createCinema(req);

        Cinema saved = savedCap.getValue();
        assertEquals("CineStar Adanaaa", saved.getName());
        assertEquals("cinestar-adanaaa", saved.getSlug());
        assertEquals(1, saved.getCities().size());

        verify(cityRepository).findAllById(anySet());
        verify(cinemaRepository).save(any(Cinema.class));
        verify(cinemaMapper).toSummary(any(Cinema.class));
        verifyNoMoreInteractions(cinemaMapper);
    }

    @Test
    void create_shouldThrow404_whenAnyCityIdIsMissing() {
        var req = CinemaCreateRequest.builder()
                .name("CineStar X")
                .cityIds(Set.of(5L))
                .build();

        when(cityRepository.findAllById(Set.of(5L))).thenReturn(List.of());

        var ex = org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class, () -> cinemaService.create(req));
        assertThat(ex.getMessage()).contains("5");

        verify(cityRepository).findAllById(Set.of(5L));
        verify(cinemaRepository, never()).save(any());
    }

    @Test
    void create_shouldUseProvidedSlug_andEnsureUniqueness() {
        var req = CinemaCreateRequest.builder()
                .name("Any Name")
                .slug("cinestar")
                .build();

        ArgumentCaptor<Cinema> savedCap = ArgumentCaptor.forClass(Cinema.class);
        when(cinemaRepository.save(savedCap.capture())).thenAnswer(inv -> {
            Cinema c = savedCap.getValue(); c.setId(200L); return c;
        });
        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.create(req);

        verify(cinemaRepository).save(any(Cinema.class));
    }

    @Test
    void create_shouldNotTouchCities_whenCityIdsNullOrEmpty() {
        var req1 = CinemaCreateRequest.builder().name("C1").build();
        var req2 = CinemaCreateRequest.builder().name("C2").cityIds(Set.of()).build();

        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cinemaMapper.toSummary(any(Cinema.class))).thenReturn(new CinemaSummaryResponse());

        cinemaService.create(req1);
        cinemaService.create(req2);

        verify(cityRepository, never()).findAllById(anySet());
        verify(cinemaRepository, times(2)).save(any(Cinema.class));
    }

    @Test
    void delete_whenCinemaExists_deletesRelationsAndReturnsMessage() {
        // given
        Long id = 12L;
        Cinema existing = Cinema.builder().id(id).name("Any").slug("any").build();
        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));

        // when
        String result = cinemaService.delete(id);

        // then: mesaj
        assertThat(result).isEqualTo(String.format(SuccessMessages.CINEMA_DELETED, id));

        // then: çağrı sırası (opsiyonel ama güzel bir güvence)
        InOrder inOrder = inOrder(cinemaRepository, ticketRepository, showtimeRepository, hallRepository);
        inOrder.verify(cinemaRepository).findById(id);
        inOrder.verify(ticketRepository).deleteByCinemaId(id);
        inOrder.verify(showtimeRepository).deleteByCinemaId(id);
        inOrder.verify(hallRepository).deleteByCinemaId(id);
        inOrder.verify(cinemaRepository).deleteMovieLinks(id);
        inOrder.verify(cinemaRepository).deleteCityLinks(id);
        inOrder.verify(cinemaRepository).deleteById(id);
        inOrder.verifyNoMoreInteractions();

        // emniyet: hiçbir ekstra etkileşim yok
        verifyNoMoreInteractions(ticketRepository, showtimeRepository, hallRepository, cinemaRepository);
    }

    @Test
    void delete_whenCinemaNotFound_throws404_andDoesNotTouchRelations() {
        // given
        Long id = 99L;
        when(cinemaRepository.findById(id)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> cinemaService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format(ErrorMessages.CINEMA_NOT_FOUND, id));

        // then: silme metotları hiç çağrılmadı
        verify(ticketRepository, never()).deleteByCinemaId(anyLong());
        verify(showtimeRepository, never()).deleteByCinemaId(anyLong());
        verify(hallRepository, never()).deleteByCinemaId(anyLong());
        verify(cinemaRepository, never()).deleteMovieLinks(anyLong());
        verify(cinemaRepository, never()).deleteCityLinks(anyLong());
        verify(cinemaRepository, never()).deleteById(anyLong());
    }



}
