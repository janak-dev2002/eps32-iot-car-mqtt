/*
 * ============================================
 * IoT Car ESP32 - MQTT Connection Test
 * ============================================
 *
 * This is a simple test program to verify:
 * 1. WiFi connection (via Wokwi Gateway)
 * 2. MQTT broker connection (Docker Mosquitto)
 * 3. MQTT publish/subscribe functionality
 * 4. GPIO control (LEDs representing motors)
 * 5. Ultrasonic sensor reading
 *
 * For Wokwi Simulation Testing
 */

#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <NewPing.h>
#include <config.h>
#include <motor_control.h>

// ============================================
// Global Objects
// ============================================
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

// Ultrasonic sensor object (Trig, Echo, Max Distance)
#define MAX_DISTANCE 400 // Maximum distance in cm
NewPing sonar(PIN_ULTRASONIC_TRIG, PIN_ULTRASONIC_ECHO, MAX_DISTANCE);

// Timing variables
unsigned long lastTelemetryTime = 0;
unsigned long lastHeartbeat = 0;
unsigned int messageCount = 0;

// Motor/LED state tracking
String currentCommand = "stop";
String previousCommand = "stop";
unsigned long commandStartTime = 0;
const unsigned long COMMAND_DURATION = 2000; // LEDs stay on for 2 seconds

bool isWiFiConnected = false;

// ============================================
// Function Prototypes // this is needed because C++11 standard, that mean function declarations must be before usage
// ============================================
void setupWiFi();
void setupMQTT();
void reconnectMQTT();
void mqttCallback(char *topic, byte *payload, unsigned int length);
void sendTelemetry();
void sendTestMessage();
void handleCommand(const char *command);
void setupPins();
void testMotorLEDs();
float readUltrasonic();
void updateMotorLEDs();

void setup()
{

    Serial.begin(115200);
    while (!Serial && millis() < 5000)
    {
        ; // Wait for serial port to connect, max 5 seconds
    }
    delay(100);

    Serial.println();
    Serial.println();
    Serial.println("===========================================");
    Serial.println("   IoT Car ESP32 - MQTT Test Program");
    Serial.println("         Wokwi Simulation Mode");
    Serial.println("===========================================");
    Serial.println();
    Serial.flush(); // Ensure all serial output is sent

    // Initialize GPIO pins
    setupPins();

    // Quick LED test
    Serial.println("[TEST] Running LED test sequence...");
    testMotorLEDs();

    // Connect to WiFi
    setupWiFi();

    // Setup MQTT
    if (isWiFiConnected)
    {
        setupMQTT();
        Serial.println();
        Serial.println(" Setup complete! Entering main loop...");
        Serial.println("────────────────────────────────────────");
    }
    else
    {
        Serial.println("[MQTT] Skipping MQTT setup due to WiFi failure");
    }
}

// ============================================
// Main Loop
// ============================================
void loop()
{
    // Ensure MQTT connection
    if (!mqttClient.connected() && isWiFiConnected)
    {
        reconnectMQTT();
    }

    mqttClient.loop(); // Process incoming MQTT messages. this will call mqttCallback when a message arrives

    // Update motor LEDs based on current command
    updateMotorLEDs(); // methana idan code eka ayee refer karanna patan ganna heta

    // Send telemetry periodically
    if (millis() - lastTelemetryTime >= TELEMETRY_INTERVAL)
    {
        sendTelemetry();
        lastTelemetryTime = millis();
    }

    // Heartbeat LED blink (built-in LED if available)
    if (millis() - lastHeartbeat >= 1000)
    {
        Serial.print("."); // Heartbeat indicator
        lastHeartbeat = millis();
    }
}

