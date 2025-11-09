package com.cinetime.payload.response.business;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoritedUserResponse {
    private Long userId;
    private String username;
    private String email;
}
