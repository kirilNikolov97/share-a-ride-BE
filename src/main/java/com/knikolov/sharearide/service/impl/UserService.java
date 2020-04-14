package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.dto.UserDto;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import com.knikolov.sharearide.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final RouteStopRepository routeStopRepository;
    private final RatingRepository ratingRepository;
    private final RouteRepository routeRepository;
    private final EmailService emailService;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, AddressRepository addressRepository, CityRepository cityRepository,
                       RouteStopRepository routeStopRepository, RatingRepository ratingRepository,
                       RouteRepository routeRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cityRepository = cityRepository;
        this.routeStopRepository = routeStopRepository;
        this.ratingRepository = ratingRepository;
        this.routeRepository = routeRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(User user) {
        User dbUser = this.userRepository.findByUsername(user.getUsername());
        user.setPassword(dbUser.getPassword());

        try {
            return this.userRepository.save(user);
        } catch (Exception e) {
            if (e.getMessage().contains("email")) {
                throw new IllegalArgumentException("Email is already taken.");
            } else {
                throw new IllegalArgumentException("Something went wrong. Try again later.");
            }
        }
    }

    public List<Address> getAddressesByUsername(String username) {
        return userRepository.findByUsername(username).getAddresses().stream().filter( address -> !address.getDeleted()).collect(Collectors.toList());
    }

    public Address getAddressById(String addressId, String username) {
        User user = this.userRepository.findByUsername(username);
        if (user.getAddresses().stream().noneMatch(addrs -> addrs.getId().equals(addressId))) {
            throw new IllegalArgumentException("This address can not be found in your profile.");
        }

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
            newAddress.setDeleted(false);

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
        User user = this.userRepository.findByUsername(username);
        if (user.getAddresses().stream().noneMatch(addrs -> addrs.getId().equals(address.getId()))) {
            throw new IllegalArgumentException("This address can not be found in your profile.");
        }

        if(currentAddress != null) {
            try {
                currentAddress.setCity(address.getCity());
                currentAddress.setDistrict(address.getDistrict());
                currentAddress.setStreet(address.getStreet());
                currentAddress.setAdditionalInfo(address.getAdditionalInfo());
                currentAddress.setLatitude(address.getLatitude());
                currentAddress.setLongitude(address.getLongitude());
                currentAddress.setDeleted(false);

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
        for (int i = 0; i < addresses.size(); i++) {
            if(addresses.get(i).getId().equals(addressId)) {
                toBeDeleted = i;
            }
        }

        if (toBeDeleted == -1) {
            throw new IllegalArgumentException("No such address in your profile");
        }

        Address deletedAddress = addresses.get(toBeDeleted);

        List<Route> futureRoutes = routeRepository.findAllFutureRoutesByUserIdAsDriver(user, LocalDateTime.now());
        futureRoutes.addAll(routeRepository.findAllFutureRoutesByUserIdAsPassenger(user, LocalDateTime.now()));

        if (futureRoutes.size() > 0) {
            for (int i = 0; i <  futureRoutes.size(); i++) {
                if (futureRoutes.get(i).getRouteStops().stream().filter(rs -> {
                    if (rs.getAddress().getId().equals(deletedAddress.getId())) {
                        return true;
                    }
                    return false;
                }).count() > 0) {
                    throw new IllegalArgumentException("The address is assigned to future route. Change the address for the route and then delete this address");
                }
            }
        }

        deletedAddress.setDeleted(true);

        addresses.get(toBeDeleted).setDeleted(true);
        user.setAddresses(addresses);
        this.userRepository.save(user);

        return this.addressRepository.save(deletedAddress);
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

    public RouteStop approveOrDeclineRoute(String routeStopId, String driverUsername, boolean approved) {
        RouteStop routeStop = this.routeStopRepository.getOne(routeStopId);
        if (!approved) {
            this.routeStopRepository.delete(routeStop);
            this.emailService.sendEmailResponseForSavedSeat(routeStop.getUserId().getEmail(), driverUsername, false);
            return null;
        } else {
            routeStop.setApproved(true);

            RouteStop saved = this.routeStopRepository.save(routeStop);
            this.emailService.sendEmailResponseForSavedSeat(routeStop.getUserId().getEmail(), driverUsername, true);
            return saved;
        }
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

    public UserDto userToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setDriver(user.isDriver());
        dto.setCreated(user.getCreated());
        dto.setCompanyId(user.getCompanyId());
        dto.setRatings(user.getRatings());
        dto.setRoles(user.getRoles());
        dto.setAddresses(user.getAddresses());
        dto.setCars(user.getCars());
        dto.setCompany(user.getCompany());

        return dto;
    }
}
