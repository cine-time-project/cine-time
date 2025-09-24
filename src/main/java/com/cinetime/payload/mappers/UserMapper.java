package com.cinetime.payload.mappers;

import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.user.User;
import com.cinetime.exception.BadRequestException;

public class UserMapper {

    // UserUpdateRequest -> User
    public static void updateEntityFromRequest(UserUpdateRequest req, User user) {
        if (req.getName() != null) {
            user.setName(req.getName());
        }
        if (req.getSurname() != null) {
            user.setSurname(req.getSurname());
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(req.getPhoneNumber());
        }
        if (req.getBirthDate() != null) {
            user.setBirthDate(req.getBirthDate());
        }
        if (req.getGender() != null) {
            try {
                user.setGender(Gender.valueOf(req.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ErrorMessages.INVALID_GENDER);
            }
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



}
