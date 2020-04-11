package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.enums.ERole;
import com.knikolov.sharearide.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByName(ERole name);

}
