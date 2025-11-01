package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Hall;
import com.cinetime.entity.business.Movie;
import com.cinetime.entity.business.Showtime;
import com.cinetime.payload.request.business.ShowtimeRequest;
import com.cinetime.payload.response.business.ShowtimeResponse;
import com.cinetime.payload.response.business.ShowtimeSimpleResponse;
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

    public ShowtimeSimpleResponse toSimpleResponse(Showtime showtime) {
        return ShowtimeSimpleResponse.builder()
                .id(showtime.getId())
                .date(showtime.getDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .moviePosterUrl(showtime.getMovie().getPosterUrl())
                .build();
    }


    public ShowtimeResponse mapShowtimeToResponse(Showtime showtime) {
        if (showtime == null) return null;

        // Unwrap associations defensively
        Hall hall = showtime.getHall();
        var cinema = (hall != null) ? hall.getCinema() : null;
        var city = (cinema != null) ? cinema.getCity() : null;
        var country = (city != null) ? city.getCountry() : null;

        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .date(showtime.getDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                // Hall / Cinema data
                .hallId(hall != null ? hall.getId() : null)
                .hallName(hall != null ? hall.getName() : null)
                .cinemaId(cinema != null ? cinema.getId() : null)
                .cinemaName(cinema != null ? cinema.getName() : null)
                // Movie data
                .movieId(showtime.getMovie() != null ? showtime.getMovie().getId() : null)
                .movieTitle(showtime.getMovie() != null ? showtime.getMovie().getTitle() : null)
                // NEW: City / Country
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getName() : null)
                .countryId(country != null ? country.getId() : null)
                .countryName(country != null ? country.getName() : null)
                .build();
    }

    public Page<ShowtimeResponse> mapToResponsePage(Page<Showtime> showtimes) {
        return showtimes.map(this::mapShowtimeToResponse);
    }

    public void updateShowtimeFromRequest(Showtime showtime, ShowtimeRequest request, Hall hall, Movie movie) {
        if (request.getDate() != null) {
            showtime.setDate(request.getDate());
        }
        if (request.getStartTime() != null) {
            showtime.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            showtime.setEndTime(request.getEndTime());
        }
        if (hall != null) {
            showtime.setHall(hall);
        }
        if (movie != null) {
            showtime.setMovie(movie);
        }
    }


    public static class ShowtimeFlatRow {
        public Long showtimeId;
        public java.time.LocalDate date;
        public java.time.LocalTime startTime;
        public java.time.LocalTime endTime;
        public Long hallId;
        public String hallName;
        public Long movieId;
        public String movieTitle;
        public Integer duration;

        public ShowtimeFlatRow(Long showtimeId, java.time.LocalDate date,
                               java.time.LocalTime startTime, java.time.LocalTime endTime,
                               Long hallId, String hallName,
                               Long movieId, String movieTitle, Integer duration) {
            this.showtimeId = showtimeId;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.hallId = hallId;
            this.hallName = hallName;
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.duration = duration;
        }
    }

    // Entity → Flat Row
    public ShowtimeFlatRow toFlatRow(Showtime st) {
        if (st == null) return null;
        var h = st.getHall();
        var m = st.getMovie();
        return new ShowtimeFlatRow(
                st.getId(),
                st.getDate(),
                st.getStartTime(),
                st.getEndTime(),
                h != null ? h.getId() : null,
                h != null ? h.getName() : null,
                m != null ? m.getId() : null,
                m != null ? m.getTitle() : null,
                m != null ? m.getDuration() : null
        );
    }


}
