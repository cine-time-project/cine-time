package com.cinetime.controller.user;

import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.*;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserCreateResponse;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
    @GetMapping("/users/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseEntity<ResponseMessage<Page<UserResponse>>> searchUsers(
            @RequestParam(required = false) String q,
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {

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
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // U10 - Update user by ADMIN or EMPLOYEE
    @PutMapping("/{userId}/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseEntity<ResponseMessage<UserResponse>> updateUserByAdminOrEmployee(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {

        return ResponseEntity.ok(userService.updateUserByAdminOrEmployee(userId, request));
    }


    // U11 - Delete user by ADMIN or EMPLOYEE
    @DeleteMapping("/{userId}/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public ResponseEntity<ResponseMessage<UserResponse>> deleteUserByAdminOrEmployee(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.deleteUserByAdminOrEmployee(userId));
    }



    //U05 - Create User
    @PostMapping("/users/auth")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE','MEMBER')")
    public ResponseEntity<ResponseMessage<UserCreateResponse>> createUser(
            @RequestBody @Valid UserCreateRequest userCreateRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(userCreateRequest));


    }

    //U04-Reset Password
    @PostMapping("/reset-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseMessage<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPasswordForAuthenticatedUser(request);
        return ResponseMessage.<String>builder()
                .message(SuccessMessages.PASSWORD_CHANGED)
                .httpStatus(HttpStatus.OK)
                .returnBody(null) // <-- use the correct field name
                .build();
    }

    // U03 - Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestEmail req){
        String msg=userService.forgotPassword(req.getEmail());
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest req) {
        boolean valid = userService.verifyResetCode(req.getEmail(), req.getCode());
        if (!valid)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Kod hatalı veya süresi dolmuş"));
        return ResponseEntity.ok(Map.of("message", "Kod doğrulandı"));
    }

    //Reset Password with Email code
    @PostMapping("/reset-password-code")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestEmail req) {
        String msg = userService.resetPassword(req);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    // U01 - Get authenticated user info
    @GetMapping("/user-information")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserInformation() {
        return ResponseEntity.ok(userService.getAuthenticatedUser());
    }

}









