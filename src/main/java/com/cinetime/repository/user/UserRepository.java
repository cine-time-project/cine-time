package com.cinetime.repository.user;

import com.cinetime.entity.user.User;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE u.email = :login OR u.phoneNumber = :login")
  Optional<User> findByLoginProperty(@Param("login") String login);


}
