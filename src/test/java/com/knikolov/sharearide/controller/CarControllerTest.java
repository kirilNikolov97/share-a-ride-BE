package com.knikolov.sharearide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.service.impl.CarServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarServiceImpl carService;

    @Mock
    private Principal principal;

    private Car car = new Car("carId", "userId", "manufacturer", "model", 4, 1999,
            "color", false);
    private Car deletedCar = new Car("carId", "userId", "manufacturer", "model", 4, 1999,
            "color", true);
    private CarDto carDto = new CarDto("carId", "userId", "manufacturer", "model", 4, 1999, "color");

    @WithMockUser("user")
    @Test
    void whenGetAllCarsByUser_thenReturns200() throws Exception {
        // given
        when(carService.getAllCarsByUser(any())).thenReturn(new ArrayList<Car>() {{ add(car); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/cars")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Car>() {{ add(car); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetCarById_thenReturns200() throws Exception {
        // given
        when(carService.getCarById("carId")).thenReturn(car);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/car/carId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(car)).isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenDeleteCarById_thenReturns200() throws Exception {
        // given
        when(carService.deleteCar("carId", "user")).thenReturn(deletedCar);

        // when
        MvcResult mvcResult = mockMvc
                .perform(delete("/car?carId=carId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(deletedCar)).isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenAddNewCar_thenReturns200() throws Exception {
        // given
        when(carService.addNewCar(any(), any())).thenReturn(car);

        // when
        MvcResult mvcResult = mockMvc
                .perform(post("/car").content(objectMapper.writeValueAsString(carDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(car)).isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenUpdateCar_thenReturns200() throws Exception {
        // given
        when(carService.updateCar(any(), any())).thenReturn(car);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/car").content(objectMapper.writeValueAsString(carDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(car)).isEqualToIgnoringWhitespace(actual);
    }

}