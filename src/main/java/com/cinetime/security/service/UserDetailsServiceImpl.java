package com.cinetime.security.service;

import com.cinetime.entity.user.User;
import com.cinetime.service.helper.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final SecurityHelper securityHelper;

  @Override
  //Since this method is provided by UserDetailsService, we cant change its name,
  // but it works fine with other properties such as phoneNumber
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = securityHelper.loadByLoginProperty(username);
    String usernameToBeAssigned =
        (user.getPhoneNumber().equals(username)) ? user.getPhoneNumber() : user.getEmail();
    return new UserDetailsImpl(
        user.getId(),
        usernameToBeAssigned,
        user.getPassword(),
        user.getRoles());
  }

}
