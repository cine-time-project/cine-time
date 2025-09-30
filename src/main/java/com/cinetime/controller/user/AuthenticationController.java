package com.cinetime.controller.user;

import com.cinetime.payload.request.authentication.GoogleLoginRequest;
import com.cinetime.payload.request.authentication.LoginRequest;
import com.cinetime.payload.response.authentication.AuthenticationResponse;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.service.user.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
//TODO: is endpoint correct?
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(loginRequest));
    }

    //TODO: these endpoints should be rearranged.
    @PostMapping("/google")
    public ResponseMessage<AuthenticationResponse> loginWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
        return authenticationService.loginOrRegisterWithGoogle(request);
    }


    @GetMapping("/_debug/auth")
    public Map<String, Object> whoAmI(org.springframework.security.core.Authentication auth) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("principal", auth == null ? null : auth.getName());
        m.put("authorities", auth == null ? null : auth.getAuthorities().toString());
        return m;
    }

}
