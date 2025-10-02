package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.MailHelper;
import com.cinetime.service.helper.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserService_GetAuthenticatedUser_Test {
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

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("test@cinetime.com", "password")
        );
    }

    // ---------------- U08 - Search Users ----------------
    // U08 - searchUsers
    @Test
    void searchUsers_ShouldReturnPagedResponse() {
        Page<User> mockPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        ResponseMessage<Page<UserResponse>> result = userService.searchUsers(null, Pageable.unpaged());

        assertEquals(HttpStatus.OK, result.getHttpStatus());
        assertEquals(SuccessMessages.USERS_LISTED, result.getMessage());
    }
}