package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.repository.business.FavoriteRepository;
import com.cinetime.repository.business.PaymentRepository;
import com.cinetime.repository.business.TicketRepository;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.MailHelper;
import com.cinetime.service.helper.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserService_DeleteAuthenticatedUser_Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private MailHelper mailHelper;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@cinetime.com");
        testUser.setPassword("encodedPass");
        testUser.setBuiltIn(false);

        // Authenticated user simülasyonu
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("test@cinetime.com", "password")
        );
    }

    // ✅ U07 - deleteAuthenticatedUser (başarılı senaryo)
    @Test
    void deleteAuthenticatedUser_ShouldDeleteAndReturnMessage() {
        when(securityHelper.loadByLoginProperty(anyString())).thenReturn(testUser);

        String result = userService.deleteAuthenticatedUser();

        assertEquals(SuccessMessages.USER_DELETED, result);

        // ilişkili kayıtların silindiğini doğrula
        verify(favoriteRepository).deleteAllByUser_Id(testUser.getId());
        verify(ticketRepository).deleteAllByUser_Id(testUser.getId());
        verify(paymentRepository).deleteAllByUser_Id(testUser.getId());

        // servis içinde önce save, sonra deleteById çağrılıyor
        verify(userRepository).save(testUser);
        verify(userRepository).deleteById(testUser.getId());
    }

    // ❌ builtIn kullanıcı silinemez
    @Test
    void deleteAuthenticatedUser_ShouldThrow_WhenBuiltInUser() {
        testUser.setBuiltIn(true);
        when(securityHelper.loadByLoginProperty(anyString())).thenReturn(testUser);

        assertThrows(ConflictException.class, () -> userService.deleteAuthenticatedUser());
    }
}
