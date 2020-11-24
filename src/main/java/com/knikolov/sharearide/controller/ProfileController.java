package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.dto.UserDto;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * Controller for profile page
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public UserDto getUser(Principal principal) {
        User user = userService.getByUsername(principal.getName());
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "/user", method = RequestMethod.PATCH)
    public UserDto updateUser(@RequestBody UserDto userDto, Principal principal) {
        validateUserDto(userDto);
        if (userDto.getUsername().equals(principal.getName())) {
            User userDb = userService.update(userDto);
            return userService.userToUserDto(userDb);
        } else {
            throw new IllegalArgumentException("Unauthorized request.");
        }
    }

    @RequestMapping(value = "/uploadPicture", method = RequestMethod.POST)
    public String uploadPicture(@RequestParam("file") MultipartFile file, Principal principal) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(file.getBytes());
            if (ImageIO.read(byteArrayInputStream) == null) {
                throw new IllegalArgumentException("This file is not an image");
            }
            return userService.uploadPicture(file, principal.getName());
        } catch (IOException e) {
            throw new IllegalArgumentException("Something went wrong.");
        }
    }

    @RequestMapping(value = "/address", method = RequestMethod.GET)
    public List<Address> getAddress(Principal principal) {
        return userService.getAddressesByUsername(principal.getName());
    }

    @RequestMapping(value = "/companyAddresses", method = RequestMethod.GET)
    public List<Address> getCompanyAddresses(Principal principal) {
        User company = userService.getCompany(principal.getName());
        return userService.getAddressesByUsername(company.getUsername());
    }

    @RequestMapping(value = "/address/{addressId}", method = RequestMethod.GET)
    public Address getAddressById(@PathVariable String addressId, Principal principal) {
        return userService.getAddressById(addressId, principal.getName());
    }

    @RequestMapping(value = "/address", method = RequestMethod.POST)
    public Address saveAddress(@RequestBody AddressDto address, Principal principal) {
        validateAddress(address);
        return userService.createAddress(address, principal.getName());
    }

    @RequestMapping(value = "/address", method = RequestMethod.PATCH)
    public Address updateAddress(@RequestBody AddressDto address, Principal principal) {
        validateAddress(address);
        return userService.updateAddress(address, principal.getName());
    }

    @RequestMapping(value = "/address", method = RequestMethod.DELETE)
    public Address deleteAddress(@RequestParam("addressId") String addressId, Principal principal) {
        return userService.deleteAddress(addressId, principal.getName());
    }

    @RequestMapping(value = "/becomeDriver", method = RequestMethod.GET)
    public UserDto becomeDriver(Principal principal) {
        User user = userService.becomeDriver(principal.getName());
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "/company", method = RequestMethod.GET)
    public UserDto getCompany(Principal principal) {
        User user = userService.getCompany(principal.getName());
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.PATCH)
    public Boolean changePassword(@RequestBody PasswordChange passwordChange, Principal principal) {
        validatePasswordChange(passwordChange);
        return userService.changePassword(passwordChange, principal.getName());
    }

    @RequestMapping(value = "/approveOrDeclineRouteStop", method = RequestMethod.PATCH)
    public RouteStop approveRoute(@RequestParam("routeStopId") String routeStopId, @RequestParam("approved") boolean approved,
                           Principal principal) {
        return userService.approveOrDeclineRoute(routeStopId, principal.getName(), approved);
    }

    @RequestMapping(value = "/routeStop/{routeStopId}", method = RequestMethod.GET)
    public RouteStop getRouteStopById(@PathVariable String routeStopId, Principal principal) {
        return userService.getRouteStopById(routeStopId);
    }

    @RequestMapping(value = "/routeStop/{routeStopId}", method = RequestMethod.DELETE)
    public RouteStop deleteRouteStopById(@PathVariable String routeStopId, Principal principal) {
        return userService.deleteRouteStopById(routeStopId, principal.getName());
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public UserDto getUserById(@PathVariable String userId) {
        User user = userService.getById(userId);
        return userService.userToUserDto(user);
    }

    @RequestMapping(value = "/rate", method = RequestMethod.POST)
    public Rating rateUser(@RequestParam("userId") String userId, @RequestParam("rating") Integer rating, Principal principal) {
        User loggedUser = userService.getByUsername(principal.getName());
        if (loggedUser.getId().equals(userId)) {
            throw new IllegalArgumentException("You can not rate yourself.");
        }

        return userService.rateUser(userId, rating, principal.getName());
    }

    @RequestMapping(value = "/searchUser", method = RequestMethod.GET)
    public List<UserDto> searchUsers(@RequestParam String username) {
        return userService.searchByUsername(username);
    }

    @RequestMapping(value = "/searchNotBlockedUser", method = RequestMethod.GET)
    public List<UserDto> searchNotBlockedUsers(@RequestParam String username) {
        return userService.searchNonBlockedByUsername(username);
    }

    @RequestMapping(value = "/blockUser", method = RequestMethod.PATCH)
    public User blockUser(@RequestParam String userId, Principal principal) {
        return userService.blockUser(userId, principal.getName());
    }

    @RequestMapping(value = "/unblockUser", method = RequestMethod.PATCH)
    public User unblockUser(@RequestParam String userId, Principal principal) {
        return userService.unblockUser(userId, principal.getName());
    }

    private void validateUserDto(UserDto user) {
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
        } else if (address.getDistrict() == null || "".equals(address.getDistrict().trim())) {
            throw new IllegalArgumentException("District field is not filled correct!");
        } else if (address.getStreet() == null || "".equals(address.getStreet().trim())) {
            throw new IllegalArgumentException("Street field is not filled correct!");
        }
    }
}
