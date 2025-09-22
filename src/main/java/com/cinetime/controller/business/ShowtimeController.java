package com.cinetime.controller.business;

import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.business.ShowtimeResponse;
import com.cinetime.service.business.ShowtimeService;
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
@RequestMapping("/api/show-time") // <-- tek base path
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showTimeService;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<ShowtimeResponse> saveShowtime(
            @RequestBody @Valid ShowtimeRequest showtimeRequest) {
        return showTimeService.saveShowtime(showtimeRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<ShowtimeResponse> deleteShowtimeById(@PathVariable Long id) {
        return showTimeService.deleteShowtimeById(id);
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


}
