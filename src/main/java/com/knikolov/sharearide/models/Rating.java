package com.knikolov.sharearide.models;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class Rating implements Serializable {

//    @Id
//    @Column(name = "driver_id")
//    private String driverId;
//
//    @Id
//    @Column(name = "passenger_id")
//    private String passengerId;


    @EmbeddedId
    private RatingId ratingId;

    @Column
    private Integer rate;

    @Column(name = "date_rating")
    private LocalDateTime dateRating;

    public Rating() {
    }

    public Rating(RatingId ratingId, Integer rate, LocalDateTime dateRating) {
        this.ratingId = ratingId;
        this.rate = rate;
        this.dateRating = dateRating;
    }

    public RatingId getRatingId() {
        return ratingId;
    }

    public void setRatingId(RatingId ratingId) {
        this.ratingId = ratingId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public LocalDateTime getDateRating() {
        return dateRating;
    }

    public void setDateRating(LocalDateTime dateRating) {
        this.dateRating = dateRating;
    }
}
