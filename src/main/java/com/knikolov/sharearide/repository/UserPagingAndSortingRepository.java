package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserPagingAndSortingRepository extends PagingAndSortingRepository<User, String> {



}
