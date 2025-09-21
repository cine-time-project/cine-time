package com.cinetime.service.validator.showtime;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ShowtimeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidShowtime {
    String message() default "Invalid showtime details";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
