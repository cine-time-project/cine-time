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

public class UserService_DeleteAuthenticatedUser_Test {

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

    public class userServiceDeleteAuthenticatedUserTest {
        // ---------------- U07 - Delete Authenticated User ----------------
        @Test
        void deleteAuthenticatedUser_ShouldReturnSuccessMessage() {
            User mockUser = new User();
            mockUser.setBuiltIn(false);
            mockUser.setEmail("test@test.com");

            Authentication auth = mock(Authentication.class);
            when(auth.getName()).thenReturn("test@test.com");
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

            String result = userService.deleteAuthenticatedUser();
            assertNotNull(result);
            verify(userRepository).delete(mockUser);
        }

        @Test
        void deleteAuthenticatedUser_BuiltInUser_ShouldThrowConflict() {
            User mockUser = new User();
            mockUser.setBuiltIn(true);
            mockUser.setEmail("test@test.com");

            Authentication auth = mock(Authentication.class);
            when(auth.getName()).thenReturn("test@test.com");
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

            assertThrows(ConflictException.class, () -> userService.deleteAuthenticatedUser());
        }
    }
}