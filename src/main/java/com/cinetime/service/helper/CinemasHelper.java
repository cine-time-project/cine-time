package com.cinetime.service.helper;

import com.cinetime.exception.BadRequestException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.repository.business.CinemaRepository;
import com.cinetime.repository.business.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CinemasHelper {

    private final CityRepository cityRepository;
    private final CinemaRepository cinemaRepository;

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
    public String ensureUniqueSlug(String base) {
        String candidate = base;
        int i = 2;
        while (cinemaRepository.existsBySlugIgnoreCase(candidate)) {
            candidate = base + "-" + i;
            i++;
        }
        return candidate;
    }


    public String slugify(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        // Entity'de slug length=50, buna saygı:
        return n.length() > 50 ? n.substring(0, 50).replaceAll("-+$", "") : n;
    }
}

