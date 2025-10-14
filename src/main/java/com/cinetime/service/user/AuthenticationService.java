package com.cinetime.service.user;

import com.cinetime.entity.user.GoogleUser;
import com.cinetime.exception.BadRequestException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.authentication.GoogleLoginRequest;
import com.cinetime.payload.request.authentication.LoginRequest;
import com.cinetime.payload.response.authentication.AuthenticationResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.user.GoogleUserRepository;
import com.cinetime.security.jwt.JwtService;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cinetime.security.service.GoogleIdTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final GoogleIdTokenService googleIdTokenService;
    private final JwtService jwtService;
    private final GoogleUserRepository googleUserRepository;
    private final UserService userService;

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

    public ResponseMessage<AuthenticationResponse> loginOrRegisterWithGoogle(@Valid GoogleLoginRequest request) {
        var payloadOpt = googleIdTokenService.verify(request.getIdToken());
        if (payloadOpt.isEmpty()) throw new BadRequestException(ErrorMessages.INVALID_TOKEN_ID);

        var payload = payloadOpt.get();

        // Email check in UserRepository
        AtomicBoolean newRegistration = new AtomicBoolean(false);
        GoogleUser googleUser = googleUserRepository.findByGoogleId(payload.getGoogleId())
                .orElseGet(() -> {
                    // New GoogleUser registration
                    newRegistration.set(true);
                    return userService.saveGoogleUser(payload);
                });

        // JWT Generation
        String jwt = jwtService.buildTokenFromLoginProp(googleUser.getGoogleId());

        // Role list in String
        List<String> userRoles = googleUser.getRoles()
                .stream()
                .map(t -> t.getRoleName().name())
                .toList();

        // Build AuthenticationResponse
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .name(googleUser.getName())
                .username(googleUser.getEmail())
                .token(jwt)
                .roles(userRoles)
                .build();

        return ResponseMessage.<AuthenticationResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message(
                        newRegistration.get()
                                ? SuccessMessages.USER_CREATE
                                : SuccessMessages.USER_LOGGED_IN
                )
                .returnBody(authenticationResponse)
                .build();
    }

}

