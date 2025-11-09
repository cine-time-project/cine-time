package com.cinetime.controller.business;

import com.cinetime.payload.request.business.SpecialHallTypeRequest;
import com.cinetime.payload.response.business.SpecialHallTypeResponse;
import com.cinetime.service.business.SpecialHallTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/special-hall-types")
@RequiredArgsConstructor
public class SpecialHallTypeController {
    private final SpecialHallTypeService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')") // <-- düzeltildi
    public ResponseEntity<Page<SpecialHallTypeResponse>> list(
            @RequestParam(defaultValue="0")  int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="id,asc") String sort
    ){
        Pageable p = pageRequest(page, size, sort);
        return ResponseEntity.ok(service.list(p));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')") // <-- düzeltildi
    public ResponseEntity<SpecialHallTypeResponse> get(@PathVariable Long id){
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialHallTypeResponse> create(@Valid @RequestBody SpecialHallTypeRequest req){
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialHallTypeResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody SpecialHallTypeRequest req){
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- yardımcı
    private Pageable pageRequest(int page, int size, String sort) {
        String[] parts = sort.split(",", 2);
        String field = parts[0];
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]);
        Sort s = desc ? Sort.by(field).descending() : Sort.by(field).ascending();
        return PageRequest.of(page, size, s);
    }
}
