package com.knikolov.sharearide.models;

import java.util.List;

public class LinearChartData {
    private String name;
    private List<ChartData> series;

    public LinearChartData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChartData> getSeries() {
        return series;
    }

    public void setSeries(List<ChartData> series) {
        this.series = series;
    }
}
