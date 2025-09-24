package com.cinetime.repository.user;

import com.cinetime.entity.user.User;

import java.util.Optional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE u.email = :login OR u.phoneNumber = :login")
  Optional<User> findByLoginProperty(@Param("login") String login);


  boolean existsByEmail(String email);
  boolean existsByRoles_RoleName(com.cinetime.entity.enums.RoleName roleName);

  Optional<User> findByEmail(String email);
    // Optional<User> findByEmail(String email);  yukarıdaki metodu böyle değiştirme ihtiyacı olabilir

  boolean existsByPhoneNumber(String phoneNumber);
  Optional<User> findByEmailIgnoreCase(String email);

  Page<User> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          String name, String surname, String email, Pageable pageable
  );

}
