package com.cinetime.controller.business;

import com.cinetime.payload.response.business.*;
import com.cinetime.service.business.CinemaService;
import com.cinetime.service.helper.PageableHelper;
import com.cinetime.payload.messages.SuccessMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
@Validated
public class CinemaController {

    private final CinemaService cinemaService;
    private final PageableHelper pageableHelper;

    // @PreAuthorize("permitAll()") // Anonymous + MEMBER + EMPLOYEE + ADMIN
    @GetMapping
    public ResponseEntity<ResponseMessage<PageResponse<CinemaSummaryResponse>>> listCinemas(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String specialHall,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String type
    ) {
        Pageable pageable = pageableHelper.buildPageable(page, size, sort, type);
        Page<CinemaSummaryResponse> result = cinemaService.searchCinemas(cityId, specialHall, pageable);

        PageResponse<CinemaSummaryResponse> body = PageResponse.of(result, sort, type);

        ResponseMessage<PageResponse<CinemaSummaryResponse>> response =
                ResponseMessage.<PageResponse<CinemaSummaryResponse>>builder()
                        .returnBody(body)
                        .message(SuccessMessages.CINEMAS_LISTED)
                        .httpStatus(HttpStatus.OK)
                        .build();

        return ResponseEntity.ok(response);
    }
}
