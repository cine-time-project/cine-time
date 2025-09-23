package com.cinetime.payload.mappers;

import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.payload.request.user.UserRegisterRequest;
import com.cinetime.payload.request.user.UserUpdateRequest;
import com.cinetime.payload.response.user.UserResponse;
import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.user.User;
import com.cinetime.exception.BadRequestException;

public class UserMapper {

    public static void updateEntityFromRequest(UserUpdateRequest request, User user) {
        if (request.getName() != null) user.setName(request.getName());
        if (request.getSurname() != null) user.setSurname(request.getSurname());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());

        if (request.getGender() != null) {
            try {
                user.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ErrorMessages.INVALID_GENDER);
            }
        }
    }

    public static UserResponse toResponse(User user) {
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
