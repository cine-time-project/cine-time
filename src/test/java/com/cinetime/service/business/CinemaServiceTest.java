package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.Hall;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.mappers.HallMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.payload.response.business.HallWithShowtimesResponse;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.HallMovieTimeRow;
import com.cinetime.repository.business.HallRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaMapper cinemaMapper;
    @Mock private CinemasHelper cinemasHelper;
    @Mock private UserRepository userRepository; // <-- yeni
    @Mock private ShowtimeRepository showtimeRepository;

    @Mock private HallRepository hallRepository;   // <-- özel salonlar için
    @Mock private HallMapper hallMapper;           // <-- entity->dto

    @InjectMocks private CinemaService cinemaService;
    @InjectMocks private CinemaService service;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 5);
    }

    @Test
    void searchCinemas_cityOnly_callsRepoOverloadWithoutSpecialHall() {
        Long cityId = 99L;

        var e1 = mock(com.cinetime.entity.business.Cinema.class);
        var e2 = mock(com.cinetime.entity.business.Cinema.class);

        var pageEntities = new PageImpl<>(java.util.List.of(e1, e2), pageable, 2);

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

        var e = mock(com.cinetime.entity.business.Cinema.class);
        var pageEntities = new PageImpl<>(java.util.List.of(e), pageable, 1);
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
        String specialHall = null;
        when(cinemasHelper.parseSpecialHall(specialHall)).thenReturn(null);

        var e = mock(com.cinetime.entity.business.Cinema.class);
        var pageEntities = new PageImpl<>(java.util.List.of(e), pageable, 1);
        when(cinemaRepository.search(eq(cityId), isNull(), eq(pageable))).thenReturn(pageEntities);
        when(cinemaMapper.toSummary(e)).thenReturn(CinemaSummaryResponse.builder().id(70L).name("NullFlow").build());

        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, specialHall, pageable);

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

        try {
            cinemaService.searchCinemas(cityId, pageable);
        } catch (RuntimeException ex) {
            assertThat(ex).hasMessageContaining("city invalid");
        }

        verifyNoInteractions(cinemaRepository);
        verify(cinemaMapper, never()).toSummary(any());
    }

    @Test
    void getCinemaById_found_returnsSummary() {
        Long id = 11L;
        var entity = mock(com.cinetime.entity.business.Cinema.class);
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

    // ---------------- NEW: Favorites by login ----------------

    @Test
    void getAuthFavoritesByLogin_userFound_returnsList() {
        String login = "member1@example.com";
        var user = mock(User.class);
        when(user.getId()).thenReturn(5L);
        when(userRepository.findByLoginProperty(login)).thenReturn(Optional.of(user));

        var e1 = mock(com.cinetime.entity.business.Cinema.class);
        var e2 = mock(com.cinetime.entity.business.Cinema.class);
        var pageEntities = new PageImpl<>(java.util.List.of(e1, e2), pageable, 2);

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



    // Yardımcı: interface-based projection için küçük bir satır implementasyonu
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
            this.hallId = hallId;
            this.hallName = hallName;
            this.seatCapacity = seatCapacity;
            this.isSpecial = isSpecial;
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.date = date;
            this.startTime = startTime;
        }

        @Override public Long getHallId() { return hallId; }
        @Override public String getHallName() { return hallName; }
        @Override public Integer getSeatCapacity() { return seatCapacity; }
        @Override public Boolean getIsSpecial() { return isSpecial; }
        @Override public Long getMovieId() { return movieId; }
        @Override public String getMovieTitle() { return movieTitle; }
        @Override public LocalDate getDate() { return date; }
        @Override public LocalTime getStartTime() { return startTime; }
    }

    @Test
    void getCinemaHallsWithShowtimes_whenCinemaNotExists_throwsNotFound() {
        Long cinemaId = 999L;
        when(cinemaRepository.existsById(cinemaId)).thenReturn(false);

        assertThatThrownBy(() -> service.getCinemaHallsWithShowtimes(cinemaId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cinemaRepository).existsById(cinemaId);
        verifyNoInteractions(showtimeRepository);
    }

    @Test
    void getCinemaHallsWithShowtimes_whenNoRows_returnsEmptyList() {
        Long cinemaId = 10L;
        when(cinemaRepository.existsById(cinemaId)).thenReturn(true);
        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(List.of());

        var result = service.getCinemaHallsWithShowtimes(cinemaId);
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
        HallMovieTimeRow r4 = new Row(101L, "Salon 2",  90, false,4L, "Inception", today, LocalTime.of(19,15));

        List<HallMovieTimeRow> rows = List.of(r1, r2, r3, r4);

        when(showtimeRepository.findShowtimesByCinemaId(cinemaId)).thenReturn(rows);

        var result = service.getCinemaHallsWithShowtimes(cinemaId);

        // 2 hall bekliyoruz
        assertThat(result).hasSize(2);

        // Hall 100 kontrol
        HallWithShowtimesResponse h100 = result.stream()
                .filter(h -> h.getId().equals(100L)).findFirst().orElseThrow();
        assertThat(h100.getName()).isEqualTo("Salon 1");
        assertThat(h100.getSeatCapacity()).isEqualTo(120);
        assertThat(h100.getIsSpecial()).isTrue();
        assertThat(h100.getMovies()).hasSize(2);

        var inceptionGroup = h100.getMovies().stream()
                .filter(g -> g.getMovie().getId().equals(4L)).findFirst().orElseThrow();
        assertThat(inceptionGroup.getMovie().getTitle()).isEqualTo("Inception");
        assertThat(inceptionGroup.getTimes())
                .containsExactly( // sıralı (18:00, 20:30)
                        LocalDateTime.of(today, LocalTime.of(18, 0)),
                        LocalDateTime.of(today, LocalTime.of(20, 30))
                );

        var duneGroup = h100.getMovies().stream()
                .filter(g -> g.getMovie().getId().equals(5L)).findFirst().orElseThrow();
        assertThat(duneGroup.getTimes())
                .containsExactly(LocalDateTime.of(today, LocalTime.of(21, 0)));

        // Hall 101 kontrol
        var h101 = result.stream().filter(h -> h.getId().equals(101L)).findFirst().orElseThrow();
        assertThat(h101.getName()).isEqualTo("Salon 2");
        assertThat(h101.getMovies()).hasSize(1);
        assertThat(h101.getMovies().get(0).getTimes())
                .containsExactly(LocalDateTime.of(today, LocalTime.of(19, 15)));

        verify(cinemaRepository).existsById(cinemaId);
        verify(showtimeRepository).findShowtimesByCinemaId(cinemaId);
    }

    @Test
    void getAllSpecialHalls_whenExists_mapsAndReturnsList() {
        // arrange: entity(ler)
        var cinema = new Cinema();
        cinema.setId(11L); cinema.setName("CineTime Beşiktaş");

        var h1 = Hall.builder().id(100L).name("IMAX").seatCapacity(200).isSpecial(true).cinema(cinema).build();
        var h2 = Hall.builder().id(101L).name("4DX").seatCapacity(160).isSpecial(true).cinema(cinema).build();
        when(hallRepository.findByIsSpecialTrueOrderByNameAsc()).thenReturn(List.of(h1, h2));

        // arrange: mapper çıktıları
        var dto1 = SpecialHallResponse.builder()
                .id(100L).name("IMAX").seatCapacity(200)
                .cinemaId(11L).cinemaName("CineTime Beşiktaş").build();
        var dto2 = SpecialHallResponse.builder()
                .id(101L).name("4DX").seatCapacity(160)
                .cinemaId(11L).cinemaName("CineTime Beşiktaş").build();

        when(hallMapper.toSpecial(h1)).thenReturn(dto1);
        when(hallMapper.toSpecial(h2)).thenReturn(dto2);

        // act
        var result = service.getAllSpecialHalls();

        // assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(SpecialHallResponse::getName)
                .containsExactly("IMAX", "4DX");

        verify(hallRepository).findByIsSpecialTrueOrderByNameAsc();
        verify(hallMapper).toSpecial(h1);
        verify(hallMapper).toSpecial(h2);
        verifyNoMoreInteractions(hallMapper);
    }

}





