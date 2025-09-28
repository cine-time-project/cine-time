//package com.cinetime.service.user;
//
//import com.cinetime.entity.user.User;
//import com.cinetime.exception.ConflictException;
//import com.cinetime.exception.ResourceNotFoundException;
//import com.cinetime.payload.mappers.UserMapper;
//import com.cinetime.payload.request.user.UserUpdateRequest;
//import com.cinetime.payload.response.user.UserResponse;
//import com.cinetime.repository.user.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import org.springframework.data.domain.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class UserService_GetAuthenticatedUser_Test {
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder encoder;
//
//    @InjectMocks
//    private UserService userService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ---------------- U08 - Search Users ----------------
//    @Test
//    void searchUsers_ShouldReturnPagedUsers() {
//        Pageable pageable = PageRequest.of(0, 20);
//        User user1 = new User();
//        User user2 = new User();
//        List<User> userList = Arrays.asList(user1, user2);
//        Page<User> mockPage = new PageImpl<>(userList, pageable, userList.size());
//
//        when(userRepository.findAll(pageable)).thenReturn(mockPage);
//
//        var result = userService.searchUsers(null, pageable);
//
//        assertNotNull(result);
//        assertEquals(2, result.getContent().size());
//        verify(userRepository).findAll(pageable);
//    }
//
//    @Test
//    void searchUsers_WithQuery_ShouldReturnFilteredPage() {
//        Pageable pageable = PageRequest.of(0, 20);
//        User user1 = new User();
//        User user2 = new User();
//        List<User> userList = Arrays.asList(user1, user2);
//        Page<User> mockPage = new PageImpl<>(userList, pageable, userList.size());
//
//        when(userRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(
//                anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(mockPage);
//
//        var result = userService.searchUsers("query", pageable);
//
//        assertNotNull(result);
//        assertEquals(2, result.getContent().size());
//        verify(userRepository).findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(
//                eq("query"), eq("query"), eq("query"), eq(pageable));
//    }
//}
