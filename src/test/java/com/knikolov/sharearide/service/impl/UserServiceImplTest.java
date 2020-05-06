package com.knikolov.sharearide.service.impl;

import com.cloudinary.Cloudinary;
import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import com.knikolov.sharearide.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private Cloudinary cloudinary;

    private User user = new User("userId", "username", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private User anotherUser = new User("anotherUserId", "username", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private Address address = new Address("addressId", new City("cityName"), "district", "street", "");
    private Address anotherAddress = new Address("anotherAddressId", new City("cityName"), "district", "street", "");
    private Address deletedAddress = new Address("deletedAddressId", new City("cityName"), "district", "street", "");
    private Car car = new Car("carId", "userId", "manufacturer", "model", 4, 1999,
            "color", false);
    private Route futureRouteDriver = new Route("anotherRouteId", LocalDateTime.now().plusHours(1) , false, true, "officeAddressId", car);
    private Route futureRoutePassenger = new Route("anotherRouteAsPassengerId", LocalDateTime.now().plusHours(1) , false, true, "officeAddressId", car);
    private RouteStop routeStop = new RouteStop("routeStopId", "routeId", address, user, PassengerEnum.DRIVER.toString(), false);
    private Route route = new Route("routeId", LocalDateTime.now(), false, true, "officeAddressId", car);

    @Test
    void whenUpdateUser_thenSuccess() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);

        // when
        User returned = userService.updateUser(userService.userToUserDto(user));

        // then
        assertEquals("userId", returned.getId());
    }

