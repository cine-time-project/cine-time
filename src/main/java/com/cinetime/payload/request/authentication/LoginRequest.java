package com.cinetime.payload.request.authentication;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

  @NotNull(message = "Email or Phone Number must be entered")
  private String username;

  @NotNull(message = "Password must not be empty")
  private String password;

}
