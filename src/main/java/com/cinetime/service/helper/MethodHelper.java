package com.cinetime.service.helper;

import com.cinetime.entity.user.User;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MethodHelper {

  private final UserRepository userRepository;

  public User loadByPhoneNumber(String phoneNumber) {
    User user = userRepository.findByPhoneNumber(phoneNumber);
    if (user == null) {
      throw new ResourceNotFoundException(
          String.format(ErrorMessages.NOT_FOUND_USER_MESSAGE_PHONE_NUMBER, phoneNumber));
    }
    return user;
  }

}
