package com.cinetime.service.business;

import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.CinemaMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.response.business.CinemaSummaryResponse;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.CinemasHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.cinetime.entity.user.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CinemaMapper cinemaMapper;
    private final CinemasHelper cinemasHelper;
    private final UserRepository userRepository;


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
    public CinemaSummaryResponse getCinemaById(Long id) {
        var cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CINEMA_NOT_FOUND));
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

}
