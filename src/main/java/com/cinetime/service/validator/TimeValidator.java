package com.cinetime.service.validator;

import com.cinetime.exception.BadRequestException;
import com.cinetime.payload.messages.ErrorMessages;

import java.time.LocalTime;

public class TimeValidator {
    //validate if start time is before stop time
    public void checkStartIsBeforeStop(LocalTime start, LocalTime stop) {
        if (start.isAfter(stop) || start.equals(stop)){
            throw new BadRequestException(ErrorMessages.SHOWTIME_HAS_PASSED);
        }
    }






}
