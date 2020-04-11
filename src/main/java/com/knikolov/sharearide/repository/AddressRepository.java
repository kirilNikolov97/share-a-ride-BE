package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
}
