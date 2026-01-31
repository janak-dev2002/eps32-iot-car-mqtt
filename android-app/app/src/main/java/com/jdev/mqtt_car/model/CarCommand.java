package com.jdev.mqtt_car.model;

public class CarCommand {

    private final String action;
    private final String command_id;

    public CarCommand(String action, String command_id) {
        this.action = action;
        this.command_id = command_id;
    }

    public String getAction() {
        return action;
    }

    public String getCommand_id() {
        return command_id;
    }
}
