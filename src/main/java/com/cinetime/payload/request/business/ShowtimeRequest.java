package com.cinetime.payload.request.business;

import com.cinetime.service.validator.showtime.ValidShowtime;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidShowtime
public class ShowtimeRequest {

    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    @NotNull(message = "Start time cannot be null")
    private LocalTime startTime;

    @NotNull(message = "End time cannot be null")
    private LocalTime endTime;

    @NotNull(message = "Hall ID cannot be null")
    private Long hallId;

    @NotNull(message = "Movie ID cannot be null")
    private Long movieId;
}
