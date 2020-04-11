package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

}
