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
            // 1) Rolleri garanti altına al
            for (RoleName rn : RoleName.values()) {
                roleRepo.findByRoleName(rn).orElseGet(() -> roleRepo.save(new Role(rn)));
            }

            // 2) Kullanıcıları (yoksa) oluştur
            createAdmin(userRepo, roleRepo, passwordEncoder);
            createEmployee(userRepo, roleRepo, passwordEncoder);
            createMember(userRepo, roleRepo, passwordEncoder);
            createAnonymous(userRepo, roleRepo, passwordEncoder);
        };
    }

    private void createAdmin(UserRepository userRepo,
                             RoleRepository roleRepo,
                             PasswordEncoder passwordEncoder) {
        Role adminRole = roleRepo.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing"));

        String email = "admin@example.com";
        if (!userRepo.existsByEmail(email)) {
            User admin = User.builder()
                    .name("Admin")
                    .surname("User")
                    .email(email)
                    .password(passwordEncoder.encode("ChangeMe123!"))
                    .phoneNumber("(555) 123-4567")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .builtIn(true)              // silinmesin
                    .roles(Set.of(adminRole))
                    .build();
            userRepo.save(admin);
        }
    }

    private void createEmployee(UserRepository userRepo,
                                RoleRepository roleRepo,
                                PasswordEncoder passwordEncoder) {
        Role role = roleRepo.findByRoleName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE role missing"));

        String email = "employee@example.com";
        if (!userRepo.existsByEmail(email)) {
            User user = User.builder()
                    .name("Employee")
                    .surname("User")
                    .email(email)
                    .password(passwordEncoder.encode("Employee123!"))
                    .phoneNumber("(555) 222-3333")
                    .birthDate(LocalDate.of(1992, 2, 2))
                    .gender(Gender.MALE)
                    .builtIn(false)
                    .roles(Set.of(role))
                    .build();
            userRepo.save(user);
        }
    }

    private void createMember(UserRepository userRepo,
                              RoleRepository roleRepo,
                              PasswordEncoder passwordEncoder) {
        Role role = roleRepo.findByRoleName(RoleName.MEMBER)
                .orElseThrow(() -> new IllegalStateException("MEMBER role missing"));

        String email = "member@example.com";
        if (!userRepo.existsByEmail(email)) {
            User user = User.builder()
                    .name("Member")
                    .surname("User")
                    .email(email)
                    .password(passwordEncoder.encode("Member123!"))
                    .phoneNumber("(555) 111-2222")
                    .birthDate(LocalDate.of(1995, 5, 5))
                    .gender(Gender.FEMALE)
                    .builtIn(false)
                    .roles(Set.of(role))
                    .build();
            userRepo.save(user);
        }
    }

    private void createAnonymous(UserRepository userRepo,
                                 RoleRepository roleRepo,
                                 PasswordEncoder passwordEncoder) {
        Role role = roleRepo.findByRoleName(RoleName.ANONYMOUS)
                .orElseThrow(() -> new IllegalStateException("ANONYMOUS role missing"));

        String email = "anonymous@example.com";
        if (!userRepo.existsByEmail(email)) {
            User user = User.builder()
                    .name("Anonymous")
                    .surname("User")
                    .email(email)
                    .password(passwordEncoder.encode("Anonymous123!"))
                    .phoneNumber("(555) 000-0000")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .gender(Gender.MALE)
                    .builtIn(false)
                    .roles(Set.of(role))
                    .build();
            userRepo.save(user);
        }
    }
}
