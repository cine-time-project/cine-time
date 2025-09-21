package com.cinetime.service.validator.showtime;

import com.cinetime.payload.request.business.ShowtimeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.LocalTime;

public class ShowtimeValidator implements ConstraintValidator<ValidShowtime, ShowtimeRequest> {

    @Override
    public boolean isValid(ShowtimeRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // @NotNull zaten ayrı kontrol ediyor
        }

        LocalDate today = LocalDate.now();

        // 1️⃣ Tarih geçmişte olamaz
        if (request.getDate() != null && request.getDate().isBefore(today)) {
            context.buildConstraintViolationWithTemplate("Showtime date cannot be in the past")
                    .addPropertyNode("date")
                    .addConstraintViolation();
            return false;
        }

        // 2️⃣ StartTime < EndTime olmalı
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (!request.getEndTime().isAfter(request.getStartTime())) {
                context.buildConstraintViolationWithTemplate("End time must be after start time")
                        .addPropertyNode("endTime")
                        .addConstraintViolation();
                return false;
            }
        }

        // 3️⃣ Eğer tarih bugünün tarihi ise, başlangıç zamanı şu andan önce olamaz
        if (request.getDate() != null && request.getStartTime() != null) {
            if (request.getDate().isEqual(today)) {
                LocalTime now = LocalTime.now();
                if (request.getStartTime().isBefore(now)) {
                    context.buildConstraintViolationWithTemplate("Start time cannot be in the past")
                            .addPropertyNode("startTime")
                            .addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }
}
