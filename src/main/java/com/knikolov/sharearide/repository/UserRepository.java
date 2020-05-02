package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {

    // TODO: tests
    User findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query(value = "SELECT u FROM User u WHERE u.isDriver = true")
    List<User> findAllByDriverEqualsTrue();

    @Query(value = "SELECT u FROM User u WHERE u.isDriver = false AND u.isCompany = false")
    List<User> findAllByDriverEqualsFalse();

    List<User> findAllByUsernameContains(String username);

    List<User> findAllByUsernameContainsAndIsBlockedEquals(String username, Boolean blocked);
}
