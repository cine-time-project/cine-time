package com.cinetime.payload.request.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoogleUserRequest {
    private String googleId; //The only field that will not be null for sure.
    private String email;
    private String name;
    private String givenName;
    private String familyName;
    private String picture;
}
