package com.cinetime.service.user;

import com.cinetime.payload.request.authentication.LoginRequest;
import com.cinetime.payload.response.authentication.AuthenticationResponse;
import com.cinetime.security.jwt.JwtService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthenticationResponse authenticate(LoginRequest loginRequest) {
    String username = loginRequest.getUsername(); // email veya phone
    String password = loginRequest.getPassword();

    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = jwtService.generateToken(authentication);

    UserDetails principal = (UserDetails) authentication.getPrincipal();

    List<String> userRoles = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

    return AuthenticationResponse.builder()
            .token(token)
            .roles(userRoles)
            .username(principal.getUsername()) // email/phone
            .build();
  }
}

