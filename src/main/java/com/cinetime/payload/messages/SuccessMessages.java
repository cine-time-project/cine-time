package com.cinetime.payload.messages;

public class SuccessMessages {
    private SuccessMessages() {
    }

    public static final String CINEMAS_LISTED = "Cinemas listed successfully.";

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

    //Hall
    public static final String HALL_FOUND = "Hall is found successfully";
    public static final String HALLS_FOUND = "Halls found successfully";
    public static final String HALL_CREATED = "Hall is saved successfully";
    public static final String HALL_UPDATED = "Hall is updated successfully";
    public static final String HALL_DELETED = "Hall is deleted successfully";
}
