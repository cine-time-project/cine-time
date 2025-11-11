package com.cinetime.controller.business;

import com.cinetime.payload.request.business.SpecialHallRequest;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.service.business.SpecialHallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/special-hall-assignments")
@RequiredArgsConstructor
public class SpecialHallController {
    private final SpecialHallService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseEntity<Page<SpecialHallResponse>> list(
            @RequestParam(defaultValue="0")  int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="id,asc") String sort
    ){
        Pageable p = pageRequest(page, size, sort);
        return ResponseEntity.ok(service.list(p));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseEntity<SpecialHallResponse> get(@PathVariable Long id){
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialHallResponse> assign(@Valid @RequestBody SpecialHallRequest req){
        return ResponseEntity.ok(service.assign(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialHallResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody SpecialHallRequest req){
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable pageRequest(int page, int size, String sort) {
        String[] parts = sort.split(",", 2);
        String field = parts[0];
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]);
        Sort s = desc ? Sort.by(field).descending() : Sort.by(field).ascending();
        return PageRequest.of(page, size, s);
    }
}
