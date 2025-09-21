package com.cinetime.controller.business;

import com.cinetime.payload.request.business.HallRequest;
import com.cinetime.payload.response.business.HallResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.business.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
