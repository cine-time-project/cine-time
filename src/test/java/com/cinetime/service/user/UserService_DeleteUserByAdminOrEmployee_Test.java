package com.cinetime.service.user;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
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

public class UserService_DeleteUserByAdminOrEmployee_Test {
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
    // ---------------- U11 - Delete User by Admin/Employee ----------------
    @Test
    void deleteUserByAdminOrEmployee_ShouldReturnUserResponse() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setBuiltIn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        var response = userService.deleteUserByAdminOrEmployee(userId);

        assertNotNull(response);
        verify(userRepository).delete(mockUser);
    }

    @Test
    void deleteUserByAdminOrEmployee_BuiltInUser_ShouldThrowConflict() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setBuiltIn(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class, () -> userService.deleteUserByAdminOrEmployee(userId));
    }


}
