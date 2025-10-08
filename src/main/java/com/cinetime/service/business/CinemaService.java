package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.City;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.mappers.HallMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.business.CinemaCreateRequest;
import com.cinetime.payload.response.business.*;
import com.cinetime.repository.business.*;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.cinetime.entity.user.User;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CinemaMapper cinemaMapper;
    private final CinemasHelper cinemasHelper;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final HallRepository hallRepository;
    private final HallMapper hallMapper;
    private final CityRepository cityRepository;
    private final TicketRepository ticketRepository;

    public List<CinemaSummaryResponse> cinemasWithShowtimes() {
        return cinemaRepository.findCinemasWithUpcomingShowtimes();
    }
    public Page<CinemaSummaryResponse> searchCinemas(Long cityId, Pageable pageable) {
        cinemasHelper.validateCityIfProvided(cityId);
        return cinemaRepository.search(cityId, pageable)
                .map(cinemaMapper::toSummary);
    }

    //C01: Cinemas based on city and sipecialHalls
    public ResponseMessage<Page<CinemaSummaryResponse>> listCinemas(Long cityId, Boolean isSpecial,
                                                                    Pageable pageable) {

        Page<Cinema> cinemas = cinemaRepository.search(cityId, isSpecial, pageable);
        Page<CinemaSummaryResponse> dtoPage = cinemas.map(cinemaMapper::toSummary);
        return ResponseMessage.<Page<CinemaSummaryResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.CINEMAS_LISTED)
                .returnBody(dtoPage) // <-- Page<CinemaSummaryResponse>
                .build();
    }


    public Set<Cinema> getAllByIdIn(Set<Long> ids) {
        Set<Cinema> cinemas = cinemaRepository.findAllByIdIn(ids);
        if (cinemas.isEmpty()) throw new ResourceNotFoundException(ErrorMessages.CINEMA_NOT_FOUND);
        return cinemas;
    }

    public Cinema getById(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CINEMA_NOT_FOUND));
    }

    //C03: Cinemas Details By id
    public ResponseMessage<CinemaSummaryResponse> getCinemaById(Long id) {
        var cinema = getById(id);
        return ResponseMessage.<CinemaSummaryResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(String.format(SuccessMessages.CINEMA_FETCHED, id))// yoksa ekleyin; geçici: "Cinema fetched"
                .returnBody(cinemaMapper.toSummary(cinema))
                .build();
    }

    //C02: Get Users Favorites
    public ResponseMessage<Page<CinemaSummaryResponse>> getAuthFavoritesByLogin(String login, Pageable pageable) {
        Long userId = userRepository.findByLoginProperty(login)
                .map(User::getId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessages.NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD));

        Page<Cinema> page = cinemaRepository.findFavoriteCinemasByUserId(userId, pageable);

        Page<CinemaSummaryResponse> dtoPage = page.map(cinemaMapper::toSummary);

        return ResponseMessage.<Page<CinemaSummaryResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.FAVORITES_LISTED)
                .returnBody(dtoPage)
                .build();
    }

    //C04: get cinemas Halls
    public List<HallWithShowtimesResponse> getCinemaHallsWithShowtimes(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new ResourceNotFoundException(String.format(ErrorMessages.CINEMA_NOT_FOUND, cinemaId));
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
                h.getMovies().forEach(m -> m.getTimes().sort(Comparator.naturalOrder()))
        );

        return new ArrayList<>(halls.values());
    }

    //C05: All of the Special Halls
    public ResponseMessage<List<SpecialHallResponse>> getAllSpecialHalls() {
        List<SpecialHallResponse> body = hallRepository.findByIsSpecialTrueOrderByNameAsc()
                .stream()
                .map(hallMapper::toSpecial)
                .toList();

        return ResponseMessage.<List<SpecialHallResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.SPECIAL_HALLS_LISTED)
                .returnBody(body)
                .build();
    }


    //C06: Create Cinema
    @Transactional
    public ResponseMessage<CinemaSummaryResponse> createCinema(@Valid CinemaCreateRequest request) {
        final String name = request.getName().trim();
        String baseSlug = (request.getSlug() == null || request.getSlug().isBlank())
                ? cinemasHelper.slugify(name)
                : cinemasHelper.slugify(request.getSlug());
        String uniqueSlug = cinemasHelper.ensureUniqueSlug(baseSlug);
        Long cityId = request.getCityId();
        if (cityId == null) {
            throw new ResourceNotFoundException(ErrorMessages.CITY_NOT_FOUND);
        }
            City city = cityRepository.findById(cityId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CITY_NOT_FOUND + " " + cityId));

            Cinema cinema = Cinema.builder()
                    .name(name)
                    .slug(uniqueSlug)
                    .city(city)
                    .build();


            var dto = cinemaMapper.toSummary(cinemaRepository.save(cinema));
            return ResponseMessage.<CinemaSummaryResponse>builder()
                    .httpStatus(HttpStatus.CREATED)
                    .message(SuccessMessages.CINEMA_CREATED)
                    .returnBody(dto)
                    .build();
        }


    //C07: Cinema Update
    @Transactional
    public ResponseMessage<CinemaSummaryResponse> update(Long id, CinemaCreateRequest req) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format(ErrorMessages.CINEMA_NOT_FOUND, String.valueOf(id))
                        )
                );

        // 1) Name
        if (req.getName() != null && !req.getName().isBlank()) {
            cinema.setName(req.getName().trim());
        }

        // 2) Slug (name değiştiyse veya slug verildiyse)
        if ((req.getName() != null && !req.getName().isBlank())
                || (req.getSlug() != null && !req.getSlug().isBlank())) {

            String base = (req.getSlug() == null || req.getSlug().isBlank())
                    ? cinemasHelper.slugify(cinema.getName())
                    : cinemasHelper.slugify(req.getSlug());

            String unique = base;
            int k = 2;
            while (cinemaRepository.existsBySlugIgnoreCaseAndIdNot(unique, id)) {
                unique = base + "-" + k++;
            }
            cinema.setSlug(unique);
        }

        // 3) City (single, required model)
        if (req.getCityId() != null) {
            Long newCityId = req.getCityId();
            City newCity = cityRepository.findById(newCityId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CITY_NOT_FOUND + " " + newCityId));
            cinema.setCity(newCity);
        }

        var saved = cinemaRepository.save(cinema);
        var dto   = cinemaMapper.toSummary(saved);

        return ResponseMessage.<CinemaSummaryResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(String.format(SuccessMessages.CINEMA_UPDATED,id))
                .returnBody(dto)
                .build();
    }

    //C08 : Delete Cinema
    @Transactional
    public ResponseMessage<Void> delete(Long id) {
        // 1) Id kontrol
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(ErrorMessages.CINEMA_NOT_FOUND, id)));

        // 2) Relation clear
        cinema.getMovies().clear();


        // 3) Tek satır: cascade zinciri halleder
        cinemaRepository.delete(cinema);

        // 4) Response
        return ResponseMessage.<Void>builder()
                .httpStatus(HttpStatus.OK)
                .message(String.format(SuccessMessages.CINEMA_DELETED, id))
                .returnBody(null)
                .build();
    }


}