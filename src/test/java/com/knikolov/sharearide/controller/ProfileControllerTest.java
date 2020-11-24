package com.knikolov.sharearide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.PasswordChange;
import com.knikolov.sharearide.dto.UserDto;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    private User user = new User("userId", "username", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private UserDto userDto = new UserDto("userDtoId", "username", "first", "last", "09990090", "dto@gmail.com", true);
    private User anotherUser = new User("anotherUserId", "username", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private User company = new User("companyId", "company", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private UserDto companyDto = new UserDto("companyId", "company", "first",
            "last", "123321", "user@user.mail", false);
    private Address address = new Address("addressId", "district", "street", "");
    private AddressDto addressDto = new AddressDto("addressId", "district", "street", "", 100d, 100d);
    private PasswordChange passwordChange = new PasswordChange("123", "321");
    private RouteStop routeStop = new RouteStop("routeStopId", "routeId", address, user, PassengerEnum.DRIVER.toString(), false);
    private RatingId ratingId = new RatingId("userId", "anotherUserId");
    private Rating rating = new Rating(ratingId, 3, LocalDateTime.now());

    @WithMockUser("user")
    @Test
    void whenGetUser_thenReturns200() throws Exception {
        // given
        when(userService.getByUsername(any())).thenReturn(user);
        when(userService.userToUserDto(any())).thenReturn(userDto);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(userDto))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenUpdateUser_thenThrowUnauthorizedRequest() throws Exception {
        // given
        when(userService.update(any())).thenReturn(user);
        when(userService.userToUserDto(any())).thenReturn(userDto);

        // when
        Exception exception = assertThrows(Exception.class,
                () -> mockMvc.perform(patch("/user").content(objectMapper.writeValueAsString(userDto)).contentType(MediaType.APPLICATION_JSON)));

        assertEquals("Unauthorized request.", exception.getCause().getMessage());
    }

    @WithMockUser("username")
    @Test
    void whenUpdateUser_thenReturns200() throws Exception {
        // given
        when(userService.update(any())).thenReturn(user);
        when(userService.userToUserDto(any())).thenReturn(userDto);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/user").content(objectMapper.writeValueAsString(userDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(userDto))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetAddress_thenReturns200() throws Exception {
        // given
        when(userService.getAddressesByUsername(any())).thenReturn(new ArrayList<Address>() {{ add(address); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/address").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Address>() {{ add(address); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetCompanyAddress_thenReturns200() throws Exception {
        // given
        when(userService.getCompany(any())).thenReturn(company);
        when(userService.getAddressesByUsername(any())).thenReturn(new ArrayList<Address>() {{ add(address); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/companyAddresses").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Address>() {{ add(address); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetAddressById_thenReturns200() throws Exception {
        // given
        when(userService.getAddressById(any(), any())).thenReturn(address);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/address/addressId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(address))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenUpdateAddress_thenReturns200() throws Exception {
        // given
        when(userService.updateAddress(any(), any())).thenReturn(address);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/address").content(objectMapper.writeValueAsString(addressDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(address))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenDeleteAddress_thenReturns200() throws Exception {
        // given
        when(userService.deleteAddress(any(), any())).thenReturn(address);

        // when
        MvcResult mvcResult = mockMvc
                .perform(delete("/address?addressId=addressId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(address))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenBecomeDriver_thenReturns200() throws Exception {
        // given
        when(userService.becomeDriver(any())).thenReturn(user);
        when(userService.userToUserDto(any())).thenReturn(userDto);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/becomeDriver").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(userDto))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetCompany_thenReturns200() throws Exception {
        // given
        when(userService.getCompany(any())).thenReturn(company);
        when(userService.userToUserDto(any())).thenReturn(companyDto);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/company").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(companyDto))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenChangePassword_thenReturns200() throws Exception {
        // given
        when(userService.changePassword(any(), any())).thenReturn(true);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/changePassword").content(objectMapper.writeValueAsString(passwordChange)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertTrue(actual.contains("true"));
    }

    @WithMockUser("user")
    @Test
    void whenApproveRoute_thenReturns200() throws Exception {
        // given
        when(userService.approveOrDeclineRoute("routeStopId", "user", true)).thenReturn(routeStop);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/approveOrDeclineRouteStop?=routeStopId=routeStopId&approved=true").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(routeStop))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetRouteStopById_thenReturns200() throws Exception {
        // given
        when(userService.getRouteStopById("routeStopId")).thenReturn(routeStop);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/routeStop/routeStopId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(routeStop))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenDeleteRouteStopById_thenReturns200() throws Exception {
        // given
        when(userService.deleteRouteStopById("routeStopId", "user")).thenReturn(routeStop);

        // when
        MvcResult mvcResult = mockMvc
                .perform(delete("/routeStop/routeStopId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(routeStop))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetUserById_thenReturns200() throws Exception {
        // given
        when(userService.getById("userId")).thenReturn(user);
        when(userService.userToUserDto(any())).thenReturn(userDto);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/user/userId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(userDto))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenRateUser_thenReturns200() throws Exception {
        // given
        when(userService.getByUsername("user")).thenReturn(user);
        when(userService.rateUser(any(), any(), any())).thenReturn(rating);

        // when
        MvcResult mvcResult = mockMvc
                .perform(post("/rate?userId=anotherUserId&rating=3").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(rating))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenSearchUsers_thenReturns200() throws Exception {
        // given
        when(userService.searchByUsername("user")).thenReturn(new ArrayList<UserDto>() {{ add(userDto); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/searchUser?username=user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<UserDto>() {{ add(userDto); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenSearchNotBlockedUsers_thenReturns200() throws Exception {
        // given
        when(userService.searchNonBlockedByUsername("user")).thenReturn(new ArrayList<UserDto>() {{ add(userDto); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/searchNotBlockedUser?username=user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<UserDto>() {{ add(userDto); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenBlockUser_thenReturns200() throws Exception {
        // given
        when(userService.blockUser(any(), any())).thenReturn(user);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/blockUser?userId=userId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(user))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenUnblockUser_thenReturns200() throws Exception {
        // given
        when(userService.unblockUser(any(), any())).thenReturn(user);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/unblockUser?userId=userId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(user))
                .isEqualToIgnoringWhitespace(actual);
    }

}