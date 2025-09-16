package com.cinetime.controller.user;

import com.cinetime.controller.user.payload.request.user.UserUpdateRequest;
import com.cinetime.controller.user.payload.response.user.UserResponse;
import com.cinetime.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: Add Spring Security to authenticate users and get the authenticated user's ID
    //       Instead of passing userId via path, use AuthenticationFacade or SecurityContext
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }
}




