package com.cinetime.service.auth;


import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.ResetPasswordRequestEmail;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.mail.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MailService mailService;
    @Mock private PasswordEncoder encoder;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void forgotPassword_ShouldGenerateCodeAndSendMail() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(user));

        authService.forgotPassword("test@example.com");

        assertNotNull(user.getResetPasswordCode());
        verify(userRepository).save(user);
        verify(mailService).sendResetCode(eq("test@example.com"), anyString());
    }

    @Test
    void forgotPassword_UserNotFound_ShouldThrow() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.forgotPassword("missing@example.com"));
    }

    @Test
    void resetPassword_ValidCode_ShouldUpdatePasswordAndClearCode() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setResetPasswordCode("123456");

        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(user));
        when(encoder.encode("Aa!23456")).thenReturn("encodedPass");

        ResetPasswordRequestEmail req = new ResetPasswordRequestEmail();
        req.setEmail("test@example.com");
        req.setCode("123456");
        req.setNewPassword("Aa!23456");

        String result = authService.resetPassword(req);

        assertEquals(SuccessMessages.PASSWORD_RESET_SUCCESS, result);
        assertEquals("encodedPass", user.getPassword());
        assertNull(user.getResetPasswordCode());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_NoCode_ShouldThrow() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setResetPasswordCode(null);

        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(user));

        ResetPasswordRequestEmail req = new ResetPasswordRequestEmail();
        req.setEmail("test@example.com");
        req.setCode("123456");
        req.setNewPassword("Aa!23456");

        assertThrows(ConflictException.class,
                () -> authService.resetPassword(req));
    }

    @Test
    void resetPassword_InvalidCode_ShouldThrow() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setResetPasswordCode("654321");

        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(user));

        ResetPasswordRequestEmail req = new ResetPasswordRequestEmail();
        req.setEmail("test@example.com");
        req.setCode("123456");
        req.setNewPassword("Aa!23456");

        assertThrows(ConflictException.class,
                () -> authService.resetPassword(req));
    }
}
