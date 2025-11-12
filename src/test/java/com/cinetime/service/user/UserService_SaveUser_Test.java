package com.cinetime.service.user;

import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.GoogleUser;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.mappers.UserMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GoogleUserRepository googleUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;  // ✅ EKLENDI
    @Mock
    private JavaMailSender mailSender;  // ✅ EKLENDI
    @Mock
    private MailHelper mailHelper;  // ✅ EKLENDI
    @Mock
    private SecurityHelper securityHelper;  // ✅ EKLENDI
    @Mock
    private TicketRepository ticketRepository;  // ✅ EKLENDI
    @Mock
    private FavoriteRepository favoriteRepository;  // ✅ EKLENDI
    @Mock
    private PaymentRepository paymentRepository;  // ✅ EKLENDI

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                googleUserRepository,
                roleRepository,
                passwordEncoder,
                userMapper,           // ✅ DÜZELTILDI - null yerine mock
                passwordEncoder,
                roleService,
                mailSender,           // ✅ EKLENDI
                mailHelper,           // ✅ EKLENDI
                securityHelper,       // ✅ EKLENDI
                ticketRepository,     // ✅ EKLENDI
                favoriteRepository,   // ✅ EKLENDI
                paymentRepository     // ✅ EKLENDI
        );
    }

    // ---------------------------------------
    // Test saveUser (normal user)
    // ---------------------------------------
    @Test
    void saveUser_ShouldSaveNormalUser_WhenValidRequest() {
        // Arrange
        UserRegisterRequest req = new UserRegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setPhone("05301234567");
        req.setFirstName("John");
        req.setLastName("Doe");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.MEMBER))
                .thenReturn(java.util.Optional.of(mock(com.cinetime.entity.business.Role.class)));
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_PASSWORD");

        // Mock save to return a User with roles set
        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setName("John");
        savedUser.setSurname("Doe");
        savedUser.setRoles(java.util.Set.of());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Mock PhoneUtils.toE164
        try (MockedStatic<PhoneUtils> phoneUtilsMock = mockStatic(PhoneUtils.class)) {
            phoneUtilsMock.when(() -> PhoneUtils.toE164(anyString(), anyString()))
                    .thenReturn("+905301234567");

            // Act
            UserResponse response = userService.saveUser(req);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void saveUser_ShouldThrowConflict_WhenEmailExists() {
        // Arrange
        UserRegisterRequest req = new UserRegisterRequest();
        req.setEmail("test@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.saveUser(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ErrorMessages.EMAIL_NOT_UNIQUE);
    }

    // ---------------------------------------
    // Test saveGoogleUser
    // ---------------------------------------
    @Test
    void saveGoogleUser_ShouldSave_WhenValidRequest() throws Exception {
        // Arrange
        GoogleRegisterRequest req = new GoogleRegisterRequest();
        req.setEmail("google@example.com");
        req.setPassword("password123");
        req.setPhone("05301234567");
        req.setGoogleId("google-123");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setBirthDate(LocalDate.of(2000, 1, 1));

        when(roleRepository.findByRoleName(RoleName.MEMBER))
                .thenReturn(java.util.Optional.of(mock(com.cinetime.entity.business.Role.class)));
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_PASSWORD");

        // Mock save to return a GoogleUser with roles set
        GoogleUser savedUser = new GoogleUser();
        savedUser.setEmail("google@example.com");
        savedUser.setName("John");
        savedUser.setSurname("Doe");
        savedUser.setRoles(java.util.Set.of());
        when(googleUserRepository.save(any(GoogleUser.class))).thenReturn(savedUser);

        // Mock PhoneUtils.toE164
        try (MockedStatic<PhoneUtils> phoneUtilsMock = mockStatic(PhoneUtils.class)) {
            phoneUtilsMock.when(() -> PhoneUtils.toE164(anyString(), anyString()))
                    .thenReturn("+905301234567");

            // Act
            UserResponse response = userService.saveUser(req);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("google@example.com");
            verify(googleUserRepository).save(any(GoogleUser.class));
        }
    }
}