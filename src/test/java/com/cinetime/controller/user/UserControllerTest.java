
package com.cinetime.controller.user;

import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserCreateResponse;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // U06 - Update authenticated user
    @Test
    void updateAuthenticatedUser_ShouldReturnUpdatedUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        UserResponse mockResponse = new UserResponse();
        mockResponse.setId(1L);
        mockResponse.setName("updatedUser");

        when(userService.updateAuthenticatedUser(any(UserUpdateRequest.class))).thenReturn(mockResponse);

        ResponseEntity<UserResponse> response = userController.updateAuthenticatedUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userService, times(1)).updateAuthenticatedUser(request);
    }

    // U07 - Delete authenticated user
    @Test
    void deleteAuthenticatedUser_ShouldReturnMessage() {
        String mockMessage = "User deleted successfully";
        when(userService.deleteAuthenticatedUser()).thenReturn(mockMessage);

        ResponseEntity<String> response = userController.deleteAuthenticatedUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockMessage, response.getBody());
        verify(userService, times(1)).deleteAuthenticatedUser();
    }

    // U08 - Search users with pagination
    @Test
    void searchUsers_ShouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        List<UserResponse> userList = Arrays.asList(new UserResponse(), new UserResponse());
        Page<UserResponse> mockPage = new PageImpl<>(userList, pageable, userList.size());

        ResponseMessage<Page<UserResponse>> mockResponse =
                ResponseMessage.<Page<UserResponse>>builder()
                        .httpStatus(HttpStatus.OK)
                        .message("success")
                        .returnBody(mockPage)
                        .build();

        when(userService.searchUsers(anyString(), any(Pageable.class))).thenReturn(mockResponse);

        ResponseEntity<ResponseMessage<Page<UserResponse>>> response =
                userController.searchUsers("test", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPage, response.getBody().getReturnBody());
        verify(userService, times(1)).searchUsers(anyString(), any(Pageable.class));
    }

    // U02 - User Register
    @Test
    void register_ShouldReturnCreatedUser() {
        UserRegisterRequest request = new UserRegisterRequest();
        UserResponse mockResponse = new UserResponse();
        mockResponse.setId(1L);
        mockResponse.setName("newUser");

        when(userService.saveUser(any(UserRegisterRequest.class))).thenReturn(mockResponse);

        ResponseEntity<UserResponse> response = userController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userService, times(1)).saveUser(request);
    }

    // U09 - Get all users
    @Test
    void getAllUsers_ShouldReturnUserList() {
        List<UserResponse> mockList = Arrays.asList(new UserResponse(), new UserResponse());
        when(userService.getAllUsers()).thenReturn(mockList);

        ResponseEntity<List<UserResponse>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockList, response.getBody());
        verify(userService, times(1)).getAllUsers();
    }

    // U10 - Update user by ADMIN or EMPLOYEE
    @Test
    void updateUserByAdminOrEmployee_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        UserResponse mockUser = new UserResponse();
        mockUser.setId(userId);
        mockUser.setName("updatedByAdmin");

        ResponseMessage<UserResponse> mockResponse =
                ResponseMessage.<UserResponse>builder()
                        .httpStatus(HttpStatus.OK)
                        .message(SuccessMessages.USER_UPDATED)
                        .returnBody(mockUser)
                        .build();

        when(userService.updateUserByAdminOrEmployee(eq(userId), any(UserUpdateRequest.class))).thenReturn(mockResponse);

        ResponseEntity<ResponseMessage<UserResponse>> response =
                userController.updateUserByAdminOrEmployee(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUser, response.getBody().getReturnBody());
        verify(userService, times(1)).updateUserByAdminOrEmployee(userId, request);
    }

    // U11 - Delete user by ADMIN or EMPLOYEE
    @Test
    void deleteUserByAdminOrEmployee_ShouldReturnDeletedUser() {
        Long userId = 1L;
        UserResponse mockUser = new UserResponse();
        mockUser.setId(userId);
        mockUser.setName("deletedByAdmin");

        ResponseMessage<UserResponse> mockResponse =
                ResponseMessage.<UserResponse>builder()
                        .httpStatus(HttpStatus.OK)
                        .message(SuccessMessages.USER_DELETED)
                        .returnBody(mockUser)
                        .build();

        when(userService.deleteUserByAdminOrEmployee(userId)).thenReturn(mockResponse);

        ResponseEntity<ResponseMessage<UserResponse>> response =
                userController.deleteUserByAdminOrEmployee(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUser, response.getBody().getReturnBody());
        verify(userService, times(1)).deleteUserByAdminOrEmployee(userId);
    }
}

