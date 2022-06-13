package com.iot.server;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Measurement(name = "point")
public class Point {

    // Attributes
    @Column(tag = true)
    String id;
    @Column
    Double temperature;
    @Column
    Double humidity;
    @Column
    int gas;
    @Column
    int aqi;

    @Column(timestamp = true)
    private Instant time;

    public Point() {
        this.time = Instant.now().plus(2, ChronoUnit.HOURS);
    }

    public Point(String id, Double temperature, Double humidity, int gas, int aqi) {
        this.id = id;
        this.temperature = temperature;
        this.humidity = humidity;
        this.gas = gas;
        this.aqi = aqi;

        this.time = Instant.now().plus(2, ChronoUnit.HOURS);
    }

    //methods

    public Double getTemperature() { return temperature; }

    public Double getHumidity() {
        return humidity;
    }

    public int getGas() {
        return gas;
    }

    public int getAqi() {
        return aqi;
    }

    public String getId() {
        return id;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Point{" +
                "id='" + id + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", gas=" + gas +
                ", aqi=" + aqi +
                ", time=" + time +
                '}';
    }
}
