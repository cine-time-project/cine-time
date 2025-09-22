package com.cinetime.controller.business;

import com.cinetime.payload.request.business.CinemaCreateRequest;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.payload.response.business.*;
import com.cinetime.service.business.CinemaService;
import com.cinetime.service.helper.PageableHelper;
import com.cinetime.payload.messages.SuccessMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // <-- tek base path
@RequiredArgsConstructor
@Validated
public class CinemaController {

    private final CinemaService cinemaService;
    private final PageableHelper pageableHelper;

    @PreAuthorize("permitAll()")
    @GetMapping("/cinemas")
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


    @GetMapping("/cinemas/{id}")
    public ResponseEntity<CinemaSummaryResponse> getCinema(@PathVariable Long id){
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }

    @PreAuthorize("hasAuthority('MEMBER')")           // ROLE_ prefix gerekmeden çalışır
    @GetMapping("/favorites/auth")
    public ResponseEntity<List<CinemaSummaryResponse>> getAuthFavorites(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc")  String type,
            org.springframework.security.core.Authentication authentication
    ) {
        String login = authentication.getName();       // token’dan gelen username (email/telefon)

        Pageable pageable = (pageableHelper != null)
                ? pageableHelper.buildPageable(page, size, sort, type)
                : PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                "desc".equalsIgnoreCase(type) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort
        );

        return ResponseEntity.ok(cinemaService.getAuthFavoritesByLogin(login, pageable));
    }

    @GetMapping("/cinemas/{id}/halls")
    public ResponseEntity<List<HallWithShowtimesResponse>> getCinemaHalls(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.getCinemaHallsWithShowtimes(id));
    }

    @GetMapping("/special-halls")
    public ResponseEntity<List<SpecialHallResponse>> getSpecialHalls(){
        return ResponseEntity.ok(cinemaService.getAllSpecialHalls());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/cinemas")
    public ResponseEntity<CinemaSummaryResponse>create(@Valid @RequestBody CinemaCreateRequest request){
        CinemaSummaryResponse response=cinemaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/cinemas/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CinemaSummaryResponse> updateCinema(
            @PathVariable Long id,
            @Valid @RequestBody CinemaCreateRequest request
    ){
        return ResponseEntity.ok(cinemaService.update(id, request));
    }

}
