package com.cinetime.service.helper;

import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.User;
import com.cinetime.exception.ResourceNotFoundException;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final UserRepository userRepository;

    public User loadByLoginProperty(String propValue) {
        return userRepository.findByLoginProperty(propValue)
                .orElseThrow(()-> new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_MESSAGE_UNIQUE_FIELD, propValue)));
    }

    public boolean hasAuth(Authentication a, String auth) {
        if (a == null) return false;
        return a.getAuthorities().stream().anyMatch(gr -> gr.getAuthority().equals(auth));
    }

    public boolean isCallerAdmin(Authentication a){
        return hasAuth(a,"ADMIN") || hasAuth(a,"ROLE_ADMIN");
    }

    public boolean isCallerEmployee(Authentication a){
        return hasAuth(a,"EMPLOYEE") || hasAuth(a,"ROLE_EMPLOYEE");
    }

    public boolean userHasRole(User u, RoleName role){
        if (u == null || u.getRoles() == null) return false;
        return u.getRoles().stream().anyMatch(r -> r.getRoleName() == role);
    }
}
