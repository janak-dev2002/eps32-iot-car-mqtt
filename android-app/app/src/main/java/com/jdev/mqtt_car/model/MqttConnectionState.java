package com.jdev.mqtt_car.model;

/**
 * Enum representing MQTT connection states.
 * Used by ViewModel to update UI reactively.
 */
public enum MqttConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
