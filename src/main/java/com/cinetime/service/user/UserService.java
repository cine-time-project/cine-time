package com.cinetime.service.user;

import com.cinetime.config.entity.enums.Gender;
import com.cinetime.config.entity.user.User;
import com.cinetime.controller.user.payload.request.user.UserUpdateRequest;
import com.cinetime.controller.user.payload.response.user.UserResponse;
import com.cinetime.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getBuiltIn())) {
            throw new RuntimeException("Built-in users cannot be updated");
        }

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBirthDate(request.getBirthDate());

        if (request.getGender() != null) {
            user.setGender(parseGender(request.getGender()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return mapToResponse(user);
    }

    private Gender parseGender(String genderStr) {
        try {
            return Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid gender value. Use MALE, FEMALE or OTHER.");
        }
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setBirthDate(user.getBirthDate());
        response.setGender(user.getGender() != null ? user.getGender().name() : null);
        return response;
    }
}

