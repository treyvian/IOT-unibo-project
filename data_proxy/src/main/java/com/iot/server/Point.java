package com.iot.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "point")
public class Point {

    // Attributes
    @JsonProperty("id")
    @Column(tag = true)
    String id;
    @JsonProperty("latitude")
    @Column(tag = true)
    Double latitude;
    @JsonProperty("longitude")
    @Column(tag = true)
    Double longitude;
    
    @JsonProperty("temperature")
    @Column
    Double temperature;
    @JsonProperty("humidity")
    @Column
    Double humidity;
    @JsonProperty("gas")
    @Column
    int gas;
    @JsonProperty("aqi")
    @Column
    int aqi;
    @JsonProperty("rssi")
    @Column
    int rssi;

    @Column(timestamp = true)
    private Instant time;

    // Constructors

    public Point() {
        this.time = Instant.now();
    }

    public Point(String id, 
                 Double temperature, 
                 Double humidity, 
                 int gas, 
                 int aqi, 
                 int rssi, 
                 Double latitude,
                 Double longitude) {

        this.id = id;
        this.temperature = temperature;
        this.humidity = humidity;
        this.gas = gas;
        this.aqi = aqi;
        this.rssi = rssi;
        this.latitude = latitude;
        this.longitude = longitude;

        this.time = Instant.now();
    }


    //methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public int getGas() {
        return gas;
    }

    public void setGas(int gas) {
        this.gas = gas;
    }

    public int getAqi() {
        return aqi;
    }

    public void setAqi(int aqi) {
        this.aqi = aqi;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Point [aqi=" + aqi + 
                    ", gas=" + gas + 
                    ", humidity=" + humidity + 
                    ", id=" + id + 
                    ", latitude=" + latitude +   
                    ", longitude=" + longitude + 
                    ", rssi=" + rssi + 
                    ", temperature=" + temperature + 
                    ", time=" + time
                + "]";
    }
}
