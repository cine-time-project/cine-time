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

  public User loadByLoginProperty(String propValue) {
    return userRepository.findByLoginProperty(propValue)
        .orElseThrow(()-> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD, propValue)));
  }

}
