package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.exception.BadRequestException;
import com.cinetime.payload.request.user.ResetPasswordRequest;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.service.business.RoleService;
import com.cinetime.service.helper.MailHelper;
import com.cinetime.service.helper.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * U04 – resetPasswordForAuthenticatedUser() unit tests.
 * Covers: success, old password mismatch, new == old edge case.
 */
public class UserService_ResetPasswordForAuthenticatedUser_Test {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder; // this is the encoder used inside UserService
    @Mock private UserMapper userMapper;
    @Mock private RoleService roleService;
    @Mock private JavaMailSender mailSender;
    @Mock private MailHelper mailHelper;
    @Mock private SecurityHelper securityHelper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Default security context for each test
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@test.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }

    private User mockExistingUser(String encodedPassword) {
        User u = new User();
        u.setEmail("test@test.com");
        u.setPassword(encodedPassword);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(u));
        when(securityHelper.loadByLoginProperty("test@test.com")).thenReturn(u);

        return u;
    }

    @Test
    void resetPassword_Success() {
        // given
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setOldPassword("Old_Pass1!");
        req.setNewPassword("New_Pass1!");

        User u = mockExistingUser("$encoded-old$");

        // encoder behavior for success scenario
        when(passwordEncoder.matches("Old_Pass1!", "$encoded-old$")).thenReturn(true);   // old password matches
        when(passwordEncoder.matches("New_Pass1!", "$encoded-old$")).thenReturn(false);  // new password is different
        when(passwordEncoder.encode("New_Pass1!")).thenReturn("$encoded-new$");

        // when
        assertDoesNotThrow(() -> userService.resetPasswordForAuthenticatedUser(req));

        // then
        assertEquals("$encoded-new$", u.getPassword());
        verify(userRepository).save(u);
    }

    @Test
    void resetPassword_OldPasswordMismatch_ShouldThrowBadRequest() {
        // given
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setOldPassword("Wrong_Old1!");
        req.setNewPassword("New_Pass1!");

        mockExistingUser("$encoded-old$");

        // old password does not match
        when(passwordEncoder.matches("Wrong_Old1!", "$encoded-old$")).thenReturn(false);

        // when / then
        assertThrows(BadRequestException.class,
                () -> userService.resetPasswordForAuthenticatedUser(req));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_NewPasswordSameAsOld_ShouldThrowBadRequest() {
        // given
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setOldPassword("Same_Pass1!");
        req.setNewPassword("Same_Pass1!");

        mockExistingUser("$encoded-same$");

        // both old and new match the current encoded password
        when(passwordEncoder.matches("Same_Pass1!", "$encoded-same$")).thenReturn(true);

        // when / then
        assertThrows(BadRequestException.class,
                () -> userService.resetPasswordForAuthenticatedUser(req));

        verify(userRepository, never()).save(any(User.class));
    }
}
