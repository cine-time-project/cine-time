package com.cinetime.controller.business;

import com.cinetime.payload.mappers.ShowtimeMapper;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.*;
import com.cinetime.service.business.ShowtimeService;
import com.cinetime.service.business.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/show-times") // <--Changed for multi base path
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showTimeService;
    private final TicketService ticketService;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<ShowtimeResponse> saveShowtime(
            @RequestBody @Valid ShowtimeRequest showtimeRequest) {
        return showTimeService.saveShowtime(showtimeRequest);
    }

    // ShowtimesController.java
    @GetMapping("/unavailable-seats")
    @PreAuthorize("permitAll()")
    public List<String> unavailableSeatsByFields(
            @RequestParam String movieName,
            @RequestParam String hall,
            @RequestParam String cinema,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime showtime
    ) {
        return ticketService.getTakenSeatsByFields(movieName, hall, cinema, date, showtime);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtimeById(@PathVariable Long id) {
        showTimeService.deleteShowtimeById(id);
        return ResponseEntity.noContent().build(); // 204
    }


    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseMessage<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        return showTimeService.getShowtimeById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Transactional
    public ResponseMessage<ShowtimeResponse> updateShowtimeById(
            @PathVariable Long id,
            @RequestBody @Valid ShowtimeRequest showtimeRequest) {
        return showTimeService.updateShowtimeById(id, showtimeRequest);
    }

    @GetMapping("/movie/{movieId}")
    @PreAuthorize("permitAll()")
    public ResponseMessage<Page<ShowtimeResponse>> getShowtimesByMovieId(
            @PathVariable Long movieId,
            @PageableDefault(page = 0, size = 10, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        return showTimeService.getShowtimesByMovieId(movieId, pageable);
    }

    // S01 Endpoint - Get showtimes by cinema ID
    @GetMapping("/cinema/{cinemaId}")
    @PreAuthorize("permitAll()")
    public ResponseMessage<List<HallWithShowtimesResponse>> getShowtimesByCinemaId(@PathVariable Long cinemaId) {
        return showTimeService.getShowtimesByCinemaId(cinemaId);
    }

    // /api/show-times/cities-with-showtimes?onOrAfter=YYYY-MM-DD&movieId=..&countryId=..
    @Transactional(readOnly = true)
    @GetMapping("/cities-with-showtimes")
    @PreAuthorize("permitAll()")
    public ResponseMessage<List<CityMiniResponse>> getCitiesWithShowtimes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onOrAfter,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false, name = "countryId") Long countryId
    ) {
        if (countryId != null) {
            return showTimeService.getCitiesWithShowtimesByCountry(onOrAfter, movieId, countryId);
        }
        return showTimeService.getCitiesWithShowtimes(onOrAfter, movieId);
    }



    @Transactional(readOnly = true)
    @GetMapping("/countries-with-showtimes")
    @PreAuthorize("permitAll()")
    public ResponseMessage<List<CountryMiniResponse>> getCountriesWithShowtimes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onOrAfter,
            @RequestParam(required = false) Long movieId
    ) {
        return showTimeService.getCountriesWithShowtimes(onOrAfter, movieId);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseMessage<Page<ShowtimeResponse>> getAllShowtimes(
            @PageableDefault(page = 0, size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long hallId,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return showTimeService.getAllShowtimes(pageable, cinemaId, hallId, movieId, dateFrom, dateTo);
    }

    @GetMapping("/cinema/{cinemaId}/flat")
    @PreAuthorize("permitAll()")
    public List<ShowtimeMapper.ShowtimeFlatRow> getFlatByCinema(@PathVariable Long cinemaId) {
        return showTimeService.findFlatByCinemaId(cinemaId);
    }

}
