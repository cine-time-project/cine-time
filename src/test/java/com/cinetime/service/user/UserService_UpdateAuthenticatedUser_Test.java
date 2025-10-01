package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.UserUpdateRequest;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserService_UpdateAuthenticatedUser_Test {

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


    // U06 - updateAuthenticatedUser
    @Test
    void updateAuthenticatedUser_ShouldUpdateAndReturnResponse() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("UpdatedName");

        when(securityHelper.loadByLoginProperty(anyString())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateAuthenticatedUser(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateAuthenticatedUser_ShouldThrow_WhenBuiltInUser() {
        testUser.setBuiltIn(true);
        when(securityHelper.loadByLoginProperty(anyString())).thenReturn(testUser);

        assertThrows(ConflictException.class,
                () -> userService.updateAuthenticatedUser(new UserUpdateRequest()));
    }

    // U07 - deleteAuthenticatedUser
    @Test
    void deleteAuthenticatedUser_ShouldDeleteAndReturnMessage() {
        when(securityHelper.loadByLoginProperty(anyString())).thenReturn(testUser);

        String result = userService.deleteAuthenticatedUser();

        assertEquals(SuccessMessages.USER_DELETED, result);
        verify(userRepository).delete(testUser);
    }
}