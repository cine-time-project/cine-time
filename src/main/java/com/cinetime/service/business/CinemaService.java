package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.payload.response.business.HallMovieShowtimesResponse;
import com.cinetime.payload.response.business.HallWithShowtimesResponse;
import com.cinetime.payload.response.business.MovieMiniResponse;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.HallMovieTimeRow;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.cinetime.entity.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CinemaMapper cinemaMapper;
    private final CinemasHelper cinemasHelper;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;


    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<CinemaSummaryResponse> searchCinemas(Long cityId, Pageable pageable) {
        cinemasHelper.validateCityIfProvided(cityId);
        return cinemaRepository.search(cityId, pageable)
                .map(cinemaMapper::toSummary);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<CinemaSummaryResponse> searchCinemas(Long cityId, String specialHall, Pageable pageable) {
        cinemasHelper.validateCityIfProvided(cityId);
        Boolean isSpecial = cinemasHelper.parseSpecialHall(specialHall); // <-- statik değil, instance
        return cinemaRepository.search(cityId, isSpecial, pageable)
                .map(cinemaMapper::toSummary);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Cinema getById(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CINEMA_NOT_FOUND));
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public CinemaSummaryResponse getCinemaById(Long id) {
        var cinema = getById(id);
        return cinemaMapper.toSummary(cinema);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CinemaSummaryResponse> getAuthFavoritesByLogin(String login, Pageable pageable) {
        Long userId = userRepository.findByLoginProperty(login)
                .map(User::getId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessages.NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD));

        return cinemaRepository.findFavoriteCinemasByUserId(userId, pageable)
                .map(cinemaMapper::toSummary)
                .getContent();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<HallWithShowtimesResponse> getCinemaHallsWithShowtimes(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new ResourceNotFoundException(ErrorMessages.CINEMA_NOT_FOUND);
        }

        var rows = showtimeRepository.findShowtimesByCinemaId(cinemaId);

        // hallId -> HallWithShowtimesResponse
        Map<Long, HallWithShowtimesResponse> halls = new LinkedHashMap<>();

        rows.forEach(r -> {
            var hall = halls.computeIfAbsent(r.getHallId(), id ->
                    HallWithShowtimesResponse.builder()
                            .id(r.getHallId())
                            .name(r.getHallName())
                            .seatCapacity(r.getSeatCapacity())
                            .isSpecial(r.getIsSpecial())
                            .movies(new ArrayList<>())
                            .build()
            );

            // aynı hall içindeki film grubu
            var movieGroup = hall.getMovies().stream()
                    .filter(mg -> mg.getMovie().getId().equals(r.getMovieId()))
                    .findFirst()
                    .orElseGet(() -> {
                        var mg = HallMovieShowtimesResponse.builder()
                                .movie(MovieMiniResponse.builder()
                                        .id(r.getMovieId())
                                        .title(r.getMovieTitle())
                                        .build())
                                .times(new ArrayList<>())
                                .build();
                        hall.getMovies().add(mg);
                        return mg;
                    });

            // date + startTime -> LocalDateTime
            movieGroup.getTimes().add(LocalDateTime.of(r.getDate(), r.getStartTime()));
        });

        // seans saatlerini sırala
        halls.values().forEach(h ->
                h.getMovies().forEach(m ->
                        m.setTimes(m.getTimes().stream().sorted().collect(Collectors.toList()))
                )
        );

        return new ArrayList<>(halls.values());
    }




}
