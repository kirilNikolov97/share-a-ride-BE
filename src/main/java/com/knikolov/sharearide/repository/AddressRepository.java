package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Address entity
 */
public interface AddressRepository extends JpaRepository<Address, String> {
}
