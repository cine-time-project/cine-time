package com.cinetime.security.service;

import com.cinetime.entity.user.User;
import com.cinetime.service.helper.MethodHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final MethodHelper methodHelper;

  @Override
  //Since this method is provided by UserDetailsService, we cant change its name,
  // but it works fine with other properties such as phoneNumber
  public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
    User user = methodHelper.loadByPhoneNumber(phoneNumber);
    return new UserDetailsImpl(
        user.getId(),
        user.getPhoneNumber(),
        user.getPassword(),
        user.getRoles());
  }

}
