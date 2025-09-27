package com.cinetime.payload.mappers;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.request.user.UserCreateRequest;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserCreateResponse;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.user.User;
import com.cinetime.exception.BadRequestException;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    // UserUpdateRequest -> User
    public static void updateEntityFromRequest(UserUpdateRequest req, User user) {
        if (req.getFirstName() != null) {
            user.setName(req.getFirstName());
        }
        if (req.getLastName() != null) {
            user.setSurname(req.getLastName());
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getPhone() != null) {
            user.setPhoneNumber(req.getPhone());
        }
        if (req.getBirthDate() != null) {
            user.setBirthDate(req.getBirthDate());
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
    }


    // User -> UserResponse
    public static UserResponse toResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setSurname(user.getSurname());
        resp.setEmail(user.getEmail());
        resp.setPhoneNumber(user.getPhoneNumber());
        resp.setBirthDate(user.getBirthDate());
        if (user.getGender() != null) {
            resp.setGender(user.getGender().name());
        }
        return resp;
    }

    // UserRegisterRequest -> User
    public static User fromRegisterRequest(UserRegisterRequest req) {
        User user = new User();
        user.setName(req.getFirstName());
        user.setSurname(req.getLastName());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhone());
        user.setPassword(req.getPassword());     // encode in service
        user.setBirthDate(req.getBirthDate());   // LocalDate
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
        return user;
    }

    // Request → Entity
    public User mapUserCreateRequestToUser(UserCreateRequest request) {
        return User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .password(request.getPassword()) // şifre encode edilecek service içinde
                .build();
    }

    // Entity → Response
    public UserCreateResponse mapUserToUserCreateResponse(User user) {
        Set<RoleName> roles = user.getRoles()
                .stream()
                .map(Role::getRoleName) // Role entity → RoleName enum
                .collect(Collectors.toSet());

        return UserCreateResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .builtIn(Boolean.TRUE.equals(user.getBuiltIn()))
                .roles(roles) //Enum olarak ekledik
                .build();
    }

    public static void updateUserFromRequest(UserUpdateRequest req, User user) {

        if (req.getFirstName() != null && !req.getFirstName().isBlank()) {
            user.setName(req.getFirstName().trim());
        }
        if (req.getLastName() != null && !req.getLastName().isBlank()) {
            user.setSurname(req.getLastName().trim());
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            user.setPhoneNumber(req.getPhone().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail().trim().toLowerCase());
        }
        if (req.getBirthDate() != null) {
            user.setBirthDate(req.getBirthDate());
        }
        if (req.getGender() != null) {
            // DTO zaten enum (Gender) ise direkt set et
            user.setGender(req.getGender());
        }

    }



}
