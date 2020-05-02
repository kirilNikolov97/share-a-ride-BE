package com.knikolov.sharearide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knikolov.sharearide.dto.AddressDto;
import com.knikolov.sharearide.dto.UserDto;
import com.knikolov.sharearide.models.Address;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.models.City;
import com.knikolov.sharearide.models.User;
import com.knikolov.sharearide.service.impl.CarServiceImpl;
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
    private Address address = new Address("addressId",new City("cityName"), "district", "street", "");
    private User company = new User("companyId", "company", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private AddressDto addressDto = new AddressDto("addressId", new City("cityName"), "district", "street", "", 100d, 100d);

    @WithMockUser("user")
    @Test
    void whenGetUser_thenReturns200() throws Exception {
        // given
        when(userService.getUserByUsername(any())).thenReturn(user);
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
        when(userService.updateUser(any())).thenReturn(user);
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
        when(userService.updateUser(any())).thenReturn(user);
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


}