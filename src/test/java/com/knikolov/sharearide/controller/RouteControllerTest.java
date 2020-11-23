package com.knikolov.sharearide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.service.EmailService;
import com.knikolov.sharearide.service.impl.RouteServiceImpl;
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

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteServiceImpl routeService;

    @Mock
    private EmailService emailService;

    private Car car = new Car("carId", "userId", "manufacturer", "model", 4, 1999,
            "color", false);
    private Route futureRouteDriver = new Route("anotherRouteId", LocalDateTime.now().plusHours(1) , false, true, "officeAddressId", car);
    private Route futureRoutePassenger = new Route("anotherRouteAsPassengerId", LocalDateTime.now().plusHours(1) , false, true, "officeAddressId", car);
    private Route route = new Route("routeId", LocalDateTime.now(), false, true, "officeAddressId", car);
    private User user = new User("userId", "username", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private Address address = new Address("addressId", "district", "street", "");
    private RouteStop routeStop = new RouteStop("routeStopId", "routeId", address, user, PassengerEnum.DRIVER.toString(), false);
    private TopUser topUser = new TopUser(user, 3, 4, 5d);

    @WithMockUser("user")
    @Test
    void whenGetRoutesByUsernameAsDriver_thenReturns200() throws Exception {
        // given
        when(routeService.getAllPastNotCanceledRoutesWhereUserIsDriver(any(), any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/routesAsDriver?username=user&sortBy=date_desc&limit=10").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetRoutesByUsernameAsDriverNoUsername_thenReturns200() throws Exception {
        // given
        when(routeService.getAllPastNotCanceledRoutesWhereUserIsDriver(any(), any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/routesAsDriver?username=&sortBy=&limit=10").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetFutureRoutesByUsernameAsDriver_thenReturns200() throws Exception {
        // given
        when(routeService.getAllFutureNotCanceledRoutesWhereUserIsDriver(any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/futureRoutesAsDriver?username=user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetFutureRoutesByUsernameAsDriverNoUsername_thenReturns200() throws Exception {
        // given
        when(routeService.getAllFutureNotCanceledRoutesWhereUserIsDriver(any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/futureRoutesAsDriver?username=").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetFutureRoutesByUsernameAsPassenger_thenReturns200() throws Exception {
        // given
        when(routeService.getAllFutureNotCanceledRoutesWhereUserIsPassenger(any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/futureRoutesAsPassenger").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetRoutesByUsernameAsPassenger_thenReturns200() throws Exception {
        // given
        when(routeService.getAllPastNotCanceledRoutesWhereUserIsPassenger(any(), any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/routesAsPassenger?sortBy=none&limit=10").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetRouteById_thenReturns200() throws Exception {
        // given
        when(routeService.getById(any(), any(), any())).thenReturn(route);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/route/routeId?validate=true").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(route))
                .isEqualToIgnoringWhitespace(actual);
    }


    @WithMockUser("user")
    @Test
    void whenAddNewRoute_thenReturns200() throws Exception {
        // given
        when(routeService.insert(any(), any(), any(), any(), any(), any())).thenReturn(route);
        doNothing().when(emailService).sendTestEmail();
        LocalDateTime date = LocalDateTime.now();

        // when
        MvcResult mvcResult = mockMvc
                .perform(post("/route?carId=carId&addressId=addressId&officeDirection=false&companyAddressId=companyAddressId").content(objectMapper.writeValueAsString(date)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(route))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenCancelRoute_thenReturns200() throws Exception {
        // given
        when(routeService.cancelRoute(any(), any())).thenReturn(route);

        // when
        MvcResult mvcResult = mockMvc
                .perform(patch("/cancelRoute?routeId=routeId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(route))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetLastRoutes_thenReturns200() throws Exception {
        // given
        when(routeService.getLastRoutes(any(), any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/lastRoutes?limit=10").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenSaveSeat_thenReturns200() throws Exception {
        // given
        when(routeService.saveSeat(any(), any(), any())).thenReturn(routeStop);

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/saveSeat/routeId?addressId=addressId").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(routeStop))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetRoutes_thenReturns200() throws Exception {
        // given
        when(routeService.getFutureNotCanceledRoutes(any(), any(), any(), any())).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/route/allRoutes?currPage=1&sortBy=none&filter=none").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetTop15Users_thenReturns200() throws Exception {
        // given
        when(routeService.getTop15RidersByNumberOfPassengers()).thenReturn(new ArrayList<TopUser>() {{ add(topUser); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/top15Users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<TopUser>() {{ add(topUser); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetTop15UsersByDrives_thenReturns200() throws Exception {
        // given
        when(routeService.getTop15RidersByNumberOfDrives()).thenReturn(new ArrayList<TopUser>() {{ add(topUser); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/top15UsersByDrives").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<TopUser>() {{ add(topUser); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetTop15RidersByRating_thenReturns200() throws Exception {
        // given
        when(routeService.getTop15RidersByRating()).thenReturn(new ArrayList<TopUser>() {{ add(topUser); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/top15UsersByRating").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<TopUser>() {{ add(topUser); }}))
                .isEqualToIgnoringWhitespace(actual);
    }

    @WithMockUser("user")
    @Test
    void whenGetAllRoutes_thenReturns200() throws Exception {
        // given
        when(routeService.getAllRoutes()).thenReturn(new ArrayList<Route>() {{ add(route); }});

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/allRoutes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String actual = mvcResult.getResponse().getContentAsString();

        // then
        assertThat(objectMapper.writeValueAsString(new ArrayList<Route>() {{ add(route); }}))
                .isEqualToIgnoringWhitespace(actual);
    }
}