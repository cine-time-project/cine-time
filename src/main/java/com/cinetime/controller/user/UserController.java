package com.cinetime.controller.user;

import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateAuthenticatedUser(request));
    }

    // U07 - Delete authenticated user
    @DeleteMapping("/users/auth")
    public ResponseEntity<String> deleteAuthenticatedUser() {
        return ResponseEntity.ok(userService.deleteAuthenticatedUser());
    }

    // U08 - Search users with pagination
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String type
    ) {
        Sort.Direction direction = type.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        return ResponseEntity.ok(userService.searchUsers(q, pageable));
    }

    // U02 - User Register
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest req){
        UserResponse body = userService.saveUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
    // U09 - Get users (ADMIN or EMPLOYEE)
    @GetMapping("/users/4/admin")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // U10 - Update user by ADMIN or EMPLOYEE
    @PutMapping("/{userId}/admin")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<UserResponse> updateUserByAdminOrEmployee(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {

        return ResponseEntity.ok(userService.updateUserByAdminOrEmployee(userId, request));
    }





}

