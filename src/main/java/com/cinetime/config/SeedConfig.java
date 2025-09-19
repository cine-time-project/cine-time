package com.cinetime.config;


import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.repository.user.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class SeedConfig {

    @Bean
    @Transactional
    ApplicationRunner seedRoles(RoleRepository repo) {
        return args -> {
            for (RoleName rn : RoleName.values()) {
                if (!repo.existsByRoleName(rn)) {
                    repo.save(new Role(rn));
                }
            }
        };
    }
}
