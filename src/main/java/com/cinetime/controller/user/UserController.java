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

    // TODO: Once security is implemented, remove userId from path and get it from authenticated user context
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {

        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }
}






