package com.cinetime.payload.messages;

public class ErrorMessages {

  public static final String CINEMA_NOT_FOUND = "Cinema not found";
  public static final String CITY_NOT_FOUND = "City not found";

  //user
  public static final String NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD = "Error: User not found with %s";

  //movie
  public static final String MOVIE_NOT_FOUND = "Movie is not found";
  public static final String MOVIE_NOT_FOUND_BY_ID = "Movie is not found by id: %s";

  //image
  // 404 - Not Found
  public static final String IMAGE_NOT_FOUND_ID = "Image not found with id: %d";
  public static final String MOVIE_NOT_FOUND_ID = "Movie not found with id: %d";

  // 400 - Bad Request
  public static final String FILE_MUST_NOT_BE_EMPTY   = "file must not be null or empty";
  public static final String FILE_SIZE_EXCEEDED       = "file size must be <= 5MB";
  public static final String UNSUPPORTED_CONTENT_TYPE = "unsupported content type: %s";

  // 409 - Conflict
  public static final String MOVIE_ALREADY_HAS_POSTER           = "This movie already has a poster.";
  public static final String MOVIE_ALREADY_HAS_DIFFERENT_POSTER = "This movie already has a different poster.";


}
