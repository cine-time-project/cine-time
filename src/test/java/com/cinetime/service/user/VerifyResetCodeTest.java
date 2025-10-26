package com.cinetime.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cinetime.entity.user.User;
import com.cinetime.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

public class VerifyResetCodeTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void verifyResetCode_shouldReturnTrue_whenCodeMatches() {
        // Arrange
        String email = "user@example.com";
        String code = "ABC123";

        User user = new User();
        user.setEmail(email);
        user.setResetPasswordCode("ABC123");

        when(userRepository.findByLoginProperty(email)).thenReturn(Optional.of(user));

        boolean result = userService.verifyResetCode(email, code);

        assertTrue(result, "Kod doğru olduğunda true dönmeli");
        verify(userRepository).findByLoginProperty(email);
    }

    @Test
    void verifyResetCode_shouldReturnFalse_whenCodeDoesNotMatch() {

        String email = "user@example.com";
        String code = "WRONGCODE";

        User user = new User();
        user.setEmail(email);
        user.setResetPasswordCode("ABC123");

        when(userRepository.findByLoginProperty(email)).thenReturn(Optional.of(user));

        boolean result = userService.verifyResetCode(email, code);

        assertFalse(result, "Kod yanlış olduğunda false dönmeli");
        verify(userRepository).findByLoginProperty(email);
    }

    @Test
    void verifyResetCode_shouldReturnFalse_whenUserNotFound() {

        String email = "notfound@example.com";
        String code = "ANYCODE";

        when(userRepository.findByLoginProperty(email)).thenReturn(Optional.empty());

        boolean result = userService.verifyResetCode(email, code);

        assertFalse(result, "Kullanıcı bulunmadığında false dönmeli");
        verify(userRepository).findByLoginProperty(email);
    }
}
