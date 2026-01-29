#ifndef CONFIG_H
#define CONFIG_H

// ============================================
// IoT Car ESP32 - Configuration for Wokwi Simulation
// ============================================

// WiFi Configuration (Wokwi Simulation)
// Wokwi-GUEST is a special SSID that enables WiFi Gateway
const char* WIFI_SSID = "Wokwi-GUEST";
const char* WIFI_PASSWORD = "";

// MQTT Broker Configuration
// host.wokwi.internal resolves to your PC (localhost) in Wokwi
const char* MQTT_BROKER = "host.wokwi.internal";
const int MQTT_PORT = 1883;

// Device Identity
const char* DEVICE_ID = "car-001";
const char* MQTT_CLIENT_ID = "esp32-car-001";

// MQTT Topics
const char* TOPIC_TELEMETRY = "iot-car/car-001/telemetry";
const char* TOPIC_COMMAND = "iot-car/car-001/command";
const char* TOPIC_STATUS = "iot-car/car-001/status";
const char* TOPIC_RESPONSE = "iot-car/car-001/response";
const char* TOPIC_TEST = "iot-car/car-001/test";

// ============================================
// GPIO Pin Definitions (matches diagram.json)
// ============================================

// Ultrasonic Sensor (HC-SR04)
#define PIN_ULTRASONIC_TRIG 32
#define PIN_ULTRASONIC_ECHO 33

// Timing Configuration
#define TELEMETRY_INTERVAL 5000  // Send telemetry every 5 seconds
#define MQTT_RECONNECT_DELAY 5000

#endif // CONFIG_H
