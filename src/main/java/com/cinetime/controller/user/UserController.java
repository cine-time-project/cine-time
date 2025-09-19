package com.cinetime.controller.user;

import com.cinetime.controller.user.payload.request.user.UserUpdateRequest;
import com.cinetime.controller.user.payload.response.user.UserResponse;
import com.cinetime.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // U06 - Update authenticated user
    @PutMapping("/auth")
    public ResponseEntity<UserResponse> updateAuthenticatedUser(
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateAuthenticatedUser(request));
    }

    // U07 - Delete authenticated user
    @DeleteMapping("/auth")
    public ResponseEntity<String> deleteAuthenticatedUser() {
        return ResponseEntity.ok(userService.deleteAuthenticatedUser());
    }
}

