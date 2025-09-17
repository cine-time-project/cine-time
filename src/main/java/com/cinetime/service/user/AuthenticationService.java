package com.cinetime.service.user;

import com.cinetime.payload.request.authentication.LoginRequest;
import com.cinetime.payload.response.authentication.AuthenticationResponse;
import com.cinetime.security.jwt.JwtService;
import com.cinetime.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;


  public AuthenticationResponse authenticate(@Valid LoginRequest loginRequest) {

    String username = loginRequest.getUsername();
    String password = loginRequest.getPassword();
    //injection of security in service
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password));

    //security authentication bu satirda yapiliyor
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = jwtService.generateToken(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    List<String> userRoles = userDetails.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    return AuthenticationResponse.builder()
        .token(token)
        .roles(userRoles)
        .username(userDetails.getUsername()) //email or phoneNumber is username here.
        .build();
  }

}
