package com.cinetime.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cinetime.entity.user.User;
import com.cinetime.payload.request.user.ResetPasswordRequestDirect;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class ResetPasswordDirectTest {

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

    @Test
    void resetPasswordDirect_shouldResetPasswordSuccessfully() {
        // Arrange
        ResetPasswordRequestDirect req = new ResetPasswordRequestDirect();
        req.setEmail("test@example.com");
        req.setNewPassword("newPass123");

        User user = new User();
        user.setEmail("test@example.com");
        user.setResetPasswordCode("123456");

        when(userRepository.findByLoginProperty("test@example.com"))
                .thenReturn(Optional.of(user));
        when(encoder.encode("newPass123")).thenReturn("encodedPass");

        String result = userService.resetPasswordDirect(req);

        assertEquals(SuccessMessages.PASSWORD_RESET_SUCCESS, result);
        assertNull(user.getResetPasswordCode(), "Reset code should be cleared");
        assertEquals("encodedPass", user.getPassword(), "Password should be encoded");
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordDirect_shouldThrowException_WhenUserNotFound() {
        // Arrange
        ResetPasswordRequestDirect req = new ResetPasswordRequestDirect();
        req.setEmail("missing@example.com");
        req.setNewPassword("newPass123");

        when(userRepository.findByLoginProperty("missing@example.com"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.resetPasswordDirect(req)
        );

        assertEquals(ErrorMessages.USER_NOT_FOUND, ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
