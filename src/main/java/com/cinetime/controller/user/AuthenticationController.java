package com.cinetime.controller.user;

import com.cinetime.payload.request.authentication.LoginRequest;
import com.cinetime.payload.response.authentication.AuthenticationResponse;
import com.cinetime.service.user.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
//TODO: is endpoint correct?
@RequestMapping("/auth")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody @Valid LoginRequest loginRequest){
    return ResponseEntity.ok(authenticationService.authenticate(loginRequest));
  }


}
