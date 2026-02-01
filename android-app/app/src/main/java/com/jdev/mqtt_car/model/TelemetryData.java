package com.jdev.mqtt_car.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


public class TelemetryData {

    @SerializedName("battery")
    private final int battery;

    @SerializedName("distance_front")
    private final int distanceFront;

    @SerializedName("temperature")
    private final int temperature;

    @SerializedName("current_action")
    private final String currentAction;

    @SerializedName("wifi_rssi")
    private final int wifiRssi;

    @SerializedName("free_heap")
    private final int freeHeap;

    private transient long timestamp; // transient to avoid Gson serialization

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

    /**
     * Default constructor required for Gson deserialization
     */
    public TelemetryData(){
        this.battery = 0;
        this.distanceFront = 0;
        this.temperature = 0;
        this.currentAction = "unknown";
        this.wifiRssi = 0;
        this.freeHeap = 0;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Initialize timestamp after Gson parsing (since transient fields are not set by Gson)
     */
    public void initTimestamp() {
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
