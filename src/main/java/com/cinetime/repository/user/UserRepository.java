package com.cinetime.repository.user;

import com.cinetime.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE u.email = :login OR u.phoneNumber = :login")
  Optional<User> findByLoginProperty(@Param("login") String login);


  boolean existsByEmail(String email);
  boolean existsByRoles_RoleName(com.cinetime.entity.enums.RoleName roleName);

    Optional<User> findByEmail(String username); //Changed <Object> --> <User>

  boolean existsByPhoneNumber(String phoneNumber);
  Optional<User> findByEmailIgnoreCase(String email);

  @Query("select u from User u where lower(u.email)=lower(:email)")
  Optional<User> findByEmailForReset(@Param("email") String email);



  Page<User> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          String name, String surname, String email, Pageable pageable
  );

  Page<User> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
          String name, String surname, String email, String phone, Pageable pageable);


}
