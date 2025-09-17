package com.cinetime.service.business;

import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.service.helper.CinemasHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaMapper cinemaMapper;
    @Mock private CinemasHelper cinemasHelper;

    @InjectMocks private CinemaService cinemaService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 5);
    }

    @Test
    void searchCinemas_cityOnly_callsRepoOverloadWithoutSpecialHall() {
        // Given
        Long cityId = 99L;

        // 2 adet Cinema mock’u (entity tipini koruyalım)
        var e1 = mock(com.cinetime.entity.business.Cinema.class);
        var e2 = mock(com.cinetime.entity.business.Cinema.class);

        var pageEntities = new PageImpl<>(java.util.List.of(e1, e2), pageable, 2);

        when(cinemaRepository.search(eq(cityId), eq(pageable)))
                .thenReturn(pageEntities);

        when(cinemaMapper.toSummary(e1))
                .thenReturn(CinemaSummaryResponse.builder().id(1L).name("A").build());
        when(cinemaMapper.toSummary(e2))
                .thenReturn(CinemaSummaryResponse.builder().id(2L).name("B").build());

        // When
        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, pageable);

        // Then
        verify(cinemasHelper).validateCityIfProvided(cityId);
        verify(cinemaRepository).search(cityId, pageable);
        verify(cinemaMapper, times(2)).toSummary(any());
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.map(CinemaSummaryResponse::getName).getContent())
                .containsExactly("A", "B");
    }

    @Test
    void searchCinemas_cityAndSpecialHall_true_case() {
        // Given
        Long cityId = 10L;
        String specialHall = "special"; // helper TRUE dönsün
        when(cinemasHelper.parseSpecialHall(specialHall)).thenReturn(Boolean.TRUE);

        var e = mock(com.cinetime.entity.business.Cinema.class);
        var pageEntities = new PageImpl<>(java.util.List.of(e), pageable, 1);

        when(cinemaRepository.search(eq(cityId), eq(Boolean.TRUE), eq(pageable)))
                .thenReturn(pageEntities);

        when(cinemaMapper.toSummary(e))
                .thenReturn(CinemaSummaryResponse.builder().id(11L).name("OnlySpecial").build());

        // When
        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, specialHall, pageable);

        // Then
        verify(cinemasHelper).validateCityIfProvided(cityId);
        verify(cinemasHelper).parseSpecialHall(specialHall);
        verify(cinemaRepository).search(cityId, true, pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("OnlySpecial");
    }

    @Test
    void searchCinemas_cityAndSpecialHall_null_flowsWithNullToRepo() {
        // Given
        Long cityId = 7L;
        String specialHall = null; // helper null dönsün
        when(cinemasHelper.parseSpecialHall(specialHall)).thenReturn(null);

        var e = mock(com.cinetime.entity.business.Cinema.class);
        var pageEntities = new PageImpl<>(java.util.List.of(e), pageable, 1);

        when(cinemaRepository.search(eq(cityId), isNull(), eq(pageable)))
                .thenReturn(pageEntities);

        when(cinemaMapper.toSummary(e))
                .thenReturn(CinemaSummaryResponse.builder().id(70L).name("NullFlow").build());

        // When
        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, specialHall, pageable);

        // Then
        verify(cinemasHelper).validateCityIfProvided(cityId);
        verify(cinemasHelper).parseSpecialHall(null);
        verify(cinemaRepository).search(cityId, null, pageable);
        assertThat(result.getContent().get(0).getName()).isEqualTo("NullFlow");
    }

    @Test
    void searchCinemas_cityValidationFails_bubblesUp() {
        // Given
        Long cityId = 123L;
        doThrow(new RuntimeException("city invalid"))
                .when(cinemasHelper).validateCityIfProvided(cityId);

        // When / Then
        try {
            cinemaService.searchCinemas(cityId, pageable);
        } catch (RuntimeException ex) {
            assertThat(ex).hasMessageContaining("city invalid");
        }

        verifyNoInteractions(cinemaRepository);
        verify(cinemaMapper, never()).toSummary(any());
    }
}
