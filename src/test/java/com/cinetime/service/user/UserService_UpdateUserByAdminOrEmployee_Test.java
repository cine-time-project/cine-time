package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserService_UpdateUserByAdminOrEmployee_Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setBuiltIn(false);

        // Sahte authentication (ADMIN gibi davranıyor)
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin@cinetime.local", "password", "ROLE_ADMIN")
        );
    }

    // ---------------- U10 - Update User by Admin/Employee ----------------
    @Test
    void updateUserByAdminOrEmployee_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Employee/admin kontrolü için sahte izin verelim
        when(securityHelper.isCallerEmployee(any())).thenReturn(false);

        UserResponse response = userService.updateUserByAdminOrEmployee(userId, request).getReturnBody();

        assertNotNull(response);
        assertEquals(mockUser.getId(), response.getId());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUserByAdminOrEmployee_BuiltInUser_ShouldThrowConflict() {
        Long userId = 2L;
        User builtInUser = new User();
        builtInUser.setId(userId);
        builtInUser.setBuiltIn(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(builtInUser));

        assertThrows(ConflictException.class,
                () -> userService.updateUserByAdminOrEmployee(userId, new UserUpdateRequest()));
    }
}

