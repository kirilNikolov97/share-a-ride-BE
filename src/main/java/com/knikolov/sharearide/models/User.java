package com.knikolov.sharearide.models;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
public class User implements Serializable {

    @Id
    @Column
    private String id;

    @Column
    private String username;

    @Column
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private String phone;

    @Column
    private String email;

    @Column(name = "is_driver")
    private boolean isDriver;

    @Column
    private LocalDateTime created;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "is_company")
    private Boolean isCompany;

    @Column(name = "picture_url")
    private String pictureUrl;

    @OneToMany(mappedBy = "ratingId.driverId", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private List<Rating> ratings;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_user", joinColumns = {
            @JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = {
            @JoinColumn(name = "role_id", referencedColumnName = "role_id") })
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinTable(name = "user_address", joinColumns = {
            @JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = {
            @JoinColumn(name = "address_id", referencedColumnName = "address_id") })
    private List<Address> addresses;

    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "userId")
    private List<Car> cars;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String username, String email, String encode, String firstName, String lastName, String phone, Boolean isCompany) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.password = encode;
        this.created = LocalDateTime.now();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.isCompany = isCompany;
    }

    public User(String id, String username, String email, String encode, String firstName, String lastName, String phone, Boolean isCompany, Boolean isDriver) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = encode;
        this.created = LocalDateTime.now();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.isCompany = isCompany;
        this.isDriver = isDriver;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isDriver() {
        return isDriver;
    }

    public void setDriver(boolean driver) {
        isDriver = driver;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Boolean getCompany() {
        return isCompany;
    }

    public void setCompany(Boolean company) {
        isCompany = company;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }


}
