package com.cinetime.service.business;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.SpecialHall;
import com.cinetime.entity.business.SpecialHallType;
import com.cinetime.payload.mappers.SpecialHallMapper;
import com.cinetime.payload.request.business.SpecialHallRequest;
import com.cinetime.payload.response.business.HallPricingResponse;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.repository.business.HallRepository;
import com.cinetime.repository.business.SpecialHallRepository;
import com.cinetime.repository.business.SpecialHallTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialHallServiceTest {

    @org.mockito.Mock private SpecialHallRepository repo;
    @org.mockito.Mock private HallRepository hallRepo;
    @org.mockito.Mock private SpecialHallTypeRepository typeRepo;
    @org.mockito.Mock private HallRepository hallRepository; // for getHallPricing

    private SpecialHallService service;

    @BeforeEach
    void setUp() {
        service = new SpecialHallService(repo, hallRepo, typeRepo, hallRepository);
    }

    @Test
    void list_should_map_page_with_mapper() {
        Hall h = Hall.builder().id(1L).name("Hall A").isSpecial(true).build();
        SpecialHallType t = SpecialHallType.builder().id(10L).name("IMAX").build();
        SpecialHall sh = SpecialHall.builder().id(100L).hall(h).type(t).build();

        when(repo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sh)));
        SpecialHallResponse mapped = SpecialHallResponse.builder()
                .id(100L).hallId(1L).hallName("Hall A").typeId(10L).typeName("IMAX").build();

        try (MockedStatic<SpecialHallMapper> ms = mockStatic(SpecialHallMapper.class)) {
            ms.when(() -> SpecialHallMapper.toResponse(sh)).thenReturn(mapped);
            Page<SpecialHallResponse> page = service.list(Pageable.unpaged());
            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getHallName()).isEqualTo("Hall A");
        }
    }

    @Test
    void get_should_return_mapped_response() {
        Hall h = Hall.builder().id(1L).name("H1").isSpecial(true).build();
        SpecialHallType t = SpecialHallType.builder().id(10L).name("IMAX").build();
        SpecialHall sh = SpecialHall.builder().id(200L).hall(h).type(t).build();

        when(repo.findById(200L)).thenReturn(Optional.of(sh));
        SpecialHallResponse mapped = SpecialHallResponse.builder()
                .id(200L).hallId(1L).typeId(10L).typeName("IMAX").build();

        try (MockedStatic<SpecialHallMapper> ms = mockStatic(SpecialHallMapper.class)) {
            ms.when(() -> SpecialHallMapper.toResponse(sh)).thenReturn(mapped);
            SpecialHallResponse resp = service.get(200L);
            assertThat(resp.getId()).isEqualTo(200L);
        }
    }

    @Test
    void get_should_throw_when_not_found() {
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Special hall not found");
    }

    @Test
    void assign_should_create_and_set_isSpecial_true_if_was_false() {
        SpecialHallRequest req = mock(SpecialHallRequest.class);
        when(req.getHallId()).thenReturn(1L);
        when(req.getTypeId()).thenReturn(10L);

        Hall hall = Hall.builder().id(1L).name("H1").isSpecial(false).build();
        SpecialHallType type = SpecialHallType.builder().id(10L).name("IMAX").build();

        when(hallRepo.findById(1L)).thenReturn(Optional.of(hall));
        when(typeRepo.findById(10L)).thenReturn(Optional.of(type));
        when(repo.findByHallId(1L)).thenReturn(Optional.empty());

        SpecialHall saved = SpecialHall.builder().id(300L).hall(hall).type(type).build();
        when(repo.save(any(SpecialHall.class))).thenReturn(saved);

        SpecialHallResponse mapped = SpecialHallResponse.builder()
                .id(300L).hallId(1L).typeId(10L).typeName("IMAX").build();

        try (MockedStatic<SpecialHallMapper> ms = mockStatic(SpecialHallMapper.class)) {
            ms.when(() -> SpecialHallMapper.toResponse(any(SpecialHall.class)))
                    .thenReturn(mapped);

            SpecialHallResponse resp = service.assign(req);
            assertThat(resp.getId()).isEqualTo(300L);
            verify(hallRepo).updateIsSpecialById(1L, true);
        }
    }

    @Test
    void assign_should_not_update_isSpecial_when_already_true() {
        SpecialHallRequest req = mock(SpecialHallRequest.class);
        when(req.getHallId()).thenReturn(2L);
        when(req.getTypeId()).thenReturn(20L);

        Hall hall = Hall.builder().id(2L).name("H2").isSpecial(true).build();
        SpecialHallType type = SpecialHallType.builder().id(20L).name("4DX").build();

        when(hallRepo.findById(2L)).thenReturn(Optional.of(hall));
        when(typeRepo.findById(20L)).thenReturn(Optional.of(type));
        when(repo.findByHallId(2L)).thenReturn(Optional.empty());

        SpecialHall saved = SpecialHall.builder().id(301L).hall(hall).type(type).build();
        when(repo.save(any(SpecialHall.class))).thenReturn(saved);

        SpecialHallResponse mapped = SpecialHallResponse.builder()
                .id(301L).hallId(2L).typeId(20L).typeName("4DX").build();

        try (MockedStatic<SpecialHallMapper> ms = mockStatic(SpecialHallMapper.class)) {
            ms.when(() -> SpecialHallMapper.toResponse(saved)).thenReturn(mapped);
            service.assign(req);
            verify(hallRepo, never()).updateIsSpecialById(anyLong(), anyBoolean());
        }
    }

    @Test
    void update_should_only_change_type_when_hall_same() {
        Hall hall = Hall.builder().id(1L).name("H1").isSpecial(true).build();
        SpecialHallType oldType = SpecialHallType.builder().id(10L).name("IMAX").build();
        SpecialHall sh = SpecialHall.builder().id(400L).hall(hall).type(oldType).build();

        when(repo.findById(400L)).thenReturn(Optional.of(sh));
        SpecialHallRequest req = mock(SpecialHallRequest.class);
        when(req.getHallId()).thenReturn(1L);
        when(req.getTypeId()).thenReturn(30L);

        SpecialHallType newType = SpecialHallType.builder().id(30L).name("DOLBY").build();
        when(typeRepo.findById(30L)).thenReturn(Optional.of(newType));
        when(repo.save(sh)).thenReturn(sh);

        SpecialHallResponse mapped = SpecialHallResponse.builder()
                .id(400L).hallId(1L).typeId(30L).typeName("DOLBY").build();

        try (MockedStatic<SpecialHallMapper> ms = mockStatic(SpecialHallMapper.class)) {
            ms.when(() -> SpecialHallMapper.toResponse(sh)).thenReturn(mapped);
            SpecialHallResponse resp = service.update(400L, req);
            assertThat(resp.getTypeId()).isEqualTo(30L);
            verify(hallRepo, never()).updateIsSpecialById(anyLong(), anyBoolean());
        }
    }

    @Test
    void update_should_switch_halls_and_flip_flags() {
        Hall oldHall = Hall.builder().id(1L).name("H1").isSpecial(true).build();
        SpecialHallType type = SpecialHallType.builder().id(10L).name("IMAX").build();
        SpecialHall sh = SpecialHall.builder().id(401L).hall(oldHall).type(type).build();

        when(repo.findById(401L)).thenReturn(Optional.of(sh));

        SpecialHallRequest req = mock(SpecialHallRequest.class);
        when(req.getHallId()).thenReturn(2L);
        when(req.getTypeId()).thenReturn(10L);

        Hall newHall = Hall.builder().id(2L).name("H2").isSpecial(false).build();
        when(typeRepo.findById(10L)).thenReturn(Optional.of(type));
        when(hallRepo.findById(2L)).thenReturn(Optional.of(newHall));
        when(repo.save(sh)).thenReturn(sh);

        SpecialHallResponse mapped = SpecialHallResponse.builder()
                .id(401L).hallId(2L).typeId(10L).typeName("IMAX").build();

        try (MockedStatic<SpecialHallMapper> ms = mockStatic(SpecialHallMapper.class)) {
            ms.when(() -> SpecialHallMapper.toResponse(sh)).thenReturn(mapped);
            SpecialHallResponse resp = service.update(401L, req);
            assertThat(resp.getHallId()).isEqualTo(2L);
            verify(hallRepo).updateIsSpecialById(1L, false);
            verify(hallRepo).updateIsSpecialById(2L, true);
        }
    }

    @Test
    void delete_should_remove_and_unset_flag() {
        Hall hall = Hall.builder().id(1L).name("H1").isSpecial(true).build();
        SpecialHallType type = SpecialHallType.builder().id(10L).name("IMAX").build();
        SpecialHall sh = SpecialHall.builder().id(500L).hall(hall).type(type).build();

        when(repo.findById(500L)).thenReturn(Optional.of(sh));

        service.delete(500L);

        verify(repo).delete(sh);
        verify(hallRepo).updateIsSpecialById(1L, false);
    }

    @Test
    void delete_should_throw_when_not_found() {
        when(repo.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(404L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Special hall not found");
    }

    @Test
    void getHallPricing_should_normalize_values() {
        List<Object[]> rows = List.of(
                new Object[]{1L, "A", true,  "IMAX", "12.50", "3.00"},
                new Object[]{2L, "B", 1,     "4DX",  0,       null},
                new Object[]{3L, "C", "t",   null,   null,     "1.75"}
        );
        when(hallRepository.findHallPricingRaw(205L)).thenReturn(rows);

        List<HallPricingResponse> result = service.getHallPricing(205L);
        assertThat(result).hasSize(3);
        assertThat(result.get(0).isSpecial()).isTrue();
        assertThat(result.get(1).getSurchargeFixed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(2).getTypeName()).isNull();
    }
}
