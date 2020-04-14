package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.dto.UserDto;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    UserDto getUser(Principal principal) {
        User user =  userService.getUserByUsername(principal.getName());
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "/user", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    UserDto updateUser(@RequestBody User user, Principal principal) {
        validateUser(user);
        if (user.getUsername().equals(principal.getName())) {
            User userDb = userService.updateUser(user);
            return userService.userToUserDto(userDb);
        } else {
            throw new IllegalArgumentException("Unauthorized request.");
        }
    }

    @RequestMapping(value = "/address", method = RequestMethod.GET)
    List<Address> getAddress(Principal principal) {
        return userService.getAddressesByUsername(principal.getName());
    }

    @RequestMapping(value = "/address/{addressId}", method = RequestMethod.GET)
    Address getAddressById(@PathVariable String addressId, Principal principal) {
        return userService.getAddressById(addressId, principal.getName());
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    Address saveAddress(@RequestBody AddressDto address, Principal principal) {
        validateAddress(address);

        return userService.addNewAddress(address, principal.getName());
    }

    @RequestMapping(value = "/address", method = RequestMethod.PATCH)
    Address updateAddress(@RequestBody AddressDto address, Principal principal) {
        validateAddress(address);

        return userService.updateAddress(address, principal.getName());
    }

    @RequestMapping(value = "/address", method = RequestMethod.DELETE)
    Address deleteAddress(@RequestParam("addressId") String addressId, Principal principal) {
        return userService.deleteAddress(addressId, principal.getName());
    }

    @RequestMapping(value = "/becomeDriver", method = RequestMethod.GET)
    UserDto becomeDriver(Principal principal) {
        User user = this.userService.becomeDriver(principal.getName());
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "/cities", method = RequestMethod.GET)
    List<City> getAllCities() {
        return this.userService.getAllCities();
    }

    @RequestMapping(value = "/company", method = RequestMethod.GET)
    UserDto getCompany(Principal principal) {
        User user = this.userService.getCompany(principal.getName());
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.PATCH)
    Boolean changePassword(@RequestBody PasswordChange passwordChange, Principal principal) {
        validatePasswordChange(passwordChange);
        return this.userService.changePassword(passwordChange, principal.getName());
    }

    @RequestMapping(value = "/approveOrDeclineRouteStop", method = RequestMethod.PATCH)
    RouteStop approveRoute(@RequestParam("routeStopId") String routeStopId, @RequestParam("approved") boolean approved,
                           Principal principal) {
        return this.userService.approveOrDeclineRoute(routeStopId, principal.getName(), approved);
    }

    @RequestMapping(value = "/routeStop/{routeStopId}", method = RequestMethod.GET)
    RouteStop getRouteStopById(@PathVariable String routeStopId, Principal principal) {
        return this.userService.getRouteStopById(routeStopId);
    }

    @RequestMapping(value = "user/{userId}", method = RequestMethod.GET)
    UserDto getUserById(@PathVariable String userId) {
        User user = this.userService.getUserById(userId);
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "rate", method = RequestMethod.POST)
    Rating rateUser(@RequestParam("userId") String userId, @RequestParam("rating") Integer rating, Principal principal) {
        User loggedUser = this.userService.getUserByUsername(principal.getName());
        if (loggedUser.getId().equals(userId)) {
            throw new IllegalArgumentException("You can not rate yourself.");
        }

        return this.userService.rateUser(userId, rating, principal.getName());
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        } else if (user.getFirstName() == null || "".equals(user.getFirstName().trim())) {
            throw new IllegalArgumentException("First name is not filled correct!");
        } else if (user.getLastName() == null || "".equals(user.getLastName().trim())) {
            throw new IllegalArgumentException("Last name is not filled correct!");
        } else if (user.getPhone() == null || "".equals(user.getPhone().trim())) {
            throw new IllegalArgumentException("Phone is not filled correct!");
        } else if (user.getEmail() == null || "".equals(user.getEmail().trim())) {
            throw new IllegalArgumentException("Email is not filled correct!");
        }
    }

    private void validatePasswordChange(PasswordChange passwordChange) {
        if (passwordChange == null) {
            throw new IllegalArgumentException("Something went wrong.");
        } else if (passwordChange.getOldPassword() == null || "".equals(passwordChange.getOldPassword().trim())) {
            throw new IllegalArgumentException("Old password field is empty or not filled correct!");
        } else if (passwordChange.getNewPassword() == null || "".equals(passwordChange.getNewPassword().trim())) {
            throw new IllegalArgumentException("New password field is empty or not filled correct!");
        }
    }

    private void validateAddress(AddressDto address) {
        if (address == null) {
            throw new IllegalArgumentException("Something went wrong. Try again later,");
        } else if (address.getCity() == null || address.getCity().getName() == null || "".equals(address.getCity().getName().trim())) {
            throw new IllegalArgumentException("City field is not filled correct!");
        } else if (address.getDistrict() == null || "".equals(address.getDistrict().trim())) {
            throw new IllegalArgumentException("District field is not filled correct!");
        } else if (address.getStreet() == null || "".equals(address.getStreet().trim())) {
            throw new IllegalArgumentException("Street field is not filled correct!");
        }
    }
}
