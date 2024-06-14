package com.example.seizmostat;

import java.util.Date;

public class SeismicData {
    private Date localDateTime;
    private Double localSeismicIntensity;

    public SeismicData(Date localDateTime, Double localSeismicIntensity) {
        this.localDateTime = localDateTime;
        this.localSeismicIntensity = localSeismicIntensity;
    }

    public Date getLocalDateTime() {
        return localDateTime;
    }

    public Double getLocalSeismicIntensity() {
        return localSeismicIntensity;
    }

    @Override
    public String toString() {
        return "SeismicData{" +
                "localDateTime=" + localDateTime +
                ", localSeismicIntensity=" + localSeismicIntensity +
                '}';
    }
}
