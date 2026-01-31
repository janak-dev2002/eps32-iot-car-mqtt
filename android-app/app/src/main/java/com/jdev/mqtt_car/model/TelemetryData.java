package com.jdev.mqtt_car.model;

import androidx.annotation.NonNull;

/**
 * Data model representing telemetry information received from the IoT car.
 * Used as a data holder in MVVM architecture for LiveData observations.
 */
public class TelemetryData {

    private final int battery;
    private final int distanceFront;
    private final int temperature;
    private final String currentAction;
    private final int wifiRssi;
    private final int freeHeap;
    private final long timestamp;

    public TelemetryData(int battery, int distanceFront, int temperature,
                         String currentAction, int wifiRssi, int freeHeap) {
        this.battery = battery;
        this.distanceFront = distanceFront;
        this.temperature = temperature;
        this.currentAction = currentAction;
        this.wifiRssi = wifiRssi;
        this.freeHeap = freeHeap;
        this.timestamp = System.currentTimeMillis();
    }

    // ========================================
    // Getters
    // ========================================

    public int getBattery() {
        return battery;
    }

    public int getDistanceFront() {
        return distanceFront;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public int getWifiRssi() {
        return wifiRssi;
    }

    public int getFreeHeap() {
        return freeHeap;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ========================================
    // Display Helpers (for UI binding)
    // ========================================

    public String getBatteryDisplay() {
        return battery + "%";
    }

    public String getDistanceDisplay() {
        return distanceFront + "cm";
    }

    public String getTemperatureDisplay() {
        return temperature + "°C";
    }

    public String getRssiDisplay() {
        return wifiRssi + " dBm";
    }

    public String getActionDisplay() {
        if (currentAction == null || currentAction.equalsIgnoreCase("stop")) {
            return "IDLE";
        }
        return currentAction.toUpperCase();
    }

    public String getFreeHeapDisplay() {
        return (freeHeap / 1024) + " KB";
    }

    // ========================================
    // Factory method for default/empty state
    // ========================================

    public static TelemetryData empty() {
        return new TelemetryData(0, 0, 0, "unknown", 0, 0);
    }

    @NonNull
    @Override
    public String toString() {
        return "TelemetryData{" +
                "battery=" + battery +
                ", distanceFront=" + distanceFront +
                ", temperature=" + temperature +
                ", currentAction='" + currentAction + '\'' +
                ", wifiRssi=" + wifiRssi +
                ", freeHeap=" + freeHeap +
                ", timestamp=" + timestamp +
                '}';
    }
}
