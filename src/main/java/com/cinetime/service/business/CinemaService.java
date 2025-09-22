package com.cinetime.service.business;

import com.cinetime.entity.business.Cinema;
import com.cinetime.entity.business.City;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.mappers.HallMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.request.business.CinemaCreateRequest;
import com.cinetime.payload.response.business.*;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.CityRepository;
import com.cinetime.repository.business.HallRepository;
import com.cinetime.repository.business.ShowtimeRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.cinetime.entity.user.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public Set<Cinema> getAllByIdIn(Set<Long> ids) {
        Set<Cinema> cinemas = cinemaRepository.findAllByIdIn(ids);
        if (cinemas.isEmpty()) throw new ResourceNotFoundException(ErrorMessages.CINEMA_NOT_FOUND);
        return cinemas;
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


    public List<SpecialHallResponse> getAllSpecialHalls() {
        return hallRepository.findByIsSpecialTrueOrderByNameAsc()
                .stream()
                .map(hallMapper::toSpecial)
                .toList();

    }

    @Transactional
    public CinemaSummaryResponse create(CinemaCreateRequest request) {
        // 1) Slug hazır geldiyse temizle; gelmediyse isimden üret
        String baseSlug = (request.getSlug() == null || request.getSlug().isBlank())
                ? cinemasHelper.slugify(request.getName())
                : cinemasHelper.slugify(request.getSlug());
        // 2) Slug must be unique
        String uniqueSlug = cinemasHelper.ensureUniqueSlug(baseSlug);

        // 3) Entity
        Cinema cinema = Cinema.builder()
                .name(request.getName().trim())
                .slug(uniqueSlug)
                .build();

        // 4) City association +ID control
        if (request.getCityIds() != null && !request.getCityIds().isEmpty()) {
            Set<Long> requestedIds = new LinkedHashSet<>(request.getCityIds());

            // DB
            Set<City> cities = new LinkedHashSet<>(cityRepository.findAllById(requestedIds));
            Set<Long> foundIds = cities.stream().map(City::getId).collect(java.util.stream.Collectors.toSet());

            // Eksikler = istenen - bulunan
            requestedIds.removeAll(foundIds);
            if (!requestedIds.isEmpty()) {
                throw new ResourceNotFoundException(ErrorMessages.CITY_NOT_FOUND + requestedIds);
            }

            cinema.setCities(cities);
        }

        // 5) Save & map
        Cinema saved = cinemaRepository.save(cinema);
        return cinemaMapper.toSummary(saved);
    }

    @Transactional
    public CinemaSummaryResponse update(Long id, CinemaCreateRequest req) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CINEMA_NOT_FOUND));

        // 1) Name
        if (req.getName() != null && !req.getName().isBlank()) {
            cinema.setName(req.getName().trim());
        }

        // 2) Slug (name or provided from slug)
        if ((req.getName() != null && !req.getName().isBlank()) || (req.getSlug() != null)) {
            String base = (req.getSlug() == null || req.getSlug().isBlank())
                    ? cinemasHelper.slugify(cinema.getName())
                    : cinemasHelper.slugify(req.getSlug());
            //  uniq
            String unique = base; int k = 2;
            while (cinemaRepository.existsBySlugIgnoreCaseAndIdNot(unique, id)) {
                unique = base + "-" + k++;
            }
            cinema.setSlug(unique);
        }

        // 3) Cities
        if (req.getCityIds() != null) {
            if (req.getCityIds().isEmpty()) {
                // Reletion delete → persistent set
                cinema.getCities().clear();
            } else {
                var wanted = new java.util.LinkedHashSet<>(req.getCityIds());
                var found = new java.util.LinkedHashSet<>(cityRepository.findAllById(wanted));
                var foundIds = found.stream().map(City::getId).collect(java.util.stream.Collectors.toSet());
                wanted.removeAll(foundIds);
                if (!wanted.isEmpty()) {
                    throw new ResourceNotFoundException(ErrorMessages.CITY_NOT_FOUND + wanted);
                }
                // SET → clear + addAll
                cinema.getCities().clear();
                cinema.getCities().addAll(found);
            }
        }

        // 4) save (or dirty checking with @Transactional)
        Cinema saved = cinemaRepository.save(cinema);
        return cinemaMapper.toSummary(saved);
    }


}