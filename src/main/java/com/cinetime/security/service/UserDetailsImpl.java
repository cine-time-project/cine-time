package com.cinetime.security.service;

import com.cinetime.entity.business.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

  private Long id;

  private String phoneNumber;

  @JsonIgnore
  private String password;

  private List<GrantedAuthority> authorities;

  public UserDetailsImpl(Long id, String phoneNumber, String password,
      Set<Role> roles) {
    this.id = id;
    this.phoneNumber = phoneNumber;
    this.password = password;
    List<GrantedAuthority> authorities = new ArrayList<>();
    //Converting Roles with their String values and adding them into authorities
    roles.stream()
        .map(t -> t.getRoleName().name())
        .forEach(t -> authorities.add(new SimpleGrantedAuthority(t)));
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return phoneNumber;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
