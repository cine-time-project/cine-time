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
        // =========================
        // BASE GEO (idempotent)
        // =========================

        // 1) District (must exist before Country because country.district_id is NOT NULL)
        jdbc.update("""
        INSERT INTO district (name)
        VALUES ('Default District')
        ON CONFLICT (name) DO NOTHING
    """);

        // 2) Country: USA under Default District
        jdbc.update("""
        INSERT INTO country (name, district_id)
        SELECT 'USA', d.id
        FROM district d
        WHERE d.name = 'Default District'
        ON CONFLICT (name) DO UPDATE
        SET district_id = EXCLUDED.district_id
        WHERE country.district_id IS DISTINCT FROM EXCLUDED.district_id
    """);

        // 3) Cities under USA
        jdbc.update("""
        INSERT INTO city (name, country_id)
        SELECT cty, c.id
        FROM (VALUES ('Miami'),('Los Angeles'),('New York'),('Philadelphia')) v(cty)
        JOIN country c ON c.name = 'USA'
        ON CONFLICT (name) DO NOTHING
    """);

        // =========================
        // CINEMAS (idempotent; each tied to a city_id)
        // (You can change which city each cinema belongs to)
        // =========================

        // CineTime Downtown @ Miami
        jdbc.update("""
        INSERT INTO cinemas (name, slug, city_id, created_at, updated_at)
        SELECT 'CineTime Downtown', 'cinetime-downtown', ci.id, NOW(), NOW()
        FROM city ci
        WHERE ci.name = 'Miami'
          AND NOT EXISTS (
            SELECT 1 FROM cinemas c WHERE c.name = 'CineTime Downtown' AND c.city_id = ci.id
          )
    """);

        // CineTime Midtown @ New York
        jdbc.update("""
        INSERT INTO cinemas (name, slug, city_id, created_at, updated_at)
        SELECT 'CineTime Midtown', 'cinetime-midtown', ci.id, NOW(), NOW()
        FROM city ci
        WHERE ci.name = 'New York'
          AND NOT EXISTS (
            SELECT 1 FROM cinemas c WHERE c.name = 'CineTime Midtown' AND c.city_id = ci.id
          )
    """);

        // CineTime Beachside @ Los Angeles
        jdbc.update("""
        INSERT INTO cinemas (name, slug, city_id, created_at, updated_at)
        SELECT 'CineTime Beachside', 'cinetime-beachside', ci.id, NOW(), NOW()
        FROM city ci
        WHERE ci.name = 'Los Angeles'
          AND NOT EXISTS (
            SELECT 1 FROM cinemas c WHERE c.name = 'CineTime Beachside' AND c.city_id = ci.id
          )
    """);

        // =========================
        // HALLS (Halls 1–4 for each cinema, idempotent)
        // =========================
        jdbc.update("""
        WITH halls_to_add(cinema_name, hall_name, seat_capacity, is_special) AS (
          VALUES
          ('CineTime Downtown','Hall 1',100,false),
          ('CineTime Downtown','Hall 2',120,false),
          ('CineTime Downtown','Hall 3',80,false),
          ('CineTime Downtown','Hall 4',150,true),
          ('CineTime Midtown','Hall 1',110,false),
          ('CineTime Midtown','Hall 2',130,false),
          ('CineTime Midtown','Hall 3',90,false),
          ('CineTime Midtown','Hall 4',140,true),
          ('CineTime Beachside','Hall 1',100,false),
          ('CineTime Beachside','Hall 2',120,false),
          ('CineTime Beachside','Hall 3',85,false),
          ('CineTime Beachside','Hall 4',160,true)
        )
        INSERT INTO halls (name, seat_capacity, is_special, cinema_id, created_at, updated_at)
        SELECT hta.hall_name, hta.seat_capacity, hta.is_special, ci.id, NOW(), NOW()
        FROM halls_to_add hta
        JOIN cinemas ci ON ci.name = hta.cinema_name
        WHERE NOT EXISTS (
          SELECT 1 FROM halls h WHERE h.name = hta.hall_name AND h.cinema_id = ci.id
        )
    """);

        // =========================
        // MOVIES: upsert 10 titles; force status = IN_THEATERS (idempotent)
        // =========================
        jdbc.update("""
        WITH new_movies(title, director, duration, rating, release_date, slug, special_halls, status, summary) AS (
          VALUES
          ('Fight Club','David Fincher',139,8.8,DATE '1999-10-15','fight-club','Standard','IN_THEATERS','An insomniac office worker and a soap maker form an underground fight club.'),
          ('Inception','Christopher Nolan',148,8.8,DATE '2010-07-16','inception','Standard','IN_THEATERS','A thief must plant an idea inside a CEO''s mind.'),
          ('The Matrix','The Wachowskis',136,8.7,DATE '1999-03-31','the-matrix','Standard','IN_THEATERS','A hacker learns the truth about reality.'),
          ('Interstellar','Christopher Nolan',169,8.6,DATE '2014-11-07','interstellar','Standard','IN_THEATERS','Explorers travel through a wormhole to save humanity.'),
          ('The Dark Knight','Christopher Nolan',152,9.0,DATE '2008-07-18','the-dark-knight','Standard','IN_THEATERS','Batman faces the Joker''s chaos in Gotham.'),
          ('Pulp Fiction','Quentin Tarantino',154,8.9,DATE '1994-10-14','pulp-fiction','Standard','IN_THEATERS','Interwoven tales of crime and redemption.'),
          ('The Shawshank Redemption','Frank Darabont',142,9.3,DATE '1994-09-23','shawshank-redemption','Standard','IN_THEATERS','Two imprisoned men bond and find redemption.'),
          ('The Godfather','Francis Ford Coppola',175,9.2,DATE '1972-03-24','the-godfather','Standard','IN_THEATERS','A crime dynasty transfers power to a reluctant son.'),
          ('Parasite','Bong Joon-ho',132,8.6,DATE '2019-05-30','parasite','Standard','IN_THEATERS','A poor family infiltrates a wealthy household.'),
          ('Spirited Away','Hayao Miyazaki',125,8.6,DATE '2001-07-20','spirited-away','Standard','IN_THEATERS','A girl enters a spirit world to save her parents.')
        )
        INSERT INTO movies (
          created_at, title, director, duration, rating, release_date,
          slug, special_halls, status, summary, updated_at
        )
        SELECT NOW(), nm.title, nm.director, nm.duration, nm.rating, nm.release_date,
               nm.slug, nm.special_halls, nm.status, nm.summary, NOW()
        FROM new_movies nm
        ON CONFLICT (slug) DO UPDATE
        SET status = 'IN_THEATERS',
            updated_at = EXCLUDED.updated_at
    """);

        // =========================
        // SHOWTIMES (future dates, different cinemas/halls, idempotent)
        // =========================
        jdbc.update("""
        WITH mapping(slug, cinema_name, hall_name, show_date, start_time) AS (
          VALUES
          ('fight-club',           'CineTime Downtown',  'Hall 1', DATE '2025-10-02', TIME '19:30:00'),
          ('inception',            'CineTime Midtown',   'Hall 2', DATE '2025-10-03', TIME '21:30:00'),
          ('the-matrix',           'CineTime Beachside', 'Hall 3', DATE '2025-10-04', TIME '16:00:00'),
          ('interstellar',         'CineTime Downtown',  'Hall 4', DATE '2025-10-05', TIME '19:30:00'),
          ('the-dark-knight',      'CineTime Midtown',   'Hall 1', DATE '2025-10-06', TIME '13:00:00'),
          ('pulp-fiction',         'CineTime Beachside', 'Hall 2', DATE '2025-10-07', TIME '19:30:00'),
          ('shawshank-redemption', 'CineTime Downtown',  'Hall 3', DATE '2025-10-08', TIME '16:00:00'),
          ('the-godfather',        'CineTime Midtown',   'Hall 4', DATE '2025-10-09', TIME '19:30:00'),
          ('parasite',             'CineTime Beachside', 'Hall 1', DATE '2025-10-10', TIME '18:00:00'),
          ('spirited-away',        'CineTime Downtown',  'Hall 2', DATE '2025-10-11', TIME '14:00:00')
        ),
        resolved AS (
          SELECT
            mv.id AS movie_id,
            h.id  AS hall_id,
            mp.show_date,
            mp.start_time,
            (mp.start_time + make_interval(mins => COALESCE(mv.duration, 120)))::time AS end_time
          FROM mapping mp
          JOIN movies  mv ON mv.slug = mp.slug
          JOIN cinemas c  ON c.name = mp.cinema_name
          JOIN halls   h  ON h.cinema_id = c.id AND h.name = mp.hall_name
        )
        INSERT INTO showtimes (created_at, date, end_time, hall_id, movie_id, start_time, updated_at)
        SELECT NOW(), r.show_date, r.end_time, r.hall_id, r.movie_id, r.start_time, NOW()
        FROM resolved r
        WHERE NOT EXISTS (
          SELECT 1 FROM showtimes s
          WHERE s.movie_id   = r.movie_id
            AND s.hall_id    = r.hall_id
            AND s.date       = r.show_date
            AND s.start_time = r.start_time
        )
    """);

        // (Optional) Verify rows exist (use in SQL console, not in code)
        // SELECT s.id, m.title, c.name AS cinema, h.name AS hall, s.date, s.start_time, s.end_time
        // FROM showtimes s
        // JOIN movies  m ON m.id = s.movie_id
        // JOIN halls   h ON h.id = s.hall_id
        // JOIN cinemas c ON c.id = h.cinema_id
        // ORDER BY s.date, s.start_time;
    }}
