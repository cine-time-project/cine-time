package com.cinetime.controller.user;

import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // U08 - Get authenticated user profile
    @GetMapping("/auth")
    public ResponseEntity<UserResponse> getAuthenticatedUser() {
        return ResponseEntity.ok(userService.getAuthenticatedUser());
    }

    // U09 - Get all users (ADMIN or EMPLOYEE)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }




}

