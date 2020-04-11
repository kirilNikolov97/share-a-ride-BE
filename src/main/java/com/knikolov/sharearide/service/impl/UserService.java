package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final RouteStopRepository routeStopRepository;
    private final RatingRepository ratingRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, AddressRepository addressRepository, CityRepository cityRepository, RouteStopRepository routeStopRepository, RatingRepository ratingRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cityRepository = cityRepository;
        this.routeStopRepository = routeStopRepository;
        this.ratingRepository = ratingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(User user) {
        return this.userRepository.save(user);
    }

    public List<Address> getAddressesByUsername(String username) {
        return userRepository.findByUsername(username).getAddresses();
    }

    public Address getAddressById(String addressId) {
        return addressRepository.findById(addressId).orElse(null);
    }

    public Address addNewAddress(AddressDto addressDto, String username) {
        User user = userRepository.findByUsername(username);
        List<Address> addresses = user.getAddresses();

        Address newAddress = new Address();
        try {
            newAddress.setId(UUID.randomUUID().toString());
            newAddress.setCity(addressDto.getCity());
            newAddress.setDistrict(addressDto.getDistrict());
            newAddress.setStreet(addressDto.getStreet());
            newAddress.setAdditionalInfo(addressDto.getAdditionalInfo());
            newAddress.setLongitude(addressDto.getLongitude());
            newAddress.setLatitude(addressDto.getLatitude());

            addresses.add(newAddress);
            user.setAddresses(addresses);
            this.userRepository.save(user);

            return newAddress;
        } catch (TransactionSystemException constraintException) {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        }
    }

    public Address updateAddress(AddressDto address, String username) {
        Address currentAddress = this.addressRepository.findById(address.getId()).orElse(null);

        if(currentAddress != null) {
            try {
                currentAddress.setCity(address.getCity());
                currentAddress.setDistrict(address.getDistrict());
                currentAddress.setStreet(address.getStreet());
                currentAddress.setAdditionalInfo(address.getAdditionalInfo());
                currentAddress.setLatitude(address.getLatitude());
                currentAddress.setLongitude(address.getLongitude());

                return this.addressRepository.save(currentAddress);
            } catch (Exception e) {
                throw new IllegalArgumentException("Something went wrong. Try again later.");
            }
        } else {
            throw new IllegalArgumentException("This address is not present.");
        }
    }

    public Address deleteAddress(String addressId, String username) {
        User user = this.userRepository.findByUsername(username);
        List<Address> addresses = user.getAddresses();

        int toBeDeleted = -1;
        for(int i = 0; i < addresses.size(); i++) {
            if(addresses.get(i).getId().equals(addressId)) {
                toBeDeleted = i;
            }
        }

        if(toBeDeleted != -1) {
            Address deletedAddress = addresses.remove(toBeDeleted);
            user.setAddresses(addresses);
            this.userRepository.save(user);
            return deletedAddress;
        }

        return null;
    }

    public User becomeDriver(String name) {
        User currentUser = this.userRepository.findByUsername(name);
        currentUser.setDriver(true);
        return this.userRepository.save(currentUser);
    }

    public List<City> getAllCities() {
        return this.cityRepository.findAll();
    }

    public User getCompany(String username) {
        User user = this.userRepository.findByUsername(username);
        return this.userRepository.findById(user.getCompanyId()).orElse(null);
    }

    public Boolean changePassword(PasswordChange passwordChange, String username) {
        User user = this.userRepository.findByUsername(username);

        if (!passwordEncoder.matches(passwordChange.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is not correct!");
        }

        if (passwordEncoder.matches(passwordChange.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Your new password cannot be the same as your old password! Choose another password!");
        }

        user.setPassword(passwordEncoder.encode(passwordChange.getNewPassword()));
        this.userRepository.save(user);
        return true;
    }

    public RouteStop approveRoute(String routeStopId, String name) {
        RouteStop routeStop = this.routeStopRepository.getOne(routeStopId);
        routeStop.setApproved(true);
        return this.routeStopRepository.save(routeStop);
    }

    public RouteStop getRouteStopById(String routeStopId) {
        return this.routeStopRepository.findById(routeStopId).orElse(null);
    }

    public User getUserById(String userId) {
        return this.userRepository.findById(userId).orElse(null);
    }

    public Rating rateUser(String userId, Integer rating, String passengerUsername) {
        User passenger = this.userRepository.findByUsername(passengerUsername);

        List<String> driverRouteIds = this.routeStopRepository.findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("DRIVER", userId);
        List<String> passengerRouteIds = this.routeStopRepository.findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("PASSENGER", passenger.getId());

        boolean isNoCommonElements = Collections.disjoint(driverRouteIds, passengerRouteIds);

        if(isNoCommonElements) {
            throw new IllegalArgumentException("The driver never drove this passenger!");
        }

        Rating newRating = new Rating();
        newRating.setRatingId(new RatingId(userId, passenger.getId()));
        newRating.setDateRating(LocalDateTime.now());
        newRating.setRate(rating);

        return this.ratingRepository.save(newRating);
    }
}
