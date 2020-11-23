package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Rating;
import com.knikolov.sharearide.models.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Rating entity
 */
public interface RatingRepository extends JpaRepository<Rating, RatingId> {
}
