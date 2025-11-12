package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class UserService_UpdateUserByAdminOrEmployee_Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setBuiltIn(false);
        // rollerle uğraşmıyoruz, servisin gerçek davranışını test edeceğiz

        // Çağıran kullanıcıyı sahte bir kullanıcı yapıyoruz
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("employee@cinetime.local", "password", "ROLE_EMPLOYEE")
        );
    }

    // ---------------- U10 - Update User by Admin/Employee ----------------
    // Şu anki servisin davranışı: employee non-MEMBER kullanıcıda AccessDenied fırlatıyor.
    @Test
    void updateUserByAdminOrEmployee_EmployeeOnNonMember_ShouldThrowAccessDenied() {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(AccessDeniedException.class,
                () -> userService.updateUserByAdminOrEmployee(userId, request));
    }

    @Test
    void updateUserByAdminOrEmployee_BuiltInUser_ShouldThrowConflict() {
        Long userId = 2L;
        User builtInUser = new User();
        builtInUser.setId(userId);
        builtInUser.setBuiltIn(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(builtInUser));

        assertThrows(ConflictException.class,
                () -> userService.updateUserByAdminOrEmployee(userId, new UserUpdateRequest()));
    }
}
