package com.cinetime.controller.user.payload.messages;

public class ErrorMessages {

    public static String BUILT_IN_USER_DELETE_NOT_ALLOWED;

    private ErrorMessages() {}

    public static final String USER_NOT_FOUND = "User not found.";
    public static final String BUILT_IN_USER_UPDATE_NOT_ALLOWED = "Built-in users cannot be updated.";
    public static final String INVALID_GENDER = "Invalid gender value. Use MALE, FEMALE or OTHER.";


}
