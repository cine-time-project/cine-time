package com.cinetime.service.user;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.repository.business.FavoriteRepository;
import com.cinetime.repository.business.PaymentRepository;
import com.cinetime.repository.business.TicketRepository;
import com.cinetime.repository.user.UserRepository;
import com.cinetime.service.helper.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserService_DeleteUserByAdminOrEmployee_Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FavoriteRepository favoriteRepository; // 🔹 EKLENDI

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setBuiltIn(false);

        // Sahte authentication set edelim
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin@cinetime.local", "password", "ROLE_ADMIN")
        );
    }

    // ---------------- U11 - Delete User by Admin/Employee ----------------
    @Test
    void deleteUserByAdminOrEmployee_ShouldReturnUserResponse() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        // Admin olduğu için employee checke girmesin
        when(securityHelper.isCallerEmployee(any())).thenReturn(false);

        var response = userService.deleteUserByAdminOrEmployee(userId);

        assertNotNull(response);
        assertEquals(mockUser.getId(), response.getReturnBody().getId());

        // ilişkili kayıtlar siliniyor mu?
        verify(favoriteRepository).deleteAllByUser_Id(userId);
        verify(ticketRepository).deleteAllByUser_Id(userId);
        verify(paymentRepository).deleteAllByUser_Id(userId);

        // user anonimleştirilip sonra siliniyor (BE’nin yaptığına göre)
        verify(userRepository).save(mockUser);
        verify(userRepository).delete(mockUser);
    }


    @Test
    void deleteUserByAdminOrEmployee_BuiltInUser_ShouldThrowConflict() {
        Long userId = 1L;
        mockUser.setBuiltIn(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class,
                () -> userService.deleteUserByAdminOrEmployee(userId));
    }
}
