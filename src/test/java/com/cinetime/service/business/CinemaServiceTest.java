package com.cinetime.service.business;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

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

    @InjectMocks private CinemaService cinemaService;

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
}
