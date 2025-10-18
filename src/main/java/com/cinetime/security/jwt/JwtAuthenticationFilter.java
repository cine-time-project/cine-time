package com.cinetime.security.jwt;

import com.cinetime.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;

  //  Yeni eklendi: JWT kontrolü yapılmayacak (public) endpoint listesi
  private static final String[] PUBLIC_ENDPOINTS = {
          "/api/forgot-password",
          "/api/reset-password",
          "/api/reset-password-code",
          "/api/reset-password-direct",
          "/api/verify-reset-code",
          "/api/send-email-code",
          "/api/register",
          "/api/contactmessages"
  };

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    try {
      String path = request.getServletPath();

      //  Yeni eklendi: Whitelist kontrolü
      for (String endpoint : PUBLIC_ENDPOINTS) {
        if (path.startsWith(endpoint)) {
          filterChain.doFilter(request, response);
          return; // Bu endpoint JWT doğrulaması istemiyor
        }
      }

      //1-from every request, we will get JWT
      String jwt = parseJwt(request);
      //validate JWT
      if (jwt != null && jwtService.validateToken(jwt)) {
        //3- we need username to get user information
        String username = jwtService.getUsernameFromToken(jwt);
        //4- check DB and fetch user and upgrade it to userDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        //5- set attribute with username
        request.setAttribute("username", username);
        //6- we load user details information to security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (UsernameNotFoundException e) {
      LOGGER.error("Can not set user authentication", e);
    }

    filterChain.doFilter(request, response);
  }

  //Authorization -> Bearer ljsdfnkltskdfnvszlkfnvaqqdfknvaefkdsnvsacdfjknvcaldknsvcal
  private String parseJwt(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}
