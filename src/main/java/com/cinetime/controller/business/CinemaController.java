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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) Boolean specialHall,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)Pageable pageable) {

     return cinemaService.listCinemas(cityId, cityName, specialHall, pageable);

    }
    @PreAuthorize("permitAll()")
    @GetMapping("/cinemas/with-showtimes")
    public ResponseEntity<?> cinemasWithShowtimes() {
        List<CinemaSummaryResponse> data = cinemaService.cinemasWithShowtimes();
        // If you already wrap with your ResponseMessage, adapt below:
        // return ResponseEntity.ok(new ResponseMessage<>(data, "Cinemas with showtimes", HttpStatus.OK));
        return ResponseEntity.ok(
                new java.util.HashMap<>() {{
                    put("returnBody", data);
                    put("message", "Cinemas with showtimes");
                    put("httpStatus", "OK");
                }}
        );
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/cinemas/with-showtimes-and-images")
    public ResponseEntity<List<CinemaSummaryResponse>> cinemasWithShowtimesAndImages() {
        List<CinemaSummaryResponse> data = cinemaService.cinemasWithShowtimesAndImages();

        // Return only cinemas that have both showtimes and associated images
        return ResponseEntity.ok(data);
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

    //C03: Cinemas Details By id For Dashboard

    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
    @GetMapping("/dashboard/cinemas/{id}")
    public ResponseMessage<CinemaDetailedResponse> getCinemaDetailed(@PathVariable Long id){
        return cinemaService.getDetailedCinemaById(id);
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

    // to delete multiple cinemas at once.
    @DeleteMapping("/cinemas")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseMessage<Void>> deleteMultiple(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(cinemaService.deleteMultiple(ids));
    }


    @PreAuthorize("permitAll()")
    @GetMapping("/cinemas/{cinemaId}/movies")
    public ResponseEntity<List<MovieWithShowtimesResponse>> getMoviesWithShowtimesByCinema(
            @PathVariable Long cinemaId,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate
    ) {
        List<MovieWithShowtimesResponse> response = cinemaService.getMoviesWithShowtimesByCinema(cinemaId, fromDate);
        return ResponseEntity.ok(response);
    }






}
