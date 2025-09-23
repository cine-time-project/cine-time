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
import org.springframework.jdbc.core.JdbcTemplate;
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
                           PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbc) {
        return args -> {

            // 1) Rolls added
            for (RoleName rn : RoleName.values()) {
                roleRepo.findByRoleName(rn).orElseGet(() -> roleRepo.save(new Role(rn)));
            }

            // 2) Create admin if there is no user
            if (userRepo.count() == 0) {
                createAdmin(userRepo, roleRepo, passwordEncoder);
            }

            seedCineTimeData(jdbc);

            createMember(userRepo, roleRepo, passwordEncoder);
            createEmployee(userRepo, roleRepo, passwordEncoder);
            createAnonymous(userRepo, roleRepo, passwordEncoder);

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
    private void createMember(UserRepository userRepo,
                              RoleRepository roleRepo,
                              PasswordEncoder passwordEncoder) {
        Role role = roleRepo.findByRoleName(RoleName.MEMBER)
                .orElseThrow(() -> new IllegalStateException("MEMBER role missing"));

        User user = User.builder()
                .name("Member")
                .surname("User")
                .email("member@cinetime.local")
                .password(passwordEncoder.encode("Member123!"))
                .phoneNumber("(555) 111-2222")
                .birthDate(LocalDate.of(1995, 5, 5))
                .gender(Gender.FEMALE)
                .builtIn(false)
                .roles(Set.of(role))
                .build();

        if (!userRepo.existsByEmail(user.getEmail())) {
            userRepo.save(user);
        }
    }

    private void createEmployee(UserRepository userRepo,
                                RoleRepository roleRepo,
                                PasswordEncoder passwordEncoder) {
        Role role = roleRepo.findByRoleName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE role missing"));

        User user = User.builder()
                .name("Employee")
                .surname("User")
                .email("employee@cinetime.local")
                .password(passwordEncoder.encode("Employee123!"))
                .phoneNumber("(555) 222-3333")
                .birthDate(LocalDate.of(1992, 2, 2))
                .gender(Gender.MALE)
                .builtIn(false)
                .roles(Set.of(role))
                .build();

        if (!userRepo.existsByEmail(user.getEmail())) {
            userRepo.save(user);
        }
    }

    private void createAnonymous(UserRepository userRepo,
                                 RoleRepository roleRepo,
                                 PasswordEncoder passwordEncoder) {
        Role role = roleRepo.findByRoleName(RoleName.ANONYMOUS)
                .orElseThrow(() -> new IllegalStateException("ANONYMOUS role missing"));

        User user = User.builder()
                .name("Anonymous")
                .surname("User")
                .email("anonymous@cinetime.local")
                .password(passwordEncoder.encode("Anonymous123!"))
                .phoneNumber("(555) 000-0000")
                .birthDate(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .builtIn(false)
                .roles(Set.of(role))
                .build();

        if (!userRepo.existsByEmail(user.getEmail())) {
            userRepo.save(user);
        }
    }
    private void seedCineTimeData(JdbcTemplate jdbc) {
        // -- 1) Country
        jdbc.update("""
            INSERT INTO country (name)
            VALUES ('USA')
            ON CONFLICT (name) DO NOTHING
        """);

        // -- 2) City (Miami) under USA
        jdbc.update("""
            INSERT INTO city (name, country_id)
            SELECT 'Miami', c.id
            FROM country c
            WHERE c.name = 'USA'
            ON CONFLICT (name) DO NOTHING
        """);

        // -- 3) Cinema (note: quoted mixed-case columns if your schema really has them)
        jdbc.update("""
            INSERT INTO cinemas (name, slug, "created_at", "updated_at")
            SELECT 'CineTime Downtown', 'cinetime-downtown', NOW(), NOW()
            WHERE NOT EXISTS (
              SELECT 1 FROM cinemas WHERE name = 'CineTime Downtown'
            )
        """);

        // -- 4) Hall 1 @ CineTime Downtown
        jdbc.update("""
            INSERT INTO halls (name, seat_capacity, is_special, cinema_id, created_at, updated_at)
            SELECT 'Hall 1', 100, FALSE, ci.id, NOW(), NOW()
            FROM cinemas ci
            WHERE ci.name = 'CineTime Downtown'
              AND NOT EXISTS (
                SELECT 1 FROM halls h WHERE h.name = 'Hall 1' AND h.cinema_id = ci.id
              )
        """);

        // -- 5) Movie: Fight Club
        jdbc.update("""
            INSERT INTO movies (
              created_at,
              title,
              director,
              duration,
              rating,
              release_date,
              slug,
              special_halls,
              status,
              summary,
              updated_at
            )
            SELECT
              NOW(),
              'Fight Club',
              'David Fincher',
              139,
              8.8,
              DATE '1999-10-15',
              'fight-club',
              'Standard',
              'COMING_SOON',
              'An insomniac office worker and a soap maker form an underground fight club that evolves into something much more.',
              NOW()
            WHERE NOT EXISTS (
              SELECT 1 FROM movies WHERE slug = 'fight-club'
            )
        """);

        // -- 6) Showtime for Fight Club @ Hall 1 (CTE insert; safe if already present)
        jdbc.update("""
            WITH movie_cte AS (
              SELECT id AS movie_id, duration
              FROM movies
              WHERE slug = 'fight-club' OR title = 'Fight Club'
              LIMIT 1
            ),
            hall_cte AS (
              SELECT h.id AS hall_id
              FROM halls h
              JOIN cinemas c ON c.id = h.cinema_id
              WHERE c.name = 'CineTime Downtown' AND h.name = 'Hall 1'
              LIMIT 1
            ),
            times AS (
              SELECT
                DATE '2025-09-25' AS show_date,
                TIME '19:30:00'   AS start_time,
                (TIME '19:30:00' + make_interval(mins => COALESCE(m.duration, 120))) AS end_time,
                m.movie_id,
                h.hall_id
              FROM movie_cte m
              CROSS JOIN hall_cte h
            )
            INSERT INTO showtimes (created_at, date, end_time, hall_id, movie_id, start_time, updated_at)
            SELECT NOW(), t.show_date, t.end_time, t.hall_id, t.movie_id, t.start_time, NOW()
            FROM times t
            WHERE NOT EXISTS (
              SELECT 1 FROM showtimes s
              WHERE s.movie_id   = t.movie_id
                AND s.hall_id    = t.hall_id
                AND s.date       = t.show_date
                AND s.start_time = t.start_time
            )
        """);

    }
}
