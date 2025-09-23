package com.cinetime.service.user;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.repository.user.RoleRepository; // sizde farklı paketse onu import edin
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService_SaveUser_Test {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder encoder;

    @InjectMocks private UserService userService;

    private UserRegisterRequest req;

    @BeforeEach
    void setUp() {
        req = new UserRegisterRequest();
        req.setFirstName("Ada");
        req.setLastName("Lovelace");
        req.setPhone("(555) 123-4579");
        req.setEmail("ADA@EXAMPLE.COM");
        req.setPassword("Aa!12345");
        req.setBirthDate(LocalDate.of(2000, 1, 15));
        req.setGender(Gender.FEMALE);
    }

    @Test
    void saveUser_success_maps_encodes_setsMemberRole_andReturnsResponse() {
        // given
        when(userRepository.existsByEmail("ADA@EXAMPLE.COM")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("(555) 123-4579")).thenReturn(false);

        // mapper: request -> entity
        User mapped = new User();
        mapped.setName("Ada");
        mapped.setSurname("Lovelace");
        mapped.setEmail("ADA@EXAMPLE.COM");
        mapped.setPhoneNumber("(555) 123-4579");
        mapped.setPassword("Aa!12345");
        mapped.setBirthDate(LocalDate.of(2000, 1, 15));
        mapped.setGender(Gender.FEMALE);

        // mapper: entity -> response (dönecek mock)
        UserResponse mappedResp = new UserResponse();
        mappedResp.setId(99L);
        mappedResp.setName("Ada");
        mappedResp.setSurname("Lovelace");
        mappedResp.setEmail("ada@example.com");
        mappedResp.setPhoneNumber("(555) 123-4579");
        mappedResp.setBirthDate(LocalDate.of(2000, 1, 15));
        mappedResp.setGender("FEMALE");

        Role member = new Role();
        member.setId(1L);
        member.setRoleName(RoleName.MEMBER);


        when(encoder.encode("Aa!12345")).thenReturn("ENC");
        when(roleRepository.findByRoleName(RoleName.MEMBER)).thenReturn(Optional.of(member));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        try (MockedStatic<UserMapper> mocked = Mockito.mockStatic(UserMapper.class)) {
            mocked.when(() -> UserMapper.fromRegisterRequest(req)).thenReturn(mapped);
            mocked.when(() -> UserMapper.toResponse(any(User.class))).thenReturn(mappedResp);

            // when
            UserResponse out = userService.saveUser(req);

            // then
            assertThat(out.getId()).isEqualTo(99L);
            assertThat(out.getEmail()).isEqualTo("ada@example.com"); // lowercased dönen response
            assertThat(out.getGender()).isEqualTo("FEMALE");

            // kaydedilen entity üzerinde kritik alanları doğrula
            verify(userRepository).save(argThat(u ->
                    u.getEmail().equals("ada@example.com") &&            // lower-case
                            u.getPassword().equals("ENC") &&                     // encoded
                            u.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleName.MEMBER)
            ));

            verify(userRepository).existsByEmail("ADA@EXAMPLE.COM");
            verify(userRepository).existsByPhoneNumber("(555) 123-4579");
            verify(roleRepository).findByRoleName(RoleName.MEMBER);

            mocked.verify(() -> UserMapper.fromRegisterRequest(req));
            mocked.verify(() -> UserMapper.toResponse(any(User.class)));
        }
    }

    @Test
    void saveUser_throws_onDuplicateEmail() {
        when(userRepository.existsByEmail("ADA@EXAMPLE.COM")).thenReturn(true);

        assertThatThrownBy(() -> userService.saveUser(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void saveUser_throws_onDuplicatePhone() {
        when(userRepository.existsByEmail("ADA@EXAMPLE.COM")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("(555) 123-4579")).thenReturn(true);

        assertThatThrownBy(() -> userService.saveUser(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Phone already in use");

        verify(userRepository, never()).save(any());
    }
}