//    @Test
//    void whenUploadPicture_thenSuccess() throws IOException {
//        // given
//        when(userRepository.findByUsername("username")).thenReturn(user);
//        when(cloudinary.uploader().upload(any(), any())).thenReturn(new HashMap() {{ put("url", "url"); }});
//
//        // when
//        String returned = userService.uploadPicture(any(), "username");
//
//        // then
//        assertEquals("Success", returned);
//    }

    @Test
    void whenGetAddressesByUsername_thenReturnSuccess() {
        // given
        address.setDeleted(false);
        deletedAddress.setDeleted(true);
        user.setAddresses(new ArrayList<Address>() {{ add(address); add(deletedAddress); }});

        when(userRepository.findByUsername(any())).thenReturn(user);

        // when
        List<Address> returned = userService.getAddressesByUsername("username");

        // then
        assertEquals(1, returned.size());
        assertEquals("addressId", returned.get(0).getId());
    }

    @Test
    void whenAddNewAddress_thenSuccess() {
        // given
        AddressDto addressDto = new AddressDto("addressDtoId", new City("cityName"), "district", "street", "additionalInfo", 1000d, 1000d);
        user.setAddresses(new ArrayList<>());

        when(userRepository.findByUsername(any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(any());

        // when
        Address returned = userService.addNewAddress(addressDto, "username");

        // then
        assertEquals(addressDto.getDistrict(), returned.getDistrict());
        assertEquals(addressDto.getStreet(), returned.getStreet());
        assertEquals(addressDto.getAdditionalInfo(), returned.getAdditionalInfo());
    }

    @Test
    void whenUpdateAddress_thenSuccess() {
        // given
        AddressDto addressDto = new AddressDto("addressId", new City("cityName"), "district", "street", "additionalInfo", 1000d, 1000d);
        user.setAddresses(new ArrayList<Address>() {{ add(address); }});

        when(addressRepository.findById(any())).thenReturn(Optional.of(address));
        when(userRepository.findByUsername(any())).thenReturn(user);
        when(addressRepository.save(any())).thenReturn(address);

        // when
        Address returned = userService.updateAddress(addressDto, "username");

        // then
        assertEquals("addressId", returned.getId());
    }

    @Test
    void whenUpdateAddress_thenThrowNotFoundInProfile() {
        // given
        AddressDto addressDto = new AddressDto("addressDtoId", new City("cityName"), "district", "street", "additionalInfo", 1000d, 1000d);
        user.setAddresses(new ArrayList<Address>() {{ add(address); }});

        when(addressRepository.findById(any())).thenReturn(Optional.of(address));
        when(userRepository.findByUsername(any())).thenReturn(user);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateAddress(addressDto, "username"));

        // then
        assertEquals("This address can not be found in your profile.", exception.getMessage());
    }

    @Test
    void whenUpdateAddress_thenThrowAddressNotPresent() {
        // given
        AddressDto addressDto = new AddressDto("addressId", new City("cityName"), "district", "street", "additionalInfo", 1000d, 1000d);
        user.setAddresses(new ArrayList<Address>() {{ add(address); }});

        when(addressRepository.findById(any())).thenReturn(Optional.ofNullable(null));
        when(userRepository.findByUsername(any())).thenReturn(user);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateAddress(addressDto, "username"));

        // then
        assertEquals("This address is not present.", exception.getMessage());
    }

    @Test
    void whenDeleteAddress_thenSuccess() {
        // given
        user.setAddresses(new ArrayList<Address>() {{ add(address); }});
        deletedAddress.setDeleted(true);

        when(userRepository.findByUsername(any())).thenReturn(user);
        when(routeRepository.findAllFutureRoutesByUserIdAsDriver(any(), any())).thenReturn(new ArrayList<>());
        when(routeRepository.findAllFutureRoutesByUserIdAsPassenger(any(), any())).thenReturn(new ArrayList<>());
        when(userRepository.save(any())).thenReturn(user);
        when(addressRepository.save(any())).thenReturn(deletedAddress);

        // when
        Address returned = userService.deleteAddress("addressId", "username");

        // then
        assertTrue(returned.getDeleted());
    }

    @Test
    void whenDeleteAddress_thenThrowNotFoundInProfile() {
        // given
        user.setAddresses(new ArrayList<>());

        when(userRepository.findByUsername(any())).thenReturn(user);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteAddress("addressId", "username"));

        // then
        assertEquals("No such address in your profile", exception.getMessage());
    }

    @Test
    void whenDeleteAddress_thenThrowAssignedToFutureRoute() {
        // given
        user.setAddresses(new ArrayList<Address>() {{ add(address); }});
        futureRouteDriver.setRouteStops(new ArrayList<RouteStop>() {{ add(routeStop); }});

        when(userRepository.findByUsername(any())).thenReturn(user);
        when(routeRepository.findAllFutureRoutesByUserIdAsDriver(any(), any())).thenReturn(new ArrayList<Route>() {{ add(futureRouteDriver); }});
        when(routeRepository.findAllFutureRoutesByUserIdAsPassenger(any(), any())).thenReturn(new ArrayList<Route>() {{ add(futureRoutePassenger); }});

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteAddress("addressId", "username"));

        // then
        assertEquals("The address is assigned to future route. Change the address for the route and then delete this address", exception.getMessage());
    }

    @Test
    void whenChangePassword_thenSuccess() {
        // given
        user.setPassword("old");

        when(userRepository.findByUsername(any())).thenReturn(user);
        doReturn(true).when(passwordEncoder).matches("old", "old");
        doReturn(false).when(passwordEncoder).matches("new", "old");
        when(userRepository.save(any())).thenReturn(user);

        // when
        Boolean returned = userService.changePassword(new PasswordChange("old", "new"), "username");

        // then
        assertTrue(returned);
    }

    @Test
    void whenChangePassword_thenThrowOldPasswordNotCorrect() {
        // given
        user.setPassword("old");

        when(userRepository.findByUsername(any())).thenReturn(user);
        doReturn(false).when(passwordEncoder).matches("old", "old");

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(new PasswordChange("old", "new"), "username"));

        // then
        assertEquals("Old password is not correct!", exception.getMessage());
    }

    @Test
    void whenChangePassword_thenThrowNewPasswordDifferentFromOld() {
        // given
        user.setPassword("old");

        when(userRepository.findByUsername(any())).thenReturn(user);
        doReturn(true).when(passwordEncoder).matches("old", "old");

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(new PasswordChange("old", "old"), "username"));

        // then
        assertEquals("Your new password cannot be the same as your old password! Choose another password!", exception.getMessage());
    }

    @Test
    void whenApproveOrDeclineRouteApprovedEqualsFalse_thenSuccess() {
        // given
        when(routeStopRepository.getOne("routeStopId")).thenReturn(routeStop);
//        when(routeStopRepository.save(any())).thenReturn(routeStop);
        doNothing().when(emailService).sendEmailResponseForSavedSeat(any(), any(), eq(false));

        // when
        RouteStop returned = userService.approveOrDeclineRoute("routeStopId", "username", false);

        // then
        assertNull(returned);
    }

    @Test
    void whenApproveOrDeclineRouteApprovedEqualsTrue_thenSuccess() {
        // given
        when(routeStopRepository.getOne("routeStopId")).thenReturn(routeStop);
        when(routeStopRepository.save(any())).thenReturn(routeStop);
        doNothing().when(emailService).sendEmailResponseForSavedSeat(any(), any(), eq(true));

        // when
        RouteStop returned = userService.approveOrDeclineRoute("routeStopId", "username", true);

        // then
        assertEquals("routeStopId", returned.getId());
    }

    @Test
    void whenRateUser_thenSuccess() {
        // given
        Rating rating = new Rating(new RatingId("userId", "anotherUserId"), 4, LocalDateTime.now());
        when(userRepository.findByUsername(any())).thenReturn(anotherUser);
        doReturn(new ArrayList<String>() {{ add("userId"); }} ).when(routeStopRepository).findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("DRIVER", "userId");
        doReturn(new ArrayList<String>() {{ add("userId"); }} ).when(routeStopRepository).findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("PASSENGER", "anotherUserId");
        when(ratingRepository.save(any())).thenReturn(rating);

        // when
        Rating returned = userService.rateUser("userId", 4, "anotherUsername");

        // then
        assertEquals(4, returned.getRate());
    }

    @Test
    void whenRateUser_thenThrowDriverNeverDrovePassenger() {
        // given
        when(userRepository.findByUsername(any())).thenReturn(anotherUser);
        doReturn(new ArrayList<>()).when(routeStopRepository).findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("DRIVER", "userId");
        doReturn(new ArrayList<>()).when(routeStopRepository).findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("PASSENGER", "anotherUserId");

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.rateUser("userId", 4, "anotherUsername"));

        // then
        assertEquals("The driver never drove this passenger!", exception.getMessage());
    }

    @Test
    void whenDeleteRouteStopById_thenSuccess() {
        // given
        when(routeStopRepository.findById(any())).thenReturn(Optional.of(routeStop));
        when(routeRepository.findById(any())).thenReturn(Optional.of(futureRouteDriver));
        doNothing().when(routeStopRepository).delete(any());
        doNothing().when(emailService).sendEmailForDeletedRouteStop(any());

        // when
        RouteStop returned = userService.deleteRouteStopById("routeStopId", "username");

        // then
        assertEquals("routeStopId", returned.getId());
    }

    @Test
    void whenDeleteRouteStopById_thenThrowSomethingWentWrong() {
        // given
        when(routeStopRepository.findById(any())).thenReturn(Optional.ofNullable(null));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteRouteStopById("routeStopId", "name"));

        // then
        assertEquals("Something went wrong. Try again later.", exception.getMessage());
    }

    @Test
    void whenDeleteRouteStopById_thenThrowNotFoundInProfile() {
        // given
        when(routeStopRepository.findById(any())).thenReturn(Optional.of(routeStop));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteRouteStopById("routeStopId", "name"));

        // then
        assertEquals("Could not find this route stop in your profile.", exception.getMessage());
    }

    @Test
    void whenDeleteRouteStopById_thenThrowRouteNotPresent() {
        // given
        when(routeStopRepository.findById(any())).thenReturn(Optional.of(routeStop));
        when(routeRepository.findById(any())).thenReturn(Optional.ofNullable(null));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteRouteStopById("routeStopId", "username"));

        // then
        assertEquals("Route is not present", exception.getMessage());
    }

    @Test
    void whenDeleteRouteStopById_thenThrowRouteAlreadyPassed() {
        // given
        when(routeStopRepository.findById(any())).thenReturn(Optional.of(routeStop));
        when(routeRepository.findById(any())).thenReturn(Optional.of(route));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteRouteStopById("routeStopId", "username"));

        // then
        assertEquals("Route already passed. Can not delete route stop", exception.getMessage());
    }
}