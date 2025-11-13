package com.cinetime.service.user;

import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.GoogleUser;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.request.user.GoogleRegisterRequest;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.business.FavoriteRepository;
import com.cinetime.repository.business.PaymentRepository;
import com.cinetime.repository.business.TicketRepository;
import com.cinetime.repository.user.GoogleUserRepository;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.business.RoleService;
import com.cinetime.service.helper.MailHelper;
import com.cinetime.service.helper.SecurityHelper;
import com.cinetime.util.PhoneUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService_SaveUser_Test {

    @Mock private UserRepository userRepository;
    @Mock private GoogleUserRepository googleUserRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder encoder;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleService roleService;
    @Mock private JavaMailSender mailSender;
    @Mock private MailHelper mailHelper;
    @Mock private SecurityHelper securityHelper;
    @Mock private TicketRepository ticketRepository;
    @Mock private FavoriteRepository favoriteRepository;
    @Mock private PaymentRepository paymentRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                googleUserRepository,
                roleRepository,
                encoder,           // 1. encoder alanı
                null,              // UserMapper: serviste static kullanılıyor, bu field’a ihtiyacımız yok
                passwordEncoder,   // 2. encoder alanı
                roleService,
                mailSender,
                mailHelper,
                securityHelper,
                ticketRepository,
                favoriteRepository,
                paymentRepository
        );

        // genel lenient stub’lar
        lenient().when(roleRepository.findByRoleName(RoleName.MEMBER))
                .thenReturn(Optional.of(mock(com.cinetime.entity.business.Role.class)));
        lenient().when(encoder.encode(anyString())).thenReturn("ENCODED");
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("ENCODED2");
    }

    @Test
    void saveUser_ShouldSaveNormalUser_WhenValidRequest() {
        UserRegisterRequest req = new UserRegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setPhone("05301234567");
        req.setFirstName("John");
        req.setLastName("Doe");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<PhoneUtils> phoneUtilsMock = mockStatic(PhoneUtils.class)) {
            phoneUtilsMock.when(() -> PhoneUtils.toE164(anyString(), anyString()))
                    .thenReturn("+905301234567");

            UserResponse response = userService.saveUser(req);

            // mapper gerçek çalıştığı için email’in aynen dönmesini bekliyoruz
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("test@example.com");

            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void saveUser_ShouldThrowConflict_WhenEmailExists() {
        UserRegisterRequest req = new UserRegisterRequest();
        req.setEmail("test@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.saveUser(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ErrorMessages.EMAIL_NOT_UNIQUE);
    }

    @Test
    void saveGoogleUser_ShouldSave_WhenValidRequest() throws Exception {
        GoogleRegisterRequest req = new GoogleRegisterRequest();
        req.setEmail("google@example.com");
        req.setPassword("password123");
        req.setPhone("05301234567");
        req.setGoogleId("google-123");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setBirthDate(LocalDate.of(2000, 1, 1));

        when(googleUserRepository.save(any(GoogleUser.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<PhoneUtils> phoneUtilsMock = mockStatic(PhoneUtils.class)) {
            phoneUtilsMock.when(() -> PhoneUtils.toE164(anyString(), anyString()))
                    .thenReturn("+905301234567");

            UserResponse response = userService.saveUser(req);

            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("google@example.com");

            verify(googleUserRepository).save(any(GoogleUser.class));
        }
    }
}
