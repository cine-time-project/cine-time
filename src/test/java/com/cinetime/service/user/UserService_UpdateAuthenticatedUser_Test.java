
package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserService_UpdateAuthenticatedUser_Test {

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

    // ---------------- U06 - Update Authenticated User ----------------
    @Test
    void updateAuthenticatedUser_ShouldReturnUpdatedUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        User mockUser = new User();
        mockUser.setBuiltIn(false);
        mockUser.setEmail("test@test.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.updateAuthenticatedUser(request);

        assertNotNull(response);
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateAuthenticatedUser_BuiltInUser_ShouldThrowConflict() {
        UserUpdateRequest request = new UserUpdateRequest();
        User mockUser = new User();
        mockUser.setBuiltIn(true);
        mockUser.setEmail("test@test.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class, () -> userService.updateAuthenticatedUser(request));
    }
}