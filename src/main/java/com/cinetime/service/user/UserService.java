package com.cinetime.service.user;


import com.cinetime.controller.user.payload.mappers.UserMapper;
import com.cinetime.controller.user.payload.messages.ErrorMessages;
import com.cinetime.controller.user.payload.messages.SuccessMessages;
import com.cinetime.controller.user.payload.request.user.UserUpdateRequest;
import com.cinetime.controller.user.payload.response.user.UserResponse;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;


import com.cinetime.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    // TODO: When security is added, ensure that the given userId matches the authenticated user's ID
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new ConflictException(ErrorMessages.BUILT_IN_USER_UPDATE_NOT_ALLOWED);
        }

        UserMapper.updateEntityFromRequest(request, user);
        userRepository.save(user);

        System.out.println(SuccessMessages.USER_UPDATED);
        return UserMapper.toResponse(user);
    }
}


