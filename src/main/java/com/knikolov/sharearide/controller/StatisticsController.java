package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.models.ChartData;
import com.knikolov.sharearide.service.RouteService;
import com.knikolov.sharearide.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for statistics page
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class StatisticsController {

    private final UserService userService;
    private final RouteService routeService;

    public StatisticsController(UserService userService, RouteService routeService) {
        this.userService = userService;
        this.routeService = routeService;
    }

    @RequestMapping(value = "/pieChartDriversData", method = RequestMethod.GET)
    public List<ChartData> getDriversCount() {
        List<ChartData> chartData = new ArrayList<>();
        chartData.add(new ChartData("Drivers",  this.userService.countByDriverTrue()));
        chartData.add(new ChartData("Nondrivers",  this.userService.countByDriverFalse()));
        return chartData;
    }

    @RequestMapping(value = "/pieChartCitiesData", method = RequestMethod.GET)
    public List<ChartData> getCitiesData() {
        List<ChartData> chartData = new ArrayList<>();
        chartData.add(new ChartData("Drivers",  this.userService.countByDriverTrue()));
        chartData.add(new ChartData("Nondrivers",  this.userService.countByDriverFalse()));
        return chartData;
    }

//    @RequestMapping(value = "/routesBetweenDates", method = RequestMethod.GET)
//    public List<LinearChartData> getRoutesBetweenDates(@RequestParam String startDate, @RequestParam String endDate) {
//        LocalDateTime start = Instant.ofEpochMilli(Long.parseLong(startDate)).atZone(ZoneId.systemDefault()).toLocalDateTime();
//        LocalDateTime end = Instant.ofEpochMilli(Long.parseLong(endDate)).atZone(ZoneId.systemDefault()).toLocalDateTime();
//
//        List<LinearChartData> list = new ArrayList<>();
//        list.add(this.routeService.getAllRoutesBetweenDates(start, end));
//        return list;
//    }

}
