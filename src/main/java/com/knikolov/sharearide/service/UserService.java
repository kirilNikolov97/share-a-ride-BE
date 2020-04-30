package com.knikolov.sharearide.service;

import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.dto.UserDto;
import com.knikolov.sharearide.models.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User getUserByUsername(String username);

    User updateUser(UserDto userDto);

    String uploadPicture(MultipartFile file, String username);

    List<Address> getAddressesByUsername(String username);

    Address getAddressById(String addressId, String username);

    Address addNewAddress(AddressDto addressDto, String username);

    Address updateAddress(AddressDto address, String username);

    Address deleteAddress(String addressId, String username);

    User becomeDriver(String name);

    List<City> getAllCities();

    User getCompany(String username);

    Boolean changePassword(PasswordChange passwordChange, String username);

    RouteStop approveOrDeclineRoute(String routeStopId, String driverUsername, boolean approved);

    RouteStop getRouteStopById(String routeStopId);

    User getUserById(String userId);

    Rating rateUser(String userId, Integer rating, String passengerUsername);

    UserDto userToUserDto(User user);

    RouteStop deleteRouteStopById(String routeStopId, String name);

    Integer countByDriverTrue();

    Integer countByDriverFalse();

    List<UserDto> searchByUsername(String username);
}