// ============================================
// WiFi Setup
// ============================================
void setupWiFi()
{

    Serial.print("[WiFi] Connecting to ");
    Serial.print(WIFI_SSID);

    WiFi.mode(WIFI_STA); // Set WiFi to station mode, that mean it will connect to AP instead of creating AP
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    int attempts = 0;

    while (WiFi.status() != WL_CONNECTED && attempts < 30)
    {
        delay(500);
        Serial.print(".");
        attempts++;
    }

    if (WiFi.status() == WL_CONNECTED)
    {
        Serial.println(" Connected!");
        isWiFiConnected = true;
        Serial.print("[WiFi] IP Address: ");
        Serial.println(WiFi.localIP());
        Serial.print("[WiFi] Signal Strength (RSSI): ");
        Serial.print(WiFi.RSSI());
        Serial.println(" dBm");
    }
    else
    {
        Serial.println(" FAILED!");
        Serial.println("[WiFi] Could not connect to WiFi");
        isWiFiConnected = false;
    }
}

// ============================================
// MQTT Setup
// ============================================
void setupMQTT()
{
    mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
    mqttClient.setCallback(mqttCallback);
    mqttClient.setBufferSize(512); // increase buffer size for larger messages. 512 mean 512 bytes

    Serial.print("[MQTT] Broker: ");
    Serial.print(MQTT_BROKER);
    Serial.print(":");
    Serial.println(MQTT_PORT);
}

void reconnectMQTT()
{

    while (!mqttClient.connected())
    {

        Serial.print("[MQTT] Attempting connection...");

        // Create a unique client ID
        String clientId = MQTT_CLIENT_ID;
        clientId += "-";
        clientId += String(random(0xffff), HEX); //

        // In reconnectMQTT(), change connect to:
        if (mqttClient.connect(clientId.c_str(),
                               NULL,                                                // username
                               NULL,                                                // password
                               TOPIC_STATUS,                                        // LWT topic
                               0,                                                   // LWT QoS
                               true,                                                // LWT retain
                               "{\"device_id\":\"car-001\",\"status\":\"offline\"}" // LWT message
                               ))
        {

            
            Serial.println(" Connected!");

            // Subscribe to command topic
            mqttClient.subscribe(TOPIC_COMMAND);
            Serial.print("[MQTT] Subscribed to: ");
            Serial.println(TOPIC_COMMAND);


            // Publish online status
            StaticJsonDocument<128> statusDoc;
            statusDoc["device_id"] = DEVICE_ID;
            statusDoc["status"] = "online";
            statusDoc["firmware"] = "test-v1.0";

            char statusBuffer[128];
            serializeJson(statusDoc, statusBuffer);
            mqttClient.publish(TOPIC_STATUS, statusBuffer, true);
            Serial.println("[MQTT] Published online status");

            // Send initial test message
            //sendTestMessage();
        }
        else
        {
            Serial.print(" Failed, rc=");
            Serial.print(mqttClient.state());
            Serial.println(" - Retrying in 5 seconds...");
            delay(MQTT_RECONNECT_DELAY);
        }
    }
}

// ============================================
// MQTT Callback - Handle Incoming Messages
// ============================================

void mqttCallback(char *topic, byte *payload, unsigned int length)
{
    Serial.println();
    Serial.println("────────────────────────────────────────");
    Serial.print("[MQTT] Message received on topic: ");
    Serial.println(topic);

    // Convert payload to string
    char message[length + 1];         // +1 for null terminator, that mean this helps to make string
    memcpy(message, payload, length); // copy payload to message
    message[length] = '\0';           // null terminate the string

    Serial.print("[MQTT] Payload: ");
    Serial.println(message);

    // Parse JSON command
    StaticJsonDocument<256> doc;
    DeserializationError error = deserializeJson(doc, message);

    if (error)
    {
        Serial.print("[MQTT] JSON parse error: ");
        Serial.println(error.c_str());
        return;
    }

    // Handle command
    if (doc.containsKey("action"))
    {
        const char *action = doc["action"];
        Serial.print("[MQTT] Action found: ");
        Serial.println(action);
        handleCommand(action);
    }
    else if (doc.containsKey("test"))
    {
        Serial.println("[MQTT] Test message received successfully!");
    }
    else
    {
        Serial.println("[MQTT] No 'action' or 'test' key found in message");
    }

    Serial.println("────────────────────────────────────────");
}

