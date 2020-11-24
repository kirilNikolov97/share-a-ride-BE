package com.knikolov.sharearide.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.dto.UserDto;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import com.knikolov.sharearide.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserService implementation
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RouteStopRepository routeStopRepository;
    private final RatingRepository ratingRepository;
    private final RouteRepository routeRepository;
    private final EmailService emailService;
    private final Cloudinary cloudinary;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, AddressRepository addressRepository,
                           RouteStopRepository routeStopRepository, RatingRepository ratingRepository,
                           RouteRepository routeRepository, EmailService emailService,
                           Cloudinary cloudinary, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.routeStopRepository = routeStopRepository;
        this.ratingRepository = ratingRepository;
        this.routeRepository = routeRepository;
        this.emailService = emailService;
        this.cloudinary = cloudinary;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User update(UserDto userDto) {
        User user = userDtoToUser(userDto);

        User dbUser = userRepository.findByUsername(user.getUsername());
        user.setPassword(dbUser.getPassword());
        user.setPictureUrl(dbUser.getPictureUrl());

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            if (e.getMessage().contains("email")) {
                throw new IllegalArgumentException("Email is already taken.");
            } else {
                throw new IllegalArgumentException("Something went wrong. Try again later.");
            }
        }
    }

    @Override
    public String uploadPicture(MultipartFile file, String username) {
        User user = userRepository.findByUsername(username);
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("url");
            user.setPictureUrl(url);
            userRepository.save(user);
            return "Success";
        } catch (IOException e) {
            e.printStackTrace();
            return "Something went wrong.";
        }
    }

    @Override
    public List<Address> getAddressesByUsername(String username) {
        return userRepository.findByUsername(username).getAddresses().stream()
                .filter(address -> !address.getDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public Address getAddressById(String addressId, String username) {
        return addressRepository.findById(addressId).orElse(null);
    }

    @Override
    public Address createAddress(AddressDto addressDto, String username) {
        User user = userRepository.findByUsername(username);
        List<Address> addresses = user.getAddresses();

        Address newAddress = new Address();
        newAddress.setId(UUID.randomUUID().toString());
        newAddress.setDistrict(addressDto.getDistrict());
        newAddress.setStreet(addressDto.getStreet());
        newAddress.setAdditionalInfo(addressDto.getAdditionalInfo());
        newAddress.setLongitude(addressDto.getLongitude());
        newAddress.setLatitude(addressDto.getLatitude());
        newAddress.setDeleted(false);
        addresses.add(newAddress);
        user.setAddresses(addresses);

        try {
            userRepository.save(user);
            return newAddress;
        } catch (TransactionSystemException constraintException) {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        }
    }

    @Override
    public Address updateAddress(AddressDto address, String username) {
        Address currentAddress = addressRepository.findById(address.getId()).orElse(null);
        User user = userRepository.findByUsername(username);

        if (user.getAddresses().stream().noneMatch(addrs -> addrs.getId().equals(address.getId()))) {
            throw new IllegalArgumentException("This address can not be found in your profile.");
        }

        if (currentAddress != null) {
            currentAddress.setDistrict(address.getDistrict());
            currentAddress.setStreet(address.getStreet());
            currentAddress.setAdditionalInfo(address.getAdditionalInfo());
            currentAddress.setLatitude(address.getLatitude());
            currentAddress.setLongitude(address.getLongitude());
            currentAddress.setDeleted(false);

            try {
                return addressRepository.save(currentAddress);
            } catch (Exception e) {
                throw new IllegalArgumentException("Something went wrong. Try again later.");
            }
        } else {
            throw new IllegalArgumentException("This address is not present.");
        }
    }

    @Override
    public Address deleteAddress(String addressId, String username) {
        User user = userRepository.findByUsername(username);
        List<Address> addresses = user.getAddresses();

        int toBeDeletedIndex = getToBeDeletedIndex(addressId, addresses);

        List<Route> futureRoutes = routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(user, LocalDateTime.now());
        futureRoutes.addAll(routeRepository.findAllFutureRoutesByUserIdAsPassengerAndIsNotCanceled(user, LocalDateTime.now()));

        Address deletedAddress = addresses.get(toBeDeletedIndex);
        checkIfAddressAssignedToFutureRoute(deletedAddress, futureRoutes);

        addresses.get(toBeDeletedIndex).setDeleted(true);
        user.setAddresses(addresses);
        userRepository.save(user);

        deletedAddress.setDeleted(true);
        return addressRepository.save(deletedAddress);
    }

    private int getToBeDeletedIndex(String addressId, List<Address> addresses) {
        int toBeDeletedIndex = -1;
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).getId().equals(addressId)) {
                toBeDeletedIndex = i;
            }
        }

        if (toBeDeletedIndex == -1) {
            throw new IllegalArgumentException("No such address in your profile");
        }

        return toBeDeletedIndex;
    }

    private void checkIfAddressAssignedToFutureRoute(Address deletedAddress, List<Route> futureRoutes) {
        if (futureRoutes.size() > 0) {
            for (Route futureRoute : futureRoutes) {
                if (futureRoute.getRouteStops().stream().filter(rs -> {
                    if (rs.getAddress().getId().equals(deletedAddress.getId())) {
                        return true;
                    }
                    return false;
                }).count() > 0) {
                    throw new IllegalArgumentException("The address is assigned to future route." +
                            " Change the address for the route and then delete this address");
                }
            }
        }
    }

    @Override
    public User becomeDriver(String username) {
        User currentUser = userRepository.findByUsername(username);
        currentUser.setDriver(true);
        return userRepository.save(currentUser);
    }

    @Override
    public User getCompany(String username) {
        User user = userRepository.findByUsername(username);
        return userRepository.findById(user.getCompanyId()).orElse(null);
    }

    @Override
    public Boolean changePassword(PasswordChange passwordChange, String username) {
        User user = userRepository.findByUsername(username);
        validateChangePassword(passwordChange, user);

        user.setPassword(passwordEncoder.encode(passwordChange.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    private void validateChangePassword(PasswordChange passwordChange, User user) {
        if (!passwordEncoder.matches(passwordChange.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is not correct!");
        }

        if (passwordEncoder.matches(passwordChange.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Your new password cannot be the same as your old password! Choose another password!");
        }
    }

    @Override
    public RouteStop approveOrDeclineRoute(String routeStopId, String driverUsername, boolean approved) {
        RouteStop routeStop = routeStopRepository.getOne(routeStopId);
        if (!approved) {
            routeStopRepository.delete(routeStop);
            emailService.sendEmailResponseForSavedSeat(routeStop.getUser().getEmail(), driverUsername, false);
            return null;
        } else {
            routeStop.setApproved(true);
            RouteStop saved = routeStopRepository.save(routeStop);
            emailService.sendEmailResponseForSavedSeat(routeStop.getUser().getEmail(), driverUsername, true);
            return saved;
        }
    }

    @Override
    public RouteStop getRouteStopById(String routeStopId) {
        return routeStopRepository.findById(routeStopId).orElse(null);
    }

    @Override
    public User getById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

   @Override
    public Rating rateUser(String userId, Integer rating, String passengerUsername) {
        User passenger = userRepository.findByUsername(passengerUsername);

        List<String> driverRouteIds = routeStopRepository.findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("DRIVER", userId);
        List<String> passengerRouteIds = routeStopRepository.findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("PASSENGER", passenger.getId());

        validateRateUser(driverRouteIds, passengerRouteIds);

        Rating newRating = new Rating();
        newRating.setRatingId(new RatingId(userId, passenger.getId()));
        newRating.setDateRating(LocalDateTime.now());
        newRating.setRate(rating);

        return ratingRepository.save(newRating);
    }

    private void validateRateUser(List<String> driverRouteIds, List<String> passengerRouteIds) {
        boolean isNoCommonElements = Collections.disjoint(driverRouteIds, passengerRouteIds);

        if (isNoCommonElements) {
            throw new IllegalArgumentException("The driver never drove this passenger!");
        }
    }

    @Override
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
        dto.setPictureUrl(user.getPictureUrl());
        dto.setBlocked(user.getBlocked());

        return dto;
    }

    @Override
    public RouteStop deleteRouteStopById(String routeStopId, String name) {
        RouteStop rsToDelete = routeStopRepository.findById(routeStopId).orElse(null);

        if (rsToDelete != null) {
            validateDeleteRouteStopById(name, rsToDelete);

            routeStopRepository.delete(rsToDelete);
            emailService.sendEmailForDeletedRouteStop(rsToDelete.getUser().getEmail());
            return rsToDelete;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        }
    }

    private void validateDeleteRouteStopById(String name, RouteStop rsToDelete) {
        if (!rsToDelete.getUser().getUsername().equals(name)) {
            throw new IllegalArgumentException("Could not find this route stop in your profile.");
        }
        Route route = routeRepository.findById(rsToDelete.getRouteId()).orElse(null);
        if (route == null) {
            throw new IllegalArgumentException("Route is not present");
        }
        if (route.getDateRoute().compareTo(LocalDateTime.now()) < 0) {
            throw new IllegalArgumentException("Route already passed. Can not delete route stop");
        }
    }

    @Override
    public Integer countByDriverTrue() {
        return userRepository.findAllByDriverEqualsTrue().size();
    }

    @Override
    public Integer countByDriverFalse() {
        return userRepository.findAllByDriverEqualsFalse().size();
    }

    private User userDtoToUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setEmail(userDto.getEmail());
        user.setDriver(userDto.isDriver());
        user.setCreated(userDto.getCreated());
        user.setCompanyId(userDto.getCompanyId());
        user.setRatings(userDto.getRatings());
        user.setRoles(userDto.getRoles());
        user.setAddresses(userDto.getAddresses());
        user.setCars(userDto.getCars());
        user.setCompany(userDto.getCompany());
        user.setBlocked(userDto.getBlocked());

        return user;
    }

    @Override
    public List<UserDto> searchByUsername(String username) {
        List<User> users = userRepository.findAllByUsernameContains(username);
        return getUserDtos(users);
    }

    @Override
    public List<UserDto> searchNonBlockedByUsername(String username) {
        List<User> users = userRepository.findAllByUsernameContainsAndIsBlockedEquals(username, false);
        return getUserDtos(users);
    }

    private List<UserDto> getUserDtos(List<User> users) {
        List<UserDto> usersDtos = new ArrayList<>();
        for (User u : users) {
            UserDto dto = userToUserDto(u);
            usersDtos.add(dto);
        }
        return usersDtos;
    }

    @Override
    public User blockUser(String userId, String username) {
        User user = userRepository.findByUsername(username);
        validateBlockUser(userId, user);

        User toBeBlocked = userRepository.findById(userId).orElse(null);
        if (toBeBlocked != null) {
            toBeBlocked.setBlocked(true);
            List<Route> routes = routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(toBeBlocked, LocalDateTime.now());
            List<RouteStop> routeStops = routeStopRepository.findAllByPassengerEnumAndUserEquals(PassengerEnum.PASSENGER.toString(), toBeBlocked);

            for (Route route : routes) {
                cancelRoute(route.getId(), toBeBlocked.getUsername());
            }
            for (RouteStop routeStop : routeStops) {
                Route route = routeRepository.findById(routeStop.getRouteId()).orElse(null);
                if (route.getDateRoute().compareTo(LocalDateTime.now()) > 0) {
                    deleteRouteStopById(routeStop.getId(), toBeBlocked.getUsername());
                }
            }
            return userRepository.save(toBeBlocked);
        } else {
            throw new IllegalArgumentException("Something went wrong.");
        }

    }

    private void validateBlockUser(String userId, User user) {
        if (!user.getCompany()) {
            throw new IllegalArgumentException("You have no rights to block the user");
        } else if (user.getId().equals(userId)) {
            throw new IllegalArgumentException("You can not block yourself");
        }
    }

    @Override
    public User unblockUser(String userId, String username) {
        User user = userRepository.findByUsername(username);
        validateUnblockUser(userId, user);

        User toBeBlocked = userRepository.findById(userId).orElse(null);
        if (toBeBlocked != null) {
            toBeBlocked.setBlocked(false);
            return userRepository.save(toBeBlocked);
        } else {
            throw new IllegalArgumentException("Something went wrong.");
        }
    }

    private void validateUnblockUser(String userId, User user) {
        if (!user.getCompany()) {
            throw new IllegalArgumentException("You have no rights to unblock the user");
        } else if (user.getId().equals(userId)) {
            throw new IllegalArgumentException("You can not unblock yourself");
        }
    }

    private Route cancelRoute(String routeId, String driverName) {
        Route routeToCancel = routeRepository.findById(routeId).orElse(null);
        User driver = userRepository.findByUsername(driverName);

        if (routeToCancel != null) {
            validateCancelRoute(routeToCancel, driver);

            routeToCancel.setCanceled(true);
            Route savedRoute = routeRepository.save(routeToCancel);

            emailService.sendEmailsForCanceledRoute(routeId, driverName);
            return savedRoute;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later");
        }
    }

    private void validateCancelRoute(Route routeToCancel, User driver) {
        if (!routeToCancel.getCar().getUserId().equals(driver.getId())) {
            throw new IllegalArgumentException("This route is not present in your profile");
        }
    }

}
