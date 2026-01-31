package com.jdev.mqtt_car.model;

public class CarStatus {

    private final String device_id;
    private final String status;
    private final String firmware;

    public CarStatus(String device_id, String status, String firmware) {
        this.device_id = device_id;
        this.status = status;
        this.firmware = firmware;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getStatus() {
        return status;
    }

    public String getFirmware() {
        return firmware;
    }

    public static CarStatus defaultStatus() {
        return new CarStatus("","offline","");
    }
}
