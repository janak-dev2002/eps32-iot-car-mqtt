package com.jdev.mqtt_car.model;

import androidx.annotation.NonNull;

public class CarCommand {

    private final String action;
    private final String command_id;

    public CarCommand(String action, String command_id) {
        this.action = action;
        this.command_id = command_id;
    }


    @NonNull
    @Override
    public String toString() {

        return "{\"action\": \"" + action + "\", \"command_id\": \"" + command_id + "\"}";
    }
}
