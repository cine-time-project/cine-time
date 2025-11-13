package com.cinetime.service.business;

import com.cinetime.entity.business.SpecialHallType;
import com.cinetime.payload.mappers.SpecialHallTypeMapper;
import com.cinetime.payload.request.business.SpecialHallTypeRequest;
import com.cinetime.payload.response.business.SpecialHallTypeResponse;
import com.cinetime.repository.business.SpecialHallTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class SpecialHallTypeServiceTest {

    @Mock
    private SpecialHallTypeRepository repo;

    private SpecialHallTypeService service;

    private SpecialHallTypeService createService() {
        // @RequiredArgsConstructor takes a single parameter
        return new SpecialHallTypeService(repo);
    }

    // ========= list =========
    @Test
    void list_shouldReturnMappedPage() {
        service = createService();

        SpecialHallType ent = new SpecialHallType();
        ent.setId(1L);

        Page<SpecialHallType> page = new PageImpl<>(List.of(ent));
        when(repo.findAll(any(Pageable.class))).thenReturn(page);

        SpecialHallTypeResponse dto = new SpecialHallTypeResponse();
        dto.setId(1L);

        try (MockedStatic<SpecialHallTypeMapper> st = mockStatic(SpecialHallTypeMapper.class)) {
            st.when(() -> SpecialHallTypeMapper.toResponse(ent)).thenReturn(dto);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
            Page<SpecialHallTypeResponse> result = service.list(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(1L, result.getContent().get(0).getId());
            verify(repo).findAll(pageable);
        }
    }

    // ========= get =========
    @Test
    void get_shouldReturnResponse_whenFound() {
        service = createService();

        SpecialHallType ent = new SpecialHallType();
        ent.setId(5L);

        when(repo.findById(5L)).thenReturn(Optional.of(ent));

        SpecialHallTypeResponse dto = new SpecialHallTypeResponse();
        dto.setId(5L);

        try (MockedStatic<SpecialHallTypeMapper> st = mockStatic(SpecialHallTypeMapper.class)) {
            st.when(() -> SpecialHallTypeMapper.toResponse(ent)).thenReturn(dto);

            SpecialHallTypeResponse resp = service.get(5L);

            assertNotNull(resp);
            assertEquals(5L, resp.getId());
            verify(repo).findById(5L);
        }
    }

    @Test
    void get_shouldThrowNotFound_whenMissing() {
        service = createService();

        when(repo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.get(99L));

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Type not found"));
    }

    // ========= create =========
    @Test
    void create_shouldSave_whenNameUnique() {
        service = createService();

        SpecialHallTypeRequest req = new SpecialHallTypeRequest();
        req.setName("IMAX");

        // name does not exist yet
        when(repo.findByNameIgnoreCase("IMAX")).thenReturn(Optional.empty());

        SpecialHallType entToSave = new SpecialHallType();
        entToSave.setId(10L);

        SpecialHallType savedEnt = new SpecialHallType();
        savedEnt.setId(10L);

        SpecialHallTypeResponse dto = new SpecialHallTypeResponse();
        dto.setId(10L);
        dto.setName("IMAX");

        try (MockedStatic<SpecialHallTypeMapper> st = mockStatic(SpecialHallTypeMapper.class)) {
            st.when(() -> SpecialHallTypeMapper.toEntity(req)).thenReturn(entToSave);
            st.when(() -> SpecialHallTypeMapper.toResponse(savedEnt)).thenReturn(dto);

            when(repo.save(entToSave)).thenReturn(savedEnt);

            SpecialHallTypeResponse resp = service.create(req);

            assertNotNull(resp);
            assertEquals(10L, resp.getId());
            assertEquals("IMAX", resp.getName());
            verify(repo).findByNameIgnoreCase("IMAX");
            verify(repo).save(entToSave);
        }
    }

    @Test
    void create_shouldThrowConflict_whenNameAlreadyExists() {
        service = createService();

        SpecialHallTypeRequest req = new SpecialHallTypeRequest();
        req.setName("4DX");

        SpecialHallType existing = new SpecialHallType();
        existing.setId(1L);

        when(repo.findByNameIgnoreCase("4DX")).thenReturn(Optional.of(existing));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(req));

        assertEquals(CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Type name already exists"));
        verify(repo).findByNameIgnoreCase("4DX");
        verify(repo, never()).save(any());
    }

    // ========= update =========
    @Test
    void update_shouldModifyAndSave_whenValid() {
        service = createService();

        Long id = 1L;
        SpecialHallTypeRequest req = new SpecialHallTypeRequest();
        req.setName("Dolby Atmos");

        SpecialHallType ent = new SpecialHallType();
        ent.setId(id);
        ent.setName("OLD");

        when(repo.findById(id)).thenReturn(Optional.of(ent));
        // same entity with the same name → no conflict
        when(repo.findByNameIgnoreCase("Dolby Atmos")).thenReturn(Optional.of(ent));

        SpecialHallTypeResponse dto = new SpecialHallTypeResponse();
        dto.setId(id);
        dto.setName("Dolby Atmos");

        try (MockedStatic<SpecialHallTypeMapper> st = mockStatic(SpecialHallTypeMapper.class)) {
            st.when(() -> SpecialHallTypeMapper.update(ent, req)).thenAnswer(inv -> null);
            st.when(() -> SpecialHallTypeMapper.toResponse(ent)).thenReturn(dto);

            when(repo.save(ent)).thenReturn(ent);

            SpecialHallTypeResponse resp = service.update(id, req);

            assertNotNull(resp);
            assertEquals(id, resp.getId());
            assertEquals("Dolby Atmos", resp.getName());

            verify(repo).findById(id);
            verify(repo).findByNameIgnoreCase("Dolby Atmos");
            verify(repo).save(ent);
        }
    }

    @Test
    void update_shouldThrowNotFound_whenIdMissing() {
        service = createService();

        when(repo.findById(123L)).thenReturn(Optional.empty());

        SpecialHallTypeRequest req = new SpecialHallTypeRequest();
        req.setName("Something");

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.update(123L, req));

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Type not found"));
        verify(repo).findById(123L);
    }

    @Test
    void update_shouldThrowConflict_whenNameUsedByAnotherType() {
        service = createService();

        Long id = 1L;
        SpecialHallTypeRequest req = new SpecialHallTypeRequest();
        req.setName("VIP");

        SpecialHallType current = new SpecialHallType();
        current.setId(id);

        SpecialHallType other = new SpecialHallType();
        other.setId(2L); // different id → conflict

        when(repo.findById(id)).thenReturn(Optional.of(current));
        when(repo.findByNameIgnoreCase("VIP")).thenReturn(Optional.of(other));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.update(id, req));

        assertEquals(CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Type name already exists"));

        verify(repo).findById(id);
        verify(repo).findByNameIgnoreCase("VIP");
        verify(repo, never()).save(any());
    }

    // ========= delete =========
    @Test
    void delete_shouldRemove_whenExists() {
        service = createService();

        when(repo.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repo).existsById(5L);
        verify(repo).deleteById(5L);
    }

    @Test
    void delete_shouldThrowNotFound_whenNotExists() {
        service = createService();

        when(repo.existsById(5L)).thenReturn(false);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.delete(5L));

        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Type not found"));
        verify(repo).existsById(5L);
        verify(repo, never()).deleteById(anyLong());
    }
}
