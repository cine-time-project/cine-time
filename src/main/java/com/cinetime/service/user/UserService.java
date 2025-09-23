package com.cinetime.service.user;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.payload.mappers.UserMapper;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.messages.SuccessMessages;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.business.ResponseMessage;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ConflictException;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.repository.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;


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


    // U02 - User Register
        public UserResponse saveUser(UserRegisterRequest req) {

            // 1) unique
            if (userRepository.existsByEmail(req.getEmail()))
                throw new ConflictException("Email already in use");

            if (userRepository.existsByPhoneNumber(req.getPhone()))
                throw new ConflictException("Phone already in use");

            // 2) request -> entity (STATIC mapper)
            User user = UserMapper.fromRegisterRequest(req);   // <<< static call
            user.setEmail(user.getEmail().trim().toLowerCase());

            // 3) encode password
            user.setPassword(encoder.encode(user.getPassword()));

            // 4) default role = MEMBER
            Role member = roleRepository.findByRoleName(RoleName.MEMBER)
                    .orElseThrow(() -> new IllegalStateException("MEMBER role missing"));
            user.setRoles(Set.of(member));

            // 5) save + map to response (STATIC mapper)
            User saved = userRepository.save(user);
            return UserMapper.toResponse(saved);               // <<< static call while mapper is not component
        }
    }





