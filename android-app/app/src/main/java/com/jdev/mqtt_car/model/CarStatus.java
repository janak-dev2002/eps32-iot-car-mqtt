package com.jdev.mqtt_car.model;

import com.google.gson.annotations.SerializedName;

public class CarStatus {

    private final String device_id;
    private final String status;
    private final String firmware;

    public CarStatus(String device_id, String status, String firmware) {
        this.device_id = device_id;
        this.status = status;
        this.firmware = firmware;
    }

    public CarStatus(){
        this.device_id = "";
        this.status = "offline";
        this.firmware = "";
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

}
