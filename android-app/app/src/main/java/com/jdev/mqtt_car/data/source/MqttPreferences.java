package com.jdev.mqtt_car.data.source;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class for managing MQTT connection preferences.
 * Stores broker IP, port, and future authentication credentials.
 */
public class MqttPreferences {
    private static final String PREF_NAME = "mqtt_preferences";

    // Keys
    private static final String KEY_BROKER_IP = "broker_ip";
    private static final String KEY_BROKER_PORT = "broker_port";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember_credentials";
    private static final String KEY_IS_CONFIGURED = "is_configured";

    // Default values
    private static final String DEFAULT_IP = "192.168.1.1";
    private static final int DEFAULT_PORT = 1883;
    private static final String DEFAULT_DEVICE_ID = "car-001";

    private final SharedPreferences prefs;

    public MqttPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ========================================
    // Broker Configuration
    // ========================================

    public void saveBrokerConfig(String ip, int port, String deviceId) {
        prefs.edit()
                .putString(KEY_BROKER_IP, ip)
                .putInt(KEY_BROKER_PORT, port)
                .putString(KEY_DEVICE_ID, deviceId)
                .putBoolean(KEY_IS_CONFIGURED, true)
                .apply();
    }

    public String getBrokerIp() {
        return prefs.getString(KEY_BROKER_IP, DEFAULT_IP);
    }

    public int getBrokerPort() {
        return prefs.getInt(KEY_BROKER_PORT, DEFAULT_PORT);
    }

    public String getDeviceId() {
        return prefs.getString(KEY_DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    public String getBrokerUrl() {
        return "tcp://" + getBrokerIp() + ":" + getBrokerPort();
    }

    // ========================================
    // Authentication (Future Use)
    // ========================================

    public void saveCredentials(String username, String password) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, "");
    }

    public boolean hasCredentials() {
        return !getUsername().isEmpty() && !getPassword().isEmpty();
    }

    // ========================================
    // Remember & Configuration State
    // ========================================

    public void setRememberCredentials(boolean remember) {
        prefs.edit().putBoolean(KEY_REMEMBER, remember).apply();
    }

    public boolean shouldRemember() {
        return prefs.getBoolean(KEY_REMEMBER, true);
    }

    public boolean isConfigured() {
        return prefs.getBoolean(KEY_IS_CONFIGURED, false);
    }

    // ========================================
    // Clear Data
    // ========================================

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    public void clearCredentials() {
        prefs.edit()
                .remove(KEY_USERNAME)
                .remove(KEY_PASSWORD)
                .apply();
    }
}
