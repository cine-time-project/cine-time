package com.cinetime.service.user;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserService_UpdateUserByAdminOrEmployee_Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- U10 - Update User by Admin/Employee ----------------
    @Test
    void updateUserByAdminOrEmployee_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        User mockUser = new User();
        mockUser.setBuiltIn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.updateUserByAdminOrEmployee(userId, request).getReturnBody();

        assertNotNull(response);
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUserByAdminOrEmployee_BuiltInUser_ShouldThrowConflict() {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        User mockUser = new User();
        mockUser.setBuiltIn(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class, () -> userService.updateUserByAdminOrEmployee(userId, request));
    }
}