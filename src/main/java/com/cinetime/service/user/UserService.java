package com.cinetime.service.user;

import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // U06 - Update Authenticated User
    public UserResponse updateAuthenticatedUser(UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = (User) userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_UPDATE_NOT_ALLOWED);
        }

        UserMapper.updateEntityFromRequest(request, user);
        userRepository.save(user);

        return UserMapper.toResponse(user);
    }

    // U07 - Delete Authenticated User
    public String deleteAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = (User) userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_DELETE_NOT_ALLOWED);
        }

        userRepository.delete(user);
        return SuccessMessages.USER_DELETED;
    }

    // U08 - Get Authenticated User
    public UserResponse getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = (User) userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        return UserMapper.toResponse(user);
    }

}




