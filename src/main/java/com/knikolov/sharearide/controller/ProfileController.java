package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
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
    User getUser(Principal principal) {
        return userService.getUserByUsername(principal.getName());
    }

    @RequestMapping(value = "/user", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    User updateUser(@RequestBody User user, Principal principal) {
        validateUser(user);
        return userService.updateUser(user);
    }

    @RequestMapping(value = "/address", method = RequestMethod.GET)
    List<Address> getAddress(Principal principal) {
        return userService.getAddressesByUsername(principal.getName());
    }

    @RequestMapping(value = "/address/{addressId}", method = RequestMethod.GET)
    Address getAddressById(@PathVariable String addressId) {
        return userService.getAddressById(addressId);
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

    //TODO: Replace GET with PATCH
    @RequestMapping(value = "/becomeDriver", method = RequestMethod.GET)
    User becomeDriver(Principal principal) {
        return this.userService.becomeDriver(principal.getName());
    }

    @RequestMapping(value = "/cities", method = RequestMethod.GET)
    List<City> getAllCities() {
        return this.userService.getAllCities();
    }

    @RequestMapping(value = "/company", method = RequestMethod.GET)
    User getCompany(Principal principal) {
        return this.userService.getCompany(principal.getName());
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.PATCH)
    Boolean changePassword(@RequestBody PasswordChange passwordChange, Principal principal) {
        validatePasswordChange(passwordChange);
        return this.userService.changePassword(passwordChange, principal.getName());
    }

    @RequestMapping(value = "/approveRouteStop", method = RequestMethod.PATCH)
    RouteStop approveRoute(@RequestParam("routeStopId") String routeStopId, Principal principal) {
        return this.userService.approveRoute(routeStopId, principal.getName());
    }

    @RequestMapping(value = "/routeStop/{routeStopId}", method = RequestMethod.GET)
    RouteStop getRouteStopById(@PathVariable String routeStopId, Principal principal) {
        return this.userService.getRouteStopById(routeStopId);
    }

    @RequestMapping(value = "user/{userId}", method = RequestMethod.GET)
    User getUserById(@PathVariable String userId) {
        //TODO
        User u = this.userService.getUserById(userId);
        return u;
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
