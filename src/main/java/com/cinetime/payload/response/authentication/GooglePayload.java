package com.cinetime.payload.response.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GooglePayload {
    private String googleId;
    private String email;
    private String name;
}
