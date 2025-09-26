package com.cinetime.service.helper;

import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SecurityHelper {

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
