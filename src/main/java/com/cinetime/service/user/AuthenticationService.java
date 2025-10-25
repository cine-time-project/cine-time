package com.cinetime.service.user;

import com.cinetime.entity.enums.AuthProvider;
import com.cinetime.entity.user.GoogleUser;
import com.cinetime.entity.user.User;
import com.cinetime.exception.BadRequestException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.authentication.GoogleLoginRequest;
import com.cinetime.payload.request.authentication.LoginRequest;
import com.cinetime.payload.response.user.GooglePreRegisterResponse;
import com.cinetime.payload.response.authentication.AuthenticatedUser;
import com.cinetime.payload.response.authentication.AuthenticationResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.repository.user.GoogleUserRepository;
import com.cinetime.security.jwt.JwtService;

import java.util.List;

import com.cinetime.security.service.GoogleIdTokenService;
import com.cinetime.service.helper.SecurityHelper;
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
    private final SecurityHelper securityHelper;

    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        String username = loginRequest.getPhoneOrEmail(); // email veya phone
        String password = loginRequest.getPassword();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtService.generateToken(authentication);

        UserDetails principal = (UserDetails) authentication.getPrincipal();

        List<String> userRoles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        User user = securityHelper.loadByLoginProperty(principal.getUsername());
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .name(user.getName())
                .username(principal.getUsername())
                .roles(userRoles)
                .build();

        return AuthenticationResponse.builder()
                .token(token)
                .user(authenticatedUser)
                .build();
    }

    private ResponseMessage<Object> loginWithGoogle(GoogleUser googleUser) {
        // JWT Generation
        String jwt = jwtService.buildTokenFromLoginProp(googleUser.getEmail());

        // Role list in String
        List<String> userRoles = googleUser.getRoles()
                .stream()
                .map(t -> t.getRoleName().name())
                .toList();

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .name(googleUser.getName())
                .username(googleUser.getEmail())
                .roles(userRoles)
                .build();

        // Build AuthenticationResponse
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token(jwt)
                .user(authenticatedUser)
                .build();

        return ResponseMessage.<Object>builder()
                .httpStatus(HttpStatus.OK)
                .message(SuccessMessages.USER_LOGGED_IN)
                .returnBody(authenticationResponse)
                .build();
    }

    public ResponseMessage<Object> loginOrRegisterWithGoogle(@Valid GoogleLoginRequest request) {
        var payloadOpt = googleIdTokenService.verify(request.getIdToken());
        if (payloadOpt.isEmpty()) throw new BadRequestException(ErrorMessages.INVALID_TOKEN_ID);

        var payload = payloadOpt.get();

        // Checking if this is a login process of an already-registered Google User
        if (googleUserRepository.findByGoogleId(payload.getGoogleId()).isPresent()) {
            GoogleUser googleUser = googleUserRepository.findByGoogleId(payload.getGoogleId()).get();
            return loginWithGoogle(googleUser);
        }

        GooglePreRegisterResponse preRegisterResponse = GooglePreRegisterResponse.builder()
                .email(payload.getEmail())
                .name(payload.getGivenName())
                .surname(payload.getFamilyName())
                .googleId(payload.getGoogleId())
                .picture(payload.getPicture())
                .build();

        return ResponseMessage.<Object>builder()
                .httpStatus(HttpStatus.I_AM_A_TEAPOT)
                .message(SuccessMessages.USER_FORWARDED)
                .returnBody(preRegisterResponse)
                .build();
    }

}

