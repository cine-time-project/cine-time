package com.cinetime.service.user;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.payload.request.user.UserCreateRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserCreateResponse;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.business.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

import static com.cinetime.payload.messages.ErrorMessages.EMAIL_NOT_UNIQUE;
import static com.cinetime.payload.messages.ErrorMessages.PHONE_NUMBER_NOT_UNIQUE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateUserTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Authentication authentication = mock(Authentication.class);
        doReturn(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    // --- TEST 1: Admin çağırıyor, request'teki rol atanıyor ---
    @Test
    void createUser_whenAdmin_createsUserWithRequestedRole() {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Ahmet")
                .surname("Yılmaz")
                .email("ahmet@example.com")
                .password("123456")
                .phoneNumber("(555) 123-4567")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .builtIn(true)
                .role(RoleName.EMPLOYEE)
                .build();

        User userEntity = new User();
        when(userMapper.mapUserCreateRequestToUser(request)).thenReturn(userEntity);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-pass");

        Role role = new Role();
        role.setRoleName(RoleName.EMPLOYEE);
        when(roleService.getRole(RoleName.EMPLOYEE)).thenReturn(role);

        when(userRepository.save(userEntity)).thenReturn(userEntity);

        UserCreateResponse responseDto = new UserCreateResponse();
        responseDto.setEmail("ahmet@example.com");
        when(userMapper.mapUserToUserCreateResponse(userEntity)).thenReturn(responseDto);

        ResponseMessage<UserCreateResponse> response = userService.createUser(request);

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(userEntity.getPassword()).isEqualTo("encoded-pass");
        assertThat(userEntity.getRoles()).contains(role);
        assertThat(userEntity.getBuiltIn()).isTrue();
    }

    // --- TEST 2: Email zaten varsa ConflictException fırlatır ---
    @Test
    void createUser_whenEmailExists_shouldThrowConflictException() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("ahmet@example.com")
                .phoneNumber("(555) 123-4567")
                .build();

        when(userRepository.existsByEmail("ahmet@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(EMAIL_NOT_UNIQUE);

        verify(userRepository, never()).save(any());
    }

    // --- TEST 3: Telefon zaten varsa ConflictException fırlatır ---
    @Test
    void createUser_whenPhoneExists_shouldThrowConflictException() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("ahmet@example.com")
                .phoneNumber("(555) 123-4567")
                .build();

        when(userRepository.existsByEmail("ahmet@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("(555) 123-4567")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(PHONE_NUMBER_NOT_UNIQUE);

        verify(userRepository, never()).save(any());
    }

    // --- TEST 4: Admin değil (ör. MEMBER) çağırıyor → Rol = MEMBER, builtIn = false ---
    @Test
    void createUser_whenNotAdmin_shouldForceMemberRoleAndBuiltInFalse() {

        Authentication authentication = mock(Authentication.class);
        doReturn(Set.of(new SimpleGrantedAuthority("ROLE_MEMBER")))
                .when(authentication).getAuthorities();

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        UserCreateRequest request = UserCreateRequest.builder()
                .email("member@example.com")
                .password("123456")
                .phoneNumber("(555) 987-6543")
                .role(RoleName.ADMIN)
                .build();

        User userEntity = new User();
        when(userMapper.mapUserCreateRequestToUser(request)).thenReturn(userEntity);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");

        Role memberRole = new Role();
        memberRole.setRoleName(RoleName.MEMBER);
        when(roleService.getRole(RoleName.MEMBER)).thenReturn(memberRole);

        when(userRepository.save(userEntity)).thenReturn(userEntity);

        UserCreateResponse responseDto = new UserCreateResponse();
        responseDto.setEmail("member@example.com");
        when(userMapper.mapUserToUserCreateResponse(userEntity)).thenReturn(responseDto);

        ResponseMessage<UserCreateResponse> response = userService.createUser(request);

        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(userEntity.getBuiltIn()).isFalse();
        assertThat(userEntity.getRoles()).contains(memberRole);
    }
}