// ============================================
// Command Handler - Control Motors/LEDs
// ============================================
void handleCommand(const char *command)
{
    Serial.print("[CMD] Executing command: ");
    Serial.println(command);

    // Update current command and reset timer
    currentCommand = String(command);
    commandStartTime = millis();

    // The LED state will be updated in updateMotorLEDs()
    Serial.print("[CMD] Current command set to: ");
    Serial.println(currentCommand);

    // Send command acknowledgment
    StaticJsonDocument<128> response;
    response["device_id"] = DEVICE_ID;
    response["command"] = command;
    response["status"] = "executed";
    response["timestamp"] = millis();

    char responseBuffer[128];
    serializeJson(response, responseBuffer);
    mqttClient.publish(TOPIC_RESPONSE, responseBuffer);
}

// ============================================
// Telemetry - Send Sensor Data
// ============================================
void sendTelemetry()
{
    messageCount++;

    float distance = readUltrasonic();

    StaticJsonDocument<256> doc;
    doc["device_id"] = DEVICE_ID;
    doc["timestamp"] = millis();
    doc["message_count"] = messageCount;
    doc["distance_front"] = distance;
    doc["wifi_rssi"] = WiFi.RSSI();
    doc["free_heap"] = ESP.getFreeHeap(); // Free heap memory. that mean this helps to monitor memory usage

    char buffer[256];
    serializeJson(doc, buffer); // Convert JSON document to string

    // PubSubClient publishes with QoS 0 by default (no QoS parameter available)
    mqttClient.publish(TOPIC_TELEMETRY, buffer);

    Serial.println();
    Serial.print("[TELEMETRY] #");
    Serial.print(messageCount);
    Serial.print(" | Distance: ");
    Serial.print(distance);
    Serial.print(" cm | RSSI: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
}

// ============================================
// Test Message
// ============================================
void sendTestMessage()
{
    StaticJsonDocument<256> doc;
    doc["device_id"] = DEVICE_ID;
    doc["type"] = "connection_test";
    doc["message"] = "Hello from ESP32! MQTT connection successful!";
    doc["timestamp"] = millis();
    doc["simulation"] = true;

    char buffer[256];
    serializeJson(doc, buffer);

    mqttClient.publish(TOPIC_TEST, buffer);
    Serial.println("[TEST] Sent connection test message");
}

// ============================================
// GPIO Pin Setup
// ============================================
/**
 * @brief Initializes and configures all GPIO pins for the ESP32 IoT car.
 *
 * This function sets up the pin modes and initial states for:
 * - Motor control pins (Motor A and B, forward and reverse directions)
 * - Ultrasonic sensor pins (trigger and echo)
 *
 * All motor control and sensor trigger pins are initialized to LOW state
 * to ensure motors and sensors are in a safe, inactive state at startup.
 *
 * @note The ultrasonic echo pin is intentionally not initialized to LOW
 *       because it is configured as an INPUT pin. Input pins should not
 *       have their output state explicitly set via digitalWrite(), as they
 *       are designed to read external signals rather than drive outputs.
 *
 * @return void
 *
 * @see PIN_MOTOR_A_FWD, PIN_MOTOR_A_REV, PIN_MOTOR_B_FWD, PIN_MOTOR_B_REV
 * @see PIN_ULTRASONIC_TRIG, PIN_ULTRASONIC_ECHO
 */
void setupPins()
{

    Serial.println("[GPIO] Configuring pins...");

    // Motor LEDs as outputs
    setupMotors(); // This method responsible for both motor pin as output and initial output as LOW

    // Ultrasonic sensor setup
    pinMode(PIN_ULTRASONIC_TRIG, OUTPUT);
    pinMode(PIN_ULTRASONIC_ECHO, INPUT);

    // Initialize ultrasonic sensor
    digitalWrite(PIN_ULTRASONIC_TRIG, LOW);

    Serial.println("[GPIO] Pins configured");
}

// ============================================
// LED Test Sequence
// ============================================
void testMotorLEDs()
{

    Serial.println("[TEST] Motors Forward (Green)");
    moveForward();
    delay(100);
    stopMotors();

    Serial.println("[TEST] Motos Reverse (Red)");
    moveBackward();
    delay(100);
    stopMotors();

    Serial.println("[TEST] LED test complete");
}

// ============================================
// Ultrasonic Sensor Reading (Using NewPing Library)
// ============================================
float readUltrasonic()
{
    // Use NewPing library for simplified ultrasonic reading
    unsigned int distance = sonar.ping_cm();

    // ping_cm() returns 0 if out of range or no echo
    if (distance == 0)
    {
        return 999.0; // Out of range
    }

    return (float)distance;
}

// float readUltrasonic()
// {

//     // triger a pulse
//     digitalWrite(PIN_ULTRASONIC_TRIG, LOW);
//     delay(2);
//     digitalWrite(PIN_ULTRASONIC_TRIG, HIGH);
//     delay(10);
//     digitalWrite(PIN_ULTRASONIC_TRIG, LOW);

//     long duration = pulseIn(PIN_ULTRASONIC_ECHO, HIGH, 30000); // why 30000 ? => timeout for pulseIn and duration coming from here is in microseconds

//     // v = (dis/t)*2
//     // dis = (v/2)t

//     float sound_velocity = 343.0/1000000.0;                         // consider as 343 m/s
//     float distance = (sound_velocity / 2) * duration; // distance coming as m

//     float distance_cm = distance * 100;
//     Serial.print("Distance === ");
//     Serial.println(distance_cm);

//     // clam to resonable value

//     if (distance_cm <= 0 || distance_cm > 400)
//     {
//         distance_cm = 999.0; // out-of-range
//     }

//     return distance_cm;
// }

// float readUltrasonic() {
//     // Send trigger pulse
//     digitalWrite(PIN_ULTRASONIC_TRIG, LOW);
//     delayMicroseconds(2);
//     digitalWrite(PIN_ULTRASONIC_TRIG, HIGH);
//     delayMicroseconds(10);
//     digitalWrite(PIN_ULTRASONIC_TRIG, LOW);

//     // Read echo pulse duration
//     long duration = pulseIn(PIN_ULTRASONIC_ECHO, HIGH, 30000);

//     // Calculate distance in cm
//     // Speed of sound = 343 m/s = 0.0343 cm/µs
//     // Distance = (duration * 0.0343) / 2
//     float distance = (duration * 0.0343) / 2.0;

//     // Clamp to reasonable values
//     if (distance <= 0 || distance > 400) {
//         distance = 999.0;  // Out of range
//     }

//     return distance;
// }

// ============================================
// Update Motor LEDs based on current command
// ============================================
void updateMotorLEDs()
{

    // Check if command has timed out (auto-stop after COMMAND_DURATION)
    if (currentCommand != "stop" && (millis() - commandStartTime) > COMMAND_DURATION)
    {
        currentCommand = "stop";
        Serial.println();
        Serial.println("[CMD] Command timeout - Auto STOP");
    }

    // Only update LEDs if command has changed
    if (currentCommand != previousCommand)
    {
        Serial.println();
        Serial.print("[LED] Command changed from '");
        Serial.print(previousCommand);
        Serial.print("' to '");
        Serial.print(currentCommand);
        Serial.println("'");

        previousCommand = currentCommand;

        // Turn off all motor LEDs first
        stopMotors();

        // Set LEDs based on current command
        if (currentCommand == "forward")
        {
            Serial.println("[LED] FORWARD: Turning ON pins 25 & 27");
            moveForward();
        }
        else if (currentCommand == "backward")
        {
            Serial.println("[LED] BACKWARD: Turning ON pins 26 & 14");
            moveBackward();
        }
        else if (currentCommand == "left")
        {
            Serial.println("[LED] LEFT: Turning ON pins 26 & 27");
            turnLeft();
        }
        else if (currentCommand == "right")
        {
            Serial.println("[LED] RIGHT: Turning ON pins 25 & 14");
            turnRight();
        }
        else
        {
            Serial.println("[LED] STOP: All LEDs OFF");
        }
    }
}
