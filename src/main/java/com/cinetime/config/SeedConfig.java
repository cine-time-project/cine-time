package com.cinetime.config;


import com.cinetime.entity.business.Role;
import com.cinetime.entity.enums.Gender;
import com.cinetime.entity.enums.RoleName;
import com.cinetime.entity.user.User;
import com.cinetime.repository.user.RoleRepository;
import com.cinetime.repository.user.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Configuration
public class SeedConfig {

    @Bean
    @Transactional
    ApplicationRunner seed(RoleRepository roleRepo,
                           UserRepository userRepo,
                           PasswordEncoder passwordEncoder) {
        return args -> {

            // 1) Rolls added
            for (RoleName rn : RoleName.values()) {
                roleRepo.findByRoleName(rn).orElseGet(() -> roleRepo.save(new Role(rn)));
            }

            // 2) Create admin if there is no user
            if (userRepo.count() == 0) {
                createAdmin(userRepo, roleRepo, passwordEncoder);
            }

        };
    }

    private void createAdmin(UserRepository userRepo,
                             RoleRepository roleRepo,
                             PasswordEncoder passwordEncoder) {

        Role adminRole = roleRepo.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing"));

        User admin = User.builder()
                .name("Admin")
                .surname("User")
                .email("admin@cinetime.local")
                .password(passwordEncoder.encode("ChangeMe123!"))  // Password
                .phoneNumber("(555) 123-4567")                     // Pattern: (XXX) XXX-XXXX
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)                               // from Enum
                .builtIn(true)                                     // Can't deleted
                .roles(Set.of(adminRole))
                .build();

        // E-posta must be unique
        if (!userRepo.existsByEmail(admin.getEmail())) {
            userRepo.save(admin);
        }
    }
}
