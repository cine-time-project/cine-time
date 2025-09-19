package com.cinetime.payload.messages;

public class SuccessMessages {

    private SuccessMessages(){
    }

    public static final String CINEMAS_LISTED = "Cinemas listed successfully.";

    //movie
    public static final String MOVIE_FOUND = "Movies are Found Successfully";
    public static final String MOVIE_WITH_SLUG_FOUND = "Movies with slug: %s  found successfully";
    public static final String MOVIE_WITH_ID_FOUND = "Movies with id: %s  found successfully";
    public static final String MOVIE_CREATE = "Movie is saved successfully";
    public static final String MOVIE_UPDATE = "Movie is updated successfully";
    public static final String MOVIE_DELETE = "Movie is deleted successfully";

    //image
    public static final String IMAGE_FETCHED  = "Image fetched successfully";
    public static final String IMAGE_UPLOADED = "Image uploaded successfully";
    public static final String IMAGE_UPDATED  = "Image updated successfully";
    public static final String IMAGE_DELETED  = "Image deleted successfully";

}
