package com.cinetime.controller.user;

import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // U06 - Update authenticated user
    @PutMapping("/users/auth")
    public ResponseEntity<UserResponse> updateAuthenticatedUser(
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateAuthenticatedUser(request));
    }

    // U07 - Delete authenticated user
    @DeleteMapping("/users/auth")
    public ResponseEntity<String> deleteAuthenticatedUser() {
        return ResponseEntity.ok(userService.deleteAuthenticatedUser());
    }

    // U08 - Get authenticated user profile
    @GetMapping("/users/auth")
    public ResponseEntity<UserResponse> getAuthenticatedUser() {
        return ResponseEntity.ok(userService.getAuthenticatedUser());
    }

    // U02 - User Register
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest req){
        UserResponse body = userService.saveUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

}

