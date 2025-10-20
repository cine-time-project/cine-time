package com.cinetime.security.service;

import com.cinetime.payload.request.user.GoogleUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleIdTokenService {

    @Value("${app.google.client-id}")
    private String clientId;

    private final JwtDecoder jwtDecoder;

    /**
     * verifies Google ID Token and returns payload.
     */
    public Optional<GoogleUserRequest> verify(String idToken) {
        try {
            Jwt jwt = jwtDecoder.decode(idToken);

            // 1- Audience control (client_id)
            Object aud = jwt.getClaim("aud");
            if (aud instanceof String && !clientId.equals(aud)) return Optional.empty();
            if (aud instanceof Collection && !((Collection<?>) aud).contains(clientId)) return Optional.empty();

            // 2- Issuer control
            String issuer = jwt.getClaimAsString("iss");
            if (!"https://accounts.google.com".equals(issuer) &&
                    !"accounts.google.com".equals(issuer)) return Optional.empty();

            // 3- Email verification
            String email = jwt.getClaimAsString("email");
            Boolean emailVerified = (Boolean) jwt.getClaim("email_verified");
            if (email == null || !Boolean.TRUE.equals(emailVerified)) {
                email = jwt.getSubject() + "@google.local"; // fake internal email
            }

            // 4- Payload creation
            GoogleUserRequest payload = GoogleUserRequest.builder()
                    .googleId(jwt.getSubject())
                    .email(jwt.getClaimAsString("email"))
                    .name(jwt.getClaimAsString("name"))
                    .givenName(jwt.getClaimAsString("given_name"))
                    .familyName(jwt.getClaimAsString("family_name"))
                    .picture(jwt.getClaimAsString("picture"))
                    .build();

            return Optional.of(payload);

        } catch (JwtException e) {
            log.error("Google ID Token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
