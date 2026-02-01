package com.jdev.mqtt_car.data.source;

import android.content.Context;
import android.util.Log;

import com.jdev.mqtt_car.model.CarCommand;

import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

public class MqttManager {
    private static final String TAG = "MqttManager";
    private static final String CLIENT_ID = "android-app-" + System.currentTimeMillis();

    private final String deviceId;
    private final MqttAndroidClient mqttClient;
    private final MqttCallback listener;

    public interface MqttCallback {
        void onConnected();

        void onDisconnected();

        void onTelemetryReceived(int battery, int distance, int temperature, String currentAction, int wifiRssi,
                int freeHeap);

        void onCarStatusReceived(String device_id, String status, String firmware);

        void onError(String message);
    }

    public MqttManager(Context context, MqttCallback listener) {
        this.listener = listener;

        // Load configuration from SharedPreferences
        MqttPreferences prefs = new MqttPreferences(context);
        // Dynamic configuration from SharedPreferences
        String brokerUrl = prefs.getBrokerUrl();
        this.deviceId = prefs.getDeviceId();

        Log.d(TAG, "Connecting to: " + brokerUrl + " as device: " + deviceId);

        this.mqttClient = new MqttAndroidClient(context, brokerUrl, CLIENT_ID);

        mqttClient.setCallback(new org.eclipse.paho.client.mqttv3.MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "Connection lost", cause);
                listener.onDisconnected();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                handleMessage(topic, new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    public void connect() {

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            // No username/password for anonymous Mosquitto
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connected to Mosquitto");
                    subscribeToTopics();
                    listener.onConnected();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect", exception);
                    listener.onError("Connection failed: " + exception.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Connect error", e);
            listener.onError("Connect error: " + e.getMessage());
        }
    }

    private void subscribeToTopics() {
        try {
            String telemetryTopic = "iot-car/" + deviceId + "/telemetry";
            String statusTopic = "iot-car/" + deviceId + "/status";

            mqttClient.subscribe(telemetryTopic, 0);
            mqttClient.subscribe(statusTopic, 1);
            Log.d(TAG, "Subscribed to: " + telemetryTopic);
            Log.d(TAG, "Subscribed to: " + statusTopic);
        } catch (Exception e) {
            Log.e(TAG, "Subscribe error", e);
        }
    }

    private void handleMessage(String topic, String payload) {

        Log.d(TAG, "Message: " + topic + " -> " + payload);

        try {
            JSONObject json = new JSONObject(payload);

            if (topic.contains("/telemetry")) {
                // Parse all telemetry fields from ESP32
                int battery = json.optInt("battery", 0);
                int distance = json.optInt("distance_front", 0);
                int temperature = json.optInt("temperature", 0);
                String currentAction = json.optString("current_action", "unknown");
                int wifiRssi = json.optInt("wifi_rssi", 0);
                int freeHeap = json.optInt("free_heap", 0);

                listener.onTelemetryReceived(battery, distance, temperature, currentAction, wifiRssi, freeHeap);
            } else if (topic.contains("/status")) {
                // Handle status updates from ESP3)
                String deviceId = json.optString("device_id");
                String currentStatus = json.optString("status", "unknown");
                String firmware = json.optString("firmware");
                listener.onCarStatusReceived(deviceId, currentStatus,firmware);
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
        }
    }


    public void sendCommand(String action) {
        try {
            // {"action": "forward", "command_id": "cmd-123456789"}
            CarCommand command = new CarCommand(action, "cmd-" + System.currentTimeMillis());
            String topic = "iot-car/" + deviceId + "/command";
            MqttMessage message = new MqttMessage(command.toString().getBytes());
            message.setQos(1);

            mqttClient.publish(topic, message);
            Log.d(TAG, "Sent command: " + command.toString());

        } catch (Exception e) {
            Log.e(TAG, "Send command error", e);
            listener.onError("Failed to send command");
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Disconnect error", e);
        }
    }
}