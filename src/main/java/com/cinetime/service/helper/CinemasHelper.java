package com.cinetime.service.helper;

import com.cinetime.exception.BadRequestException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.repository.business.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CinemasHelper {

    private final CityRepository cityRepository;

    /* cityId >0  */
    public void validateCityIfProvided(Long cityId) {
        if (cityId == null) return;
        if (cityId <= 0) {
            throw new BadRequestException("Invalid 'cityId': must be a positive id");
        }
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException(ErrorMessages.CITY_NOT_FOUND);
        }
    }

    /** "true/1/yes/y/special" -> TRUE ; "false/0/no/n/normal" -> FALSE ; other/empty -> null */
    public Boolean parseSpecialHall(String specialHall) {
        if (specialHall == null || specialHall.isBlank()) return null;
        String v = specialHall.trim().toLowerCase(Locale.ROOT);
        if (v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y") || v.equals("special")) {
            return Boolean.TRUE;
        }
        if (v.equals("false") || v.equals("0") || v.equals("no") || v.equals("n") || v.equals("normal")) {
            return Boolean.FALSE;
        }
        return null;
    }
}
