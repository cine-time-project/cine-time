package com.cinetime.controller.business;

import com.cinetime.payload.request.business.HallRequest;
import com.cinetime.payload.response.business.HallResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hall") // <-- tek base path
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Transactional
    public ResponseMessage<HallResponse> saveHall(
            @RequestBody @Valid HallRequest hallRequest) {
        return hallService.saveHall(hallRequest);
    }

    @GetMapping("/{hallId}")
    @PreAuthorize("permitAll()")
    @Transactional(readOnly = true)
    public ResponseMessage<HallResponse> getHallById(@PathVariable Long hallId){
        return hallService.getHallById(hallId);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Transactional(readOnly = true)
    public ResponseMessage<Page<HallResponse>> getAllHalls(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ){
        return hallService.getAllHalls(pageable);
    }

    @DeleteMapping("/del/{hallId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<HallResponse> deleteHallById(@PathVariable Long hallId){
        return hallService.deleteHallById(hallId);
    }


}
