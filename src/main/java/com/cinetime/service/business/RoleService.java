package com.cinetime.service.business;

import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Finds a role by RoleName enum (e.g., ADMIN, MEMBER).
     */
    public Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }

    /**
     * Finds a role by string (useful when request comes with a string role name).
     */
    public Role getRole(String roleName) {
        try {
            RoleName rn = RoleName.valueOf(roleName.toUpperCase());
            return getRole(rn);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }
}

 