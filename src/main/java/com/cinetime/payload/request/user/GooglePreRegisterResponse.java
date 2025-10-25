package com.cinetime.payload.request.user;

import com.cinetime.entity.enums.AuthProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GooglePreRegisterResponse {

    private String email;

    private String name;

    private String surname;

    private String googleId; // Google "sub" field

    private String picture; // profile picture

    private AuthProvider provider;

    //roles eklenecek



}
