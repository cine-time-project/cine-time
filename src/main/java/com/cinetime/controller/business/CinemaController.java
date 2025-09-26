package com.cinetime.controller.business;

import com.cinetime.payload.request.business.CinemaCreateRequest;
import com.cinetime.payload.response.business.SpecialHallResponse;
import com.cinetime.payload.response.business.*;
import com.cinetime.service.business.CinemaService;
import com.cinetime.service.helper.PageableHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
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

    //C01: Cinemas based on city and sipecialHalls
    @PreAuthorize("permitAll()")
    @GetMapping("/cinemas")
    public ResponseMessage<Page<CinemaSummaryResponse>> listCinemas(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Boolean specialHall,
            @PageableDefault(page = 0, size = 10, sort = "title", direction = Sort.Direction.ASC)Pageable pageable) {

     return cinemaService.listCinemas(cityId, specialHall, pageable);

    }

    //C02: Get Users Favorites
    @PreAuthorize("hasAuthority('MEMBER')")     //<= ***changed***
    @GetMapping("/favorites/auth")
    public ResponseEntity<ResponseMessage<Page<CinemaSummaryResponse>>> getAuthFavorites(
            org.springframework.security.core.Authentication authentication,
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        String login = authentication.getName();
        return ResponseEntity.ok(cinemaService.getAuthFavoritesByLogin(login, pageable));
    }

    //C03: Cinemas Details By id
    @GetMapping("/cinemas/{id}")
    public ResponseEntity<ResponseMessage<CinemaSummaryResponse>> getCinema(@PathVariable Long id){
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }


    // C04: get cinemas Halls
    @GetMapping("/cinemas/{id}/halls")
    public ResponseEntity<List<HallWithShowtimesResponse>> getCinemaHalls(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.getCinemaHallsWithShowtimes(id));
    }

    //C05: All of the Special Halls
    @GetMapping("/special-halls")
    public ResponseEntity<ResponseMessage<List<SpecialHallResponse>>> getSpecialHalls(){
        return ResponseEntity.ok(cinemaService.getAllSpecialHalls());
    }

    //C06: Create Cinema
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/cinemas")
    public ResponseEntity<ResponseMessage<CinemaSummaryResponse>> create(@Valid @RequestBody CinemaCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(cinemaService.createCinema(request));
    }

    //C07: Cinema Update
    @PutMapping("/cinemas/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")  //<= ***changed***
    public ResponseEntity<ResponseMessage<CinemaSummaryResponse>> updateCinema(
            @PathVariable Long id,
            @Valid @RequestBody CinemaCreateRequest request) {

        return ResponseEntity.ok(cinemaService.update(id, request));
    }

    //C08: Delete Cinema
    @DeleteMapping("/cinemas/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")  // <= ***changed***
    public ResponseEntity<ResponseMessage<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.delete(id));
    }

}
