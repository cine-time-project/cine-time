package com.cinetime.payload.messages;

public class ErrorMessages {
    private ErrorMessages() {
    }

    //cinema
    public static final String CINEMA_NOT_FOUND = "Cinema not found by id: %s";
    public static final String CITY_NOT_FOUND = "City not found by id: %s";


    //user
    public static final String NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD = "Error: User not found with %s";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String BUILT_IN_USER_UPDATE_NOT_ALLOWED = "Built-in users cannot be updated.";
    public static final String BUILT_IN_USER_DELETE_NOT_ALLOWED = "Built-in users cannot be deleted.";
    public static final String INVALID_GENDER = "Invalid gender value. Use MALE, FEMALE or OTHER.";
    public static final String EMAIL_NOT_UNIQUE = "Email already in use";
    public static final String PHONE_NUMBER_NOT_UNIQUE = "Phone already in use";
    public static final String MEMBER_ROLE_MISSING = "MEMBER role missing";
    public static final String OLD_PASSWORD_MISMATCH = "Old password is incorrect.";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "New password cannot be the same as old password.";

    public static final String EMAIL_SENDING_FAILED = "Error: Failed to send email.";
    public static final String RESET_CODE_REQUIRED = "Reset code is required.";
    public static final String INVALID_RESET_CODE = "Invalid reset code.";
    public static final String ACCESS_DANIED = "Employees can operate only on MEMBER users.";



    //movie
    public static final String MOVIE_NOT_FOUND = "Movie is not found";
    public static final String MOVIE_NOT_FOUND_BY_ID = "Movie is not found by id: %s";
    public static final String MOVIES_NOT_FOUND = "Movies are not found";
    public static final String MOVIES_NOT_FOUND_IN_HALL = "No movies found in hall: %s";
    public static final String MOVIES_NOT_IN_THEATRES = "No movies currently in theatres";
    public static final String MOVIES_NOT_FOUND_ON_DATE = "No movies found on date";
    public static final String MOVIES_COMING_SOON_NOT_FOUND = "No coming soon movies found";

    //image
    // 404 - Not Found
    public static final String IMAGE_NOT_FOUND_ID = "Image not found with id: %d";
    public static final String IMAGE_NOT_FOUND = "Image not found";
    public static final String MOVIE_NOT_FOUND_ID = "Movie not found with id: %d";

    // 400 - Bad Request
    public static final String FILE_MUST_NOT_BE_EMPTY = "file must not be null or empty";
    public static final String FILE_SIZE_EXCEEDED = "file size must be <= 5MB";
    public static final String UNSUPPORTED_CONTENT_TYPE = "unsupported content type: %s";

    // 409 - Conflict
    public static final String MOVIE_ALREADY_HAS_POSTER = "This movie already has a poster.";
    public static final String MOVIE_ALREADY_HAS_DIFFERENT_POSTER = "This movie already has a different poster.";

    //Hall
    public static final String HALL_NOT_FOUND_ID = "Hall not found with id: %d";
    public static final String HALLS_NOT_FOUND = "Halls not found";

    //Showtime
    public static final String SHOWTIME_NOT_FOUND_ID = "Showtime not found with id: %d";
    public static final String SHOWTIMES_NOT_FOUND = "Showtimes not found";

    //Ticket
    //409 - Conflict
    public static final String SHOWTIME_HAS_PASSED= "Movie time can not be in the past.";

}



