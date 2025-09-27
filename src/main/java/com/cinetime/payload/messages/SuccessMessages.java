package com.cinetime.payload.messages;

public class SuccessMessages {
    private SuccessMessages() {
    }

    //cinema
    public static final String CINEMAS_LISTED = "Cinemas listed successfully.";
    public static final String CINEMA_DELETED = "Cinema deleted successfully: %s";
    public static final String CINEMA_FETCHED = "Cinema fetched successfully: %s";
    public static final String SPECIAL_HALLS_LISTED = "Special Halls listed successfully";
    public static final String FAVORITES_LISTED = "Favorites listed successfully";
    public static final String CINEMA_CREATED = "Cinema created successfully";
    public static final String CINEMA_UPDATED = "Cinema updated successfully: %s";



    //movie
    public static final String MOVIE_FOUND = "Movies are Found Successfully";
    public static final String MOVIES_FOUND = "Movies are Found Successfully";
    public static final String MOVIES_FOUND_IN_THEATRES = "Now playing movies found";
    public static final String MOVIES_FOUND_IN_HALL = "Movies found in hall: %s";
    public static final String MOVIES_FOUND_ON_DATE = "Movies found as from the date: ";
    public static final String MOVIE_WITH_SLUG_FOUND = "Movies with slug: %s  found successfully";
    public static final String MOVIES_COMING_SOON_FOUND = "Coming soon movies found";
    public static final String MOVIE_WITH_ID_FOUND = "Movies with id: %s  found successfully";
    public static final String MOVIES_LISTED = "Movies have been successfully listed.";
    public static final String MOVIE_CREATE = "Movie is saved successfully";
    public static final String MOVIE_UPDATE = "Movie is updated successfully";
    public static final String MOVIE_DELETE = "Movie is deleted successfully";

    //image
    public static final String IMAGE_FETCHED = "Image fetched successfully";
    public static final String IMAGE_UPLOADED = "Image uploaded successfully";
    public static final String IMAGE_UPDATED = "Image updated successfully";
    public static final String IMAGE_DELETED = "Image deleted successfully";

    //user
    public static final String USER_UPDATED = "User updated successfully.";
    public static final String USER_DELETED = "User deleted successfully.";
    public static final String FORGOT_PASSWORD_EMAIL_SENT ="If the email exists, a reset code has been sent.";
    public static final String PASSWORD_RESET_SUCCESS = "Password has been reset successfully.";
    public static final String USER_REGISTERED = "User registered successfully.";
    public static final String USERS_LISTED = "Users listed successfully.";


    public static final String USER_CREATE = "User successfully created";
    public static final String PASSWORD_CHANGED = "Password has been updated successfully.";

    //Hall
    public static final String HALL_FOUND = "Hall is found successfully";
    public static final String HALLS_FOUND = "Halls found successfully";
    public static final String HALL_CREATED = "Hall is saved successfully";
    public static final String HALL_UPDATED = "Hall is updated successfully";
    public static final String HALL_DELETED = "Hall is deleted successfully";

    //Showtime
    public static final String SHOWTIME_FOUND = "Showtime is found successfully";
    public static final String SHOWTIMES_FOUND = "Showtime found successfully";
    public static final String SHOWTIME_CREATED = "Showtime is saved successfully";
    public static final String SHOWTIME_UPDATED = "Showtime is updated successfully";
    public static final String SHOWTIME_DELETED = "Showtime is deleted successfully";
    public static final String SHOWTIMES_FOUND_BY_CINEMA = "Showtimes found for cinema successfully";

    //Ticket
    public static final String CURRENT_TICKETS_LISTED = "Current tickets listed successfully.";
    public static final String PASSED_TICKETS_LISTED = "Passed tickets listed successfully.";
    public static final String TICKET_RESERVED = "Ticket reserved successfully.";
    public static final String TICKET_BOUGHT = "Ticket bought successfully.";


}
