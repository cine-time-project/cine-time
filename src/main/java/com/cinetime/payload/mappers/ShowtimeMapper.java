package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.ShowtimeResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ShowtimeMapper {

    public Showtime mapRequestToShowtime(ShowtimeRequest request, Hall hall, Movie movie) {
        return Showtime.builder()
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .hall(hall)
                .movie(movie)
                .build();
    }


    public ShowtimeResponse mapShowtimeToResponse(Showtime showtime) {
        if (showtime == null) return null;

        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .date(showtime.getDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                // Hall / Cinema data
                .hallId(showtime.getHall() != null ? showtime.getHall().getId() : null)
                .hallName(showtime.getHall() != null ? showtime.getHall().getName() : null)
                .cinemaId(showtime.getHall() != null && showtime.getHall().getCinema() != null
                        ? showtime.getHall().getCinema().getId() : null)
                .cinemaName(showtime.getHall() != null && showtime.getHall().getCinema() != null
                        ? showtime.getHall().getCinema().getName() : null)
                // Movie data
                .movieId(showtime.getMovie() != null ? showtime.getMovie().getId() : null)
                .movieTitle(showtime.getMovie() != null ? showtime.getMovie().getTitle() : null)
                .build();
    }

    public Page<ShowtimeResponse> mapToResponsePage(Page<Showtime> showtimes) {
        return showtimes.map(this::mapShowtimeToResponse);
    }
}
