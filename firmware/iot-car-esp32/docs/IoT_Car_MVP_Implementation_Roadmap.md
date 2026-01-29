# 🗺️ IoT Car MVP - Step-by-Step Implementation Roadmap

> **Goal:** Complete the 7 MUST-HAVE features with Go backend + Android Java app
> **Timeline:** 2-3 weeks (focused work)
> **Philosophy:** One small step at a time. Finish each step before moving on.

---

## 📋 Quick Reference

### Scope (MUST-HAVE Only)

| # | Feature | Layer |
|---|---------|-------|
| 1 | MQTT Communication | All |
| 2 | Basic Motor Control | Firmware |
| 3 | Distance Sensing | Firmware |
| 4 | Real-time Dashboard | Mobile App |
| 5 | Remote Control | Mobile App → Firmware |
| 6 | TLS Security | MQTT Broker |
| 7 | JSON Data Format | All |

### Tech Stack (Finalized)

| Layer | Technology |
|-------|------------|
| **Firmware** | ESP32 + PlatformIO + PubSubClient |
| **MQTT Broker** | Mosquitto (Local) |
| **Backend** | Go + Eclipse Paho MQTT |
| **Mobile App** | Android Java + Eclipse Paho Android |
| **Data Format** | JSON |

### 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    YOUR PC (Windows)                                    │
│                                                                                         │
│   ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│   │                         MOSQUITTO BROKER (localhost:1883)                       │   │
│   │                                                                                 │   │
│   │    Topics:                                                                      │   │
│   │    ├── iot-car/car-001/telemetry    ← Device publishes sensor data              │   │
│   │    ├── iot-car/car-001/status       ← Device publishes online/offline           │   │
│   │    ├── iot-car/car-001/command      → Device subscribes for commands            │   │
│   │    └── iot-car/car-001/response     ← Device publishes command ACKs             │   │
│   │                                                                                 │   │
│   └───────────────▲─────────────────────────────▲───────────────────────────────────┘   │
│                   │                             │                                       │
│                   │ TCP :1883                   │ TCP :1883                             │
│                   │                             │                                       │
│   ┌───────────────┴───────────────┐   ┌─────────┴───────────────────────────────────┐   │
│   │       GO BACKEND              │   │              ANDROID APP                    │   │
│   │                               │   │         (on your phone)                     │   │
│   │   ┌─────────────────────┐     │   │                                             │   │
│   │   │  MQTT Subscriber    │     │   │   ┌─────────────────────────────────────┐   │   │
│   │   │  - Telemetry        │     │   │   │         MQTT Manager                │   │   │
│   │   │  - Status           │     │   │   │  - Subscribe: telemetry             │   │   │
│   │   └─────────────────────┘     │   │   │  - Publish: commands                │   │   │
│   │                               │   │   └─────────────────────────────────────┘   │   │
│   │   ┌─────────────────────┐     │   │                                             │   │
│   │   │  Data Processing    │     │   │   ┌─────────────────────────────────────┐   │   │
│   │   │  - Parse JSON       │     │   │   │         Control UI                  │   │   │
│   │   │  - Log data         │     │   │   │  ▲ Forward  ◀ Left  ▶ Right  ▼ Back│   │   │
│   │   │  - (Future: Store)  │     │   │   │          ⏹ STOP                    │   │   │
│   │   └─────────────────────┘     │   │   └─────────────────────────────────────┘   │   │
│   │                               │   │                                             │   │
│   └───────────────────────────────┘   │   ┌─────────────────────────────────────┐   │   │
│                                       │   │         Telemetry Display           │   │   │
│                                       │   │  🔋 Battery: 85%  📏 Distance: 45cm│   │   │
│                                       │   └─────────────────────────────────────┘   │   │
│                                       │                                             │   │
│                                       └─────────────────────────────────────────────┘   │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                            │
                                            │ WiFi (Same Network)
                                            │
┌───────────────────────────────────────────▼─────────────────────────────────────────────┐
│                                    IOT CAR (ESP32)                                      │
│                                                                                         │
│   ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│   │                              ESP32 MICROCONTROLLER                              │   │
│   │                                                                                 │   │
│   │   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                      │   │
│   │   │  WiFi Module │    │ MQTT Client  │    │ JSON Parser  │                      │   │
│   │   │  (Built-in)  │    │ (PubSubClient)    │ (ArduinoJson)│                      │   │
│   │   └──────────────┘    └──────────────┘    └──────────────┘                      │   │
│   │                                                                                 │   │
│   │   ┌──────────────────────────────────────────────────────────────────────────┐  │   │
│   │   │                           MAIN LOOP                                      │  │   │
│   │   │  1. Keep MQTT connected                                                  │  │   │
│   │   │  2. Read sensors → Publish telemetry (every 1 sec)                       │  │   │
│   │   │  3. Process incoming commands → Control motors                           │  │   │
│   │   │  4. Send command acknowledgments                                         │  │   │
│   │   └──────────────────────────────────────────────────────────────────────────┘  │   │
│   │                                                                                 │   │
│   └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                         │
│   ┌─────────────────────────────┐              ┌────────────────────────────────────┐   │
│   │      MOTOR DRIVER (L298N)   │              │       SENSORS                      │   │
│   │                             │              │                                    │   │
│   │   IN1 ← GPIO25              │              │   HC-SR04 Ultrasonic               │   │
│   │   IN2 ← GPIO26              │              │   ├── TRIG ← GPIO32                │   │
│   │   IN3 ← GPIO27              │              │   └── ECHO → GPIO33                │   │
│   │   IN4 ← GPIO14              │              │                                    │   │
│   │                             │              │   (Future: GPS, Battery Monitor)   │   │
│   │   OUT1,2 → Motor A (Left)   │              │                                    │   │
│   │   OUT3,4 → Motor B (Right)  │              │                                    │   │
│   │                             │              │                                    │   │
│   └─────────────────────────────┘              └────────────────────────────────────┘   │
│                                                                                         │
│   ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│   │                              POWER SYSTEM                                       │   │
│   │         Battery (7-12V) → L298N → 5V Regulator → ESP32                          │   │
│   └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                               DATA FLOWS                                                │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  1️⃣ TELEMETRY FLOW (Device → Cloud)                                                     │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐       │
│  │ Sensors  │────►│  ESP32   │────►│ Mosquitto│────►│Go Backend│     │          │       │
│  │ HC-SR04  │     │  JSON    │     │  Broker  │────►│  Logs    │     │          │       │
│  └──────────┘     └──────────┘     └──────────┘     └──────────┘     │ Android  │       │
│                                          │                           │   App    │       │
│                                          └──────────────────────────►│ Display  │       │
│                                                                      └──────────┘       │
│                                                                                         │
│  2️⃣ COMMAND FLOW (Phone → Device)                                                       │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐       │
│  │ Android  │────►│  MQTT    │────►│ Mosquitto│────►│  ESP32   │────►│  Motors  │       │
│  │ UI Touch │     │ Publish  │     │  Broker  │     │  Parse   │     │  Move!   │       │
│  └──────────┘     └──────────┘     └──────────┘     └──────────┘     └──────────┘       │
│                                                                                         │
│  3️⃣ ACKNOWLEDGMENT FLOW (Device → Phone)                                                │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐                        │
│  │  ESP32   │────►│ Response │────►│ Mosquitto│────►│ Android  │                        │
│  │ Executed │     │  JSON    │     │  Broker  │     │ Confirm  │                        │
│  └──────────┘     └──────────┘     └──────────┘     └──────────┘                        │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### 🔌 Network Configuration

```
┌─────────────────────────────────────────────────────────────────┐
│                    NETWORK SETUP                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Your WiFi Router (e.g., 192.168.1.1)                          │
│         │                                                       │
│         ├── Your PC (e.g., 192.168.1.100)                       │
│         │     └── Mosquitto Broker listening on :1883           │
│         │     └── Go Backend connecting to localhost:1883       │
│         │                                                       │
│         ├── ESP32 (e.g., 192.168.1.105)                         │
│         │     └── Connects to 192.168.1.100:1883                │
│         │                                                       │
│         └── Android Phone (e.g., 192.168.1.110)                 │
│               └── Connects to 192.168.1.100:1883                │
│                                                                 │
│   ⚠️ All devices must be on the SAME WiFi network!             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏁 Phase 0: Environment Setup (Day 1)

> **Goal:** All tools installed and Mosquitto broker running. Zero coding yet.

### Step 0.1: Install Mosquitto MQTT Broker (30 min)

- [ ] **Install Mosquitto on Windows**
  1. Download from https://mosquitto.org/download/
  2. Run the installer (choose "Install as service")
  3. Add Mosquitto to PATH:
     - Add `C:\Program Files\mosquitto` to System Environment Variables → Path
  4. Verify installation:
     ```powershell
     mosquitto -v
     ```

- [ ] **Configure Mosquitto**
  1. Open `C:\Program Files\mosquitto\mosquitto.conf`
  2. Add these lines at the end:
     ```
     # Allow connections from any IP
     listener 1883
     allow_anonymous true
     
     # Enable WebSocket for testing (optional)
     listener 9001
     protocol websockets
     ```
  3. Restart Mosquitto service:
     ```powershell
     net stop mosquitto
     net start mosquitto
     ```

- [ ] **Find your PC's IP address** (ESP32 will connect to this)
  ```powershell
  ipconfig
  # Look for "IPv4 Address" under your WiFi adapter
  # Example: 192.168.1.100
  ```

- [ ] **Save configuration**
  ```
  Create file: credentials.txt (DO NOT commit to Git!)
  
  MQTT_BROKER_IP=192.168.1.100  # Your PC's IP
  MQTT_PORT=1883
  ```

### Step 0.2: Install Development Tools (1 hour)

- [ ] **For ESP32 Firmware (PlatformIO)**
  - [ ] Install VS Code from https://code.visualstudio.com/
  - [ ] Install PlatformIO extension:
    1. Open VS Code
    2. Go to Extensions (Ctrl+Shift+X)
    3. Search "PlatformIO IDE"
    4. Click Install
    5. Restart VS Code
    6. Wait for PlatformIO to finish installing (see status bar)
  - [ ] Verify PlatformIO:
    - Click the PlatformIO icon (alien head) in the sidebar
    - You should see "PIO Home" tab

- [ ] **For Go Backend**
  - [ ] Install Go 1.21+ from https://go.dev/dl/
  - [ ] Verify: `go version`

- [ ] **For Android App**
  - [ ] Install Android Studio from https://developer.android.com/studio
  - [ ] During setup, install Android SDK 33 or higher
  - [ ] Enable USB debugging on your Android phone

### Step 0.3: Create Project Structure (15 min)

```
IoT MQTT Car/
├── docs/                          # Documentation (already exists)
│   ├── IoT_Car_MVP_Project_Guide.md
│   └── AIoT_Car_MVP_Project_Guide.md
│
├── firmware/                      # ESP32 PlatformIO project
│   └── iot-car-esp32/
│       ├── src/
│       │   └── main.cpp           # Main application code
│       ├── include/
│       │   ├── config.h           # WiFi & MQTT credentials
│       │   ├── motor_control.h    # Motor functions
│       │   └── sensors.h          # Sensor functions
│       └── platformio.ini         # PlatformIO configuration
│
├── backend/                       # Go backend
│   ├── cmd/
│   │   └── server/
│   │       └── main.go
│   ├── internal/
│   │   ├── mqtt/
│   │   └── handlers/
│   ├── go.mod
│   └── go.sum
│
├── android-app/                   # Android Java app
│   └── IoTCarController/
│       └── (Android Studio project)
│
├── credentials.txt                # (gitignored)
└── .gitignore
```

- [ ] Create this folder structure
- [ ] Create `.gitignore`:
  ```
  credentials.txt
  *.env
  .idea/
  *.iml
  build/
  .gradle/
  local.properties
  .pio/
  .vscode/
  ```

### ✅ Phase 0 Checkpoint

Before proceeding, verify:
- [ ] Mosquitto installed and running (`mosquitto -v` works)
- [ ] Your PC's IP address noted (ESP32 will connect to this)
- [ ] PlatformIO extension installed in VS Code
- [ ] Go installed
- [ ] Android Studio installed
- [ ] Project folders created

---

## ⚡ Phase 1: MQTT Foundation (Days 2-3)

> **Goal:** Prove MQTT communication works end-to-end before adding hardware.

### Step 1.1: Test Mosquitto with CLI (30 min)

This step uses NO code - just proving the broker works.

- [ ] **Open TWO PowerShell terminals**

- [ ] **Terminal 1 - Subscribe to test topic:**
  ```powershell
  mosquitto_sub -h localhost -t "iot-car/test/#" -v
  ```
  (This will wait for messages)

- [ ] **Terminal 2 - Publish a test message:**
  ```powershell
  mosquitto_pub -h localhost -t "iot-car/test/hello" -m '{"message": "Hello MQTT!"}'
  ```

- [ ] **Verify Terminal 1 shows:**
  ```
  iot-car/test/hello {"message": "Hello MQTT!"}
  ```

✅ If you see this, Mosquitto is working!

### Step 1.2: Create PlatformIO Project for ESP32 (1-2 hours)

**Hardware Needed:** Just ESP32 board (no motors yet)

- [ ] **Create PlatformIO Project:**
  1. Open VS Code
  2. Click PlatformIO icon (alien head) in sidebar
  3. Click "Create New Project"
  4. Settings:
     - Name: `iot-car-esp32`
     - Board: `Espressif ESP32 Dev Module`
     - Framework: `Arduino`
     - Location: `IoT MQTT Car/firmware/`
  5. Click Finish (wait for initialization)

- [ ] **Update `platformio.ini`:**
  ```ini
  [env:esp32dev]
  platform = espressif32
  board = esp32dev
  framework = arduino
  monitor_speed = 115200
  
  lib_deps =
      knolleary/PubSubClient@^2.8
      bblanchon/ArduinoJson@^6.21.3
  ```

- [ ] **Create `include/config.h`:**
  ```cpp
  #ifndef CONFIG_H
  #define CONFIG_H
  
  // WiFi Configuration
  const char* WIFI_SSID = "YOUR_WIFI_NAME";
  const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
  
  // Mosquitto Broker Configuration (Your PC's IP)
  const char* MQTT_BROKER = "192.168.1.100";  // Replace with your PC's IP!
  const int MQTT_PORT = 1883;
  
  // No username/password for local Mosquitto (anonymous mode)
  // const char* MQTT_USER = "";
  // const char* MQTT_PASSWORD = "";
  
  // Device Identity
  const char* DEVICE_ID = "car-001";
  
  // MQTT Topics
  const char* TOPIC_TELEMETRY = "iot-car/car-001/telemetry";
  const char* TOPIC_COMMAND = "iot-car/car-001/command";
  const char* TOPIC_STATUS = "iot-car/car-001/status";
  
  #endif
  ```

- [ ] **Create `src/main.cpp`:**
  ```cpp
  #include <Arduino.h>
  #include <WiFi.h>
  #include <PubSubClient.h>
  #include <ArduinoJson.h>
  #include "config.h"
  
  WiFiClient espClient;
  PubSubClient mqttClient(espClient);
  
  unsigned long lastTelemetry = 0;
  const long telemetryInterval = 1000;  // Send every 1 second
  
  // Function declarations
  void connectWiFi();
  void connectMQTT();
  void onMqttMessage(char* topic, byte* payload, unsigned int length);
  void sendTelemetry();
  void publishStatus(const char* status);
  
  void setup() {
    Serial.begin(115200);
    delay(1000);
    
    Serial.println("IoT Car Starting...");
    
    // Connect WiFi
    connectWiFi();
    
    // Configure MQTT (no TLS for local Mosquitto)
    mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
    mqttClient.setCallback(onMqttMessage);
    
    // Connect MQTT
    connectMQTT();
  }
  
  void loop() {
    // Ensure connections
    if (!mqttClient.connected()) {
      connectMQTT();
    }
    mqttClient.loop();
    
    // Send telemetry periodically
    if (millis() - lastTelemetry > telemetryInterval) {
      sendTelemetry();
      lastTelemetry = millis();
    }
  }
  
  void connectWiFi() {
    Serial.print("Connecting to WiFi");
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    
    while (WiFi.status() != WL_CONNECTED) {
      delay(500);
      Serial.print(".");
    }
    
    Serial.println();
    Serial.print("Connected! IP: ");
    Serial.println(WiFi.localIP());
  }
  
  void connectMQTT() {
    while (!mqttClient.connected()) {
      Serial.print("Connecting to MQTT...");
      
      String clientId = "ESP32-" + String(DEVICE_ID);
      
      // No username/password for anonymous Mosquitto
      if (mqttClient.connect(clientId.c_str())) {
        Serial.println("connected!");
        
        // Subscribe to commands
        mqttClient.subscribe(TOPIC_COMMAND);
        Serial.print("Subscribed to: ");
        Serial.println(TOPIC_COMMAND);
        
        // Publish online status
        publishStatus("online");
        
      } else {
        Serial.print("failed, rc=");
        Serial.print(mqttClient.state());
        Serial.println(" retrying in 5 seconds...");
        delay(5000);
      }
    }
  }
  
  void onMqttMessage(char* topic, byte* payload, unsigned int length) {
    Serial.print("Message received on ");
    Serial.print(topic);
    Serial.print(": ");
    
    // Convert payload to string
    String message;
    for (unsigned int i = 0; i < length; i++) {
      message += (char)payload[i];
    }
    Serial.println(message);
    
    // Parse JSON command (for later)
    // StaticJsonDocument<256> doc;
    // deserializeJson(doc, message);
  }
  
  void sendTelemetry() {
    StaticJsonDocument<256> doc;
    
    doc["device_id"] = DEVICE_ID;
    doc["timestamp"] = millis();
    doc["battery"] = 100;  // Placeholder
    doc["distance_front"] = 0;  // Placeholder
    
    String output;
    serializeJson(doc, output);
    
    mqttClient.publish(TOPIC_TELEMETRY, output.c_str());
    Serial.print("Telemetry sent: ");
    Serial.println(output);
  }
  
  void publishStatus(const char* status) {
    StaticJsonDocument<128> doc;
    doc["device_id"] = DEVICE_ID;
    doc["status"] = status;
    
    String output;
    serializeJson(doc, output);
    
    mqttClient.publish(TOPIC_STATUS, output.c_str());
  }
  ```

- [ ] **Update `include/config.h`** with YOUR WiFi and PC IP address

- [ ] **Build and Upload:**
  1. Connect ESP32 via USB
  2. Click PlatformIO icon → `Build` (checkmark icon)
  3. Click `Upload` (arrow icon)
  4. Click `Serial Monitor` (plug icon)

- [ ] **Verify output shows:**
  ```
  IoT Car Starting...
  Connecting to WiFi....
  Connected! IP: 192.168.1.xxx
  Connecting to MQTT...connected!
  Subscribed to: iot-car/car-001/command
  Telemetry sent: {"device_id":"car-001","timestamp":1234,"battery":100,"distance_front":0}
  ```

- [ ] **Verify in Terminal 1** (mosquitto_sub):
  ```
  mosquitto_sub -h localhost -t "iot-car/#" -v
  ```
  You should see telemetry messages appearing!

### Step 1.3: Go Backend MQTT Subscriber (1-2 hours)

- [ ] Initialize Go module:
  ```bash
  cd backend
  go mod init iot-car-backend
  ```

- [ ] Create `backend/cmd/server/main.go`:
  ```go
  package main

  import (
      "encoding/json"
      "fmt"
      "log"
      "os"
      "os/signal"
      "syscall"

      mqtt "github.com/eclipse/paho.mqtt.golang"
  )

  // Config holds MQTT configuration
  type Config struct {
      BrokerIP string
      Port     int
  }

  // Telemetry represents incoming telemetry data
  type Telemetry struct {
      DeviceID      string `json:"device_id"`
      Timestamp     int64  `json:"timestamp"`
      Battery       int    `json:"battery"`
      DistanceFront int    `json:"distance_front"`
  }

  // Update this with your PC's IP if running backend on different machine
  var config = Config{
      BrokerIP: "localhost",  // Use "localhost" if backend runs on same PC as Mosquitto
      Port:     1883,
  }

  func main() {
      log.Println("IoT Car Backend Starting...")

      // Create MQTT client options
      opts := mqtt.NewClientOptions()
      opts.AddBroker(fmt.Sprintf("tcp://%s:%d", config.BrokerIP, config.Port))
      opts.SetClientID("go-backend-001")
      // No username/password for anonymous Mosquitto

      // Connection handlers
      opts.SetOnConnectHandler(onConnect)
      opts.SetConnectionLostHandler(onConnectionLost)
      opts.SetDefaultPublishHandler(onMessage)

      // Create and connect client
      client := mqtt.NewClient(opts)
      if token := client.Connect(); token.Wait() && token.Error() != nil {
          log.Fatalf("Failed to connect: %v", token.Error())
      }

      log.Println("Connected to Mosquitto broker!")

      // Subscribe to all car telemetry
      topic := "iot-car/+/telemetry"
      if token := client.Subscribe(topic, 1, nil); token.Wait() && token.Error() != nil {
          log.Fatalf("Failed to subscribe: %v", token.Error())
      }
      log.Printf("Subscribed to: %s", topic)

      // Subscribe to status updates
      statusTopic := "iot-car/+/status"
      if token := client.Subscribe(statusTopic, 1, nil); token.Wait() && token.Error() != nil {
          log.Fatalf("Failed to subscribe: %v", token.Error())
      }
      log.Printf("Subscribed to: %s", statusTopic)

      // Wait for interrupt signal
      sigChan := make(chan os.Signal, 1)
      signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
      <-sigChan

      log.Println("Shutting down...")
      client.Disconnect(250)
  }

  func onConnect(client mqtt.Client) {
      log.Println("MQTT Connected!")
  }

  func onConnectionLost(client mqtt.Client, err error) {
      log.Printf("Connection lost: %v", err)
  }

  func onMessage(client mqtt.Client, msg mqtt.Message) {
      log.Printf("Received on [%s]: %s", msg.Topic(), string(msg.Payload()))

      // Try to parse as telemetry
      var telemetry Telemetry
      if err := json.Unmarshal(msg.Payload(), &telemetry); err == nil {
          log.Printf("  → Device: %s, Battery: %d%%, Distance: %dcm",
              telemetry.DeviceID, telemetry.Battery, telemetry.DistanceFront)
      }
  }
  ```

- [ ] Install dependencies:
  ```bash
  cd backend
  go mod tidy
  ```

- [ ] Update credentials in `main.go`

- [ ] Run the backend:
  ```bash
  go run cmd/server/main.go
  ```

- [ ] Verify output shows ESP32 telemetry:
  ```
  IoT Car Backend Starting...
  Connected to Mosquitto broker!
  Subscribed to: iot-car/+/telemetry
  Subscribed to: iot-car/+/status
  Received on [iot-car/car-001/status]: {"device_id":"car-001","status":"online"}
  Received on [iot-car/car-001/telemetry]: {"device_id":"car-001","timestamp":1234,"battery":100,"distance_front":0}
    → Device: car-001, Battery: 100%, Distance: 0cm
  ```

### ✅ Phase 1 Checkpoint

Before proceeding, verify:
- [ ] ESP32 connects to local Mosquitto broker
- [ ] ESP32 sends telemetry every 1 second
- [ ] Go backend receives and logs telemetry
- [ ] You can see messages in `mosquitto_sub`

---

## 🔧 Phase 2: Hardware Integration (Days 4-6)

> **Goal:** Car can move and sense obstacles. No app control yet.

### Step 2.1: Wire the Hardware (1-2 hours)

**Components Needed:**
- ESP32 DevKit
- L298N Motor Driver
- 2x DC Motors (or 4x for 4WD)
- HC-SR04 Ultrasonic Sensor
- Car chassis
- 2x 18650 batteries (or similar 7-12V source)

**Wiring Diagram:**

```
                    ┌─────────────────────┐
                    │       L298N         │
                    │   Motor Driver      │
    ┌───────────────┤                     ├───────────────┐
    │               │  IN1  IN2  IN3  IN4 │               │
    │               │   │    │    │    │  │               │
    │  ┌────────────┼───┼────┼────┼────┼──┼────────────┐  │
    │  │            │   │    │    │    │  │            │  │
    │  │         GPIO25 GPIO26 GPIO27 GPIO14           │  │
    │  │            │          │                       │  │
    │  │            └─ESP32────┘                       │  │
    │  │              │    │                           │  │
    │  │          GPIO32 GPIO33                        │  │
    │  │              │    │                           │  │
    │  │              │    │    ┌──────────────┐       │  │
    │  │              │    └────┤  HC-SR04     │       │  │
    │  │              └─────────┤  Ultrasonic  │       │  │
    │  │                   TRIG─┤              │──ECHO │  │
    │  │                        └──────────────┘       │  │
    │  │                                               │  │
    ▼  ▼                                               ▼  ▼
  Motor A                                           Motor B
  (Left)                                            (Right)


ESP32 Connections:
├── GPIO25 → L298N IN1 (Motor A direction)
├── GPIO26 → L298N IN2 (Motor A direction)
├── GPIO27 → L298N IN3 (Motor B direction)
├── GPIO14 → L298N IN4 (Motor B direction)
├── GPIO32 → HC-SR04 TRIGGER
├── GPIO33 → HC-SR04 ECHO
├── 5V     → HC-SR04 VCC
├── GND    → HC-SR04 GND, L298N GND
└── VIN    ← L298N 5V output

L298N Connections:
├── 12V    ← Battery positive (7-12V)
├── GND    ← Battery negative
├── 5V     → ESP32 VIN (if jumper present)
├── OUT1,2 → Motor A
└── OUT3,4 → Motor B
```

- [ ] Wire ESP32 to L298N (4 control wires)
- [ ] Wire motors to L298N outputs
- [ ] Wire HC-SR04 to ESP32
- [ ] Connect power supply
- [ ] Double-check all connections!

### Step 2.2: Add Motor Control Code (1 hour)

- [ ] Create `firmware/iot_car_esp32/motor_control.h`:
  ```cpp
  #ifndef MOTOR_CONTROL_H
  #define MOTOR_CONTROL_H
  
  // Motor A (Left) pins
  #define MOTOR_A_IN1 25
  #define MOTOR_A_IN2 26
  
  // Motor B (Right) pins
  #define MOTOR_B_IN3 27
  #define MOTOR_B_IN4 14
  
  void setupMotors() {
    pinMode(MOTOR_A_IN1, OUTPUT);
    pinMode(MOTOR_A_IN2, OUTPUT);
    pinMode(MOTOR_B_IN3, OUTPUT);
    pinMode(MOTOR_B_IN4, OUTPUT);
    
    stopMotors();  // Ensure motors are stopped
  }
  
  void stopMotors() {
    digitalWrite(MOTOR_A_IN1, LOW);
    digitalWrite(MOTOR_A_IN2, LOW);
    digitalWrite(MOTOR_B_IN3, LOW);
    digitalWrite(MOTOR_B_IN4, LOW);
    Serial.println("Motors: STOP");
  }
  
  void moveForward() {
    digitalWrite(MOTOR_A_IN1, HIGH);
    digitalWrite(MOTOR_A_IN2, LOW);
    digitalWrite(MOTOR_B_IN3, HIGH);
    digitalWrite(MOTOR_B_IN4, LOW);
    Serial.println("Motors: FORWARD");
  }
  
  void moveBackward() {
    digitalWrite(MOTOR_A_IN1, LOW);
    digitalWrite(MOTOR_A_IN2, HIGH);
    digitalWrite(MOTOR_B_IN3, LOW);
    digitalWrite(MOTOR_B_IN4, HIGH);
    Serial.println("Motors: BACKWARD");
  }
  
  void turnLeft() {
    digitalWrite(MOTOR_A_IN1, LOW);
    digitalWrite(MOTOR_A_IN2, HIGH);
    digitalWrite(MOTOR_B_IN3, HIGH);
    digitalWrite(MOTOR_B_IN4, LOW);
    Serial.println("Motors: LEFT");
  }
  
  void turnRight() {
    digitalWrite(MOTOR_A_IN1, HIGH);
    digitalWrite(MOTOR_A_IN2, LOW);
    digitalWrite(MOTOR_B_IN3, LOW);
    digitalWrite(MOTOR_B_IN4, HIGH);
    Serial.println("Motors: RIGHT");
  }
  
  #endif
  ```

- [ ] Test motors:
  ```cpp
  // Add to setup() temporarily
  setupMotors();
  
  moveForward();
  delay(2000);
  stopMotors();
  delay(1000);
  
  moveBackward();
  delay(2000);
  stopMotors();
  ```

- [ ] Verify all 4 movements work (forward, backward, left, right)

### Step 2.3: Add Ultrasonic Sensor (30 min)

- [ ] Create `firmware/iot_car_esp32/sensors.h`:
  ```cpp
  #ifndef SENSORS_H
  #define SENSORS_H
  
  #define TRIG_PIN 32
  #define ECHO_PIN 33
  
  void setupSensors() {
    pinMode(TRIG_PIN, OUTPUT);
    pinMode(ECHO_PIN, INPUT);
  }
  
  // Returns distance in cm
  int getDistanceCm() {
    // Clear trigger
    digitalWrite(TRIG_PIN, LOW);
    delayMicroseconds(2);
    
    // Send 10µs pulse
    digitalWrite(TRIG_PIN, HIGH);
    delayMicroseconds(10);
    digitalWrite(TRIG_PIN, LOW);
    
    // Read echo pulse duration
    long duration = pulseIn(ECHO_PIN, HIGH, 30000);  // 30ms timeout
    
    // Calculate distance: duration/2 * speed of sound (0.0343 cm/µs)
    int distance = duration * 0.0343 / 2;
    
    // Return max 400cm (sensor limit)
    return (distance > 400 || distance <= 0) ? 400 : distance;
  }
  
  #endif
  ```

- [ ] Update main sketch to use sensors:
  ```cpp
  #include "sensors.h"
  
  // In setup():
  setupSensors();
  
  // In sendTelemetry():
  doc["distance_front"] = getDistanceCm();
  ```

- [ ] Test sensor readings in Serial Monitor

### Step 2.4: Integrate Motors with MQTT Commands (1-2 hours)

- [ ] Update the `onMqttMessage` function:
  ```cpp
  void onMqttMessage(char* topic, byte* payload, unsigned int length) {
    // Convert payload to string
    String message;
    for (unsigned int i = 0; i < length; i++) {
      message += (char)payload[i];
    }
    
    Serial.print("Command received: ");
    Serial.println(message);
    
    // Parse JSON
    StaticJsonDocument<256> doc;
    DeserializationError error = deserializeJson(doc, message);
    
    if (error) {
      Serial.print("JSON parse error: ");
      Serial.println(error.c_str());
      return;
    }
    
    // Handle command
    const char* action = doc["action"];
    const char* direction = doc["direction"];
    int duration = doc["duration"] | 0;  // Default 0 = continuous
    
    if (strcmp(action, "move") == 0) {
      if (strcmp(direction, "forward") == 0) {
        moveForward();
      } else if (strcmp(direction, "backward") == 0) {
        moveBackward();
      } else if (strcmp(direction, "left") == 0) {
        turnLeft();
      } else if (strcmp(direction, "right") == 0) {
        turnRight();
      }
      
      // If duration specified, stop after delay
      if (duration > 0) {
        delay(duration);  // For now; will improve later
        stopMotors();
      }
    } else if (strcmp(action, "stop") == 0) {
      stopMotors();
    }
    
    // Send acknowledgment
    sendCommandAck(doc["command_id"] | "unknown");
  }
  
  void sendCommandAck(const char* commandId) {
    StaticJsonDocument<128> doc;
    doc["device_id"] = DEVICE_ID;
    doc["command_id"] = commandId;
    doc["status"] = "executed";
    
    String output;
    serializeJson(doc, output);
    
    String ackTopic = String("iot-car/") + DEVICE_ID + "/response";
    mqttClient.publish(ackTopic.c_str(), output.c_str());
  }
  ```

- [ ] Test with Mosquitto CLI:
  1. In a terminal, subscribe to responses:
     ```powershell
     mosquitto_sub -h localhost -t "iot-car/car-001/response" -v
     ```
  2. In another terminal, publish a command:
     ```powershell
     mosquitto_pub -h localhost -t "iot-car/car-001/command" -m '{"action":"move","direction":"forward","duration":2000,"command_id":"test-1"}'
     ```
  3. Car should move forward for 2 seconds
  4. Check for acknowledgment in the subscribe terminal

### ✅ Phase 2 Checkpoint

Before proceeding, verify:
- [ ] Car moves in all 4 directions
- [ ] Distance sensor readings appear in telemetry
- [ ] Commands from MQTT control the car
- [ ] Command acknowledgments are sent

---

## 📱 Phase 3: Android App (Days 7-10)

> **Goal:** Control the car from your phone!

### Step 3.1: Create Android Project (30 min)

- [ ] Open Android Studio
- [ ] New Project → Empty Activity
  - Name: IoTCarController
  - Package: com.example.iotcarcontroller
  - Language: Java
  - Minimum SDK: API 24 (Android 7.0)
- [ ] Save to `android-app/IoTCarController/`

### Step 3.2: Add Dependencies (15 min)

- [ ] Open `app/build.gradle` and add:
  ```gradle
  dependencies {
      // Existing dependencies...
      
      // MQTT Client
      implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5'
      implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'
      
      // JSON parsing
      implementation 'com.google.code.gson:gson:2.10.1'
  }
  ```

- [ ] Open `app/build.gradle` and add to `android` block:
  ```gradle
  android {
      // Existing config...
      
      packagingOptions {
          exclude 'META-INF/INDEX.LIST'
          exclude 'META-INF/io.netty.versions.properties'
      }
  }
  ```

- [ ] Sync Gradle

### Step 3.3: Add Permissions (5 min)

- [ ] Open `AndroidManifest.xml` and add:
  ```xml
  <manifest ...>
      <uses-permission android:name="android.permission.INTERNET" />
      <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
      <uses-permission android:name="android.permission.WAKE_LOCK" />
      
      <application ...>
          <!-- Add MQTT Service -->
          <service android:name="org.eclipse.paho.android.service.MqttService" />
      </application>
  </manifest>
  ```

### Step 3.4: Create MQTT Manager Class (1 hour)

- [ ] Create `app/src/main/java/.../MqttManager.java`:
  ```java
  package com.example.iotcarcontroller;

  import android.content.Context;
  import android.util.Log;

  import org.eclipse.paho.android.service.MqttAndroidClient;
  import org.eclipse.paho.client.mqttv3.*;
  import org.json.JSONObject;

  public class MqttManager {
      private static final String TAG = "MqttManager";
      
      // Mosquitto broker config - UPDATE WITH YOUR PC's IP!
      private static final String BROKER_URL = "tcp://192.168.1.100:1883";
      private static final String CLIENT_ID = "android-app-" + System.currentTimeMillis();
      
      private static final String DEVICE_ID = "car-001";
      
      private MqttAndroidClient mqttClient;
      private MqttCallback listener;
      
      public interface MqttCallback {
          void onConnected();
          void onDisconnected();
          void onTelemetryReceived(int battery, int distance);
          void onError(String message);
      }
      
      public MqttManager(Context context, MqttCallback listener) {
          this.listener = listener;
          this.mqttClient = new MqttAndroidClient(context, BROKER_URL, CLIENT_ID);
          
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
              public void deliveryComplete(IMqttDeliveryToken token) {}
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
          } catch (MqttException e) {
              Log.e(TAG, "Connect error", e);
              listener.onError("Connect error: " + e.getMessage());
          }
      }
      
      private void subscribeToTopics() {
          try {
              String telemetryTopic = "iot-car/" + DEVICE_ID + "/telemetry";
              mqttClient.subscribe(telemetryTopic, 1);
              Log.d(TAG, "Subscribed to: " + telemetryTopic);
          } catch (MqttException e) {
              Log.e(TAG, "Subscribe error", e);
          }
      }
      
      private void handleMessage(String topic, String payload) {
          Log.d(TAG, "Message: " + topic + " -> " + payload);
          
          try {
              JSONObject json = new JSONObject(payload);
              
              if (topic.contains("/telemetry")) {
                  int battery = json.optInt("battery", 0);
                  int distance = json.optInt("distance_front", 0);
                  listener.onTelemetryReceived(battery, distance);
              }
          } catch (Exception e) {
              Log.e(TAG, "Parse error", e);
          }
      }
      
      public void sendCommand(String action, String direction, int duration) {
          try {
              JSONObject command = new JSONObject();
              command.put("action", action);
              command.put("direction", direction);
              command.put("duration", duration);
              command.put("command_id", "cmd-" + System.currentTimeMillis());
              
              String topic = "iot-car/" + DEVICE_ID + "/command";
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
          } catch (MqttException e) {
              Log.e(TAG, "Disconnect error", e);
          }
      }
  }
  ```

### Step 3.5: Create Control UI Layout (1 hour)

- [ ] Replace `activity_main.xml`:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <LinearLayout 
      xmlns:android="http://schemas.android.com/apk/res/android"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:orientation="vertical"
      android:padding="16dp"
      android:background="#1a1a2e">
      
      <!-- Status Bar -->
      <LinearLayout
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:orientation="horizontal"
          android:gravity="center"
          android:padding="16dp">
          
          <View
              android:id="@+id/connectionIndicator"
              android:layout_width="16dp"
              android:layout_height="16dp"
              android:background="@drawable/circle_red" />
          
          <TextView
              android:id="@+id/connectionStatus"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="Disconnected"
              android:textColor="#ffffff"
              android:textSize="18sp"
              android:layout_marginStart="8dp" />
      </LinearLayout>
      
      <!-- Telemetry Display -->
      <LinearLayout
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:orientation="horizontal"
          android:gravity="center"
          android:padding="16dp">
          
          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="🔋 Battery: "
              android:textColor="#ffffff"
              android:textSize="16sp" />
          
          <TextView
              android:id="@+id/batteryText"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="---%"
              android:textColor="#00ff88"
              android:textSize="16sp" />
          
          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="    📏 Distance: "
              android:textColor="#ffffff"
              android:textSize="16sp" />
          
          <TextView
              android:id="@+id/distanceText"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="---cm"
              android:textColor="#00ff88"
              android:textSize="16sp" />
      </LinearLayout>
      
      <!-- Control Pad -->
      <LinearLayout
          android:layout_width="match_parent"
          android:layout_height="0dp"
          android:layout_weight="1"
          android:orientation="vertical"
          android:gravity="center">
          
          <!-- Forward Button -->
          <Button
              android:id="@+id/btnForward"
              android:layout_width="100dp"
              android:layout_height="100dp"
              android:text="▲"
              android:textSize="32sp"
              android:backgroundTint="#4a4e69" />
          
          <!-- Left / Stop / Right -->
          <LinearLayout
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:orientation="horizontal"
              android:layout_marginTop="8dp">
              
              <Button
                  android:id="@+id/btnLeft"
                  android:layout_width="100dp"
                  android:layout_height="100dp"
                  android:text="◀"
                  android:textSize="32sp"
                  android:backgroundTint="#4a4e69" />
              
              <Button
                  android:id="@+id/btnStop"
                  android:layout_width="100dp"
                  android:layout_height="100dp"
                  android:text="⏹"
                  android:textSize="32sp"
                  android:backgroundTint="#e63946"
                  android:layout_marginStart="8dp"
                  android:layout_marginEnd="8dp" />
              
              <Button
                  android:id="@+id/btnRight"
                  android:layout_width="100dp"
                  android:layout_height="100dp"
                  android:text="▶"
                  android:textSize="32sp"
                  android:backgroundTint="#4a4e69" />
          </LinearLayout>
          
          <!-- Backward Button -->
          <Button
              android:id="@+id/btnBackward"
              android:layout_width="100dp"
              android:layout_height="100dp"
              android:text="▼"
              android:textSize="32sp"
              android:backgroundTint="#4a4e69"
              android:layout_marginTop="8dp" />
      </LinearLayout>
      
      <!-- Connect Button -->
      <Button
          android:id="@+id/btnConnect"
          android:layout_width="match_parent"
          android:layout_height="60dp"
          android:text="Connect"
          android:textSize="18sp"
          android:backgroundTint="#2d6a4f" />
          
  </LinearLayout>
  ```

- [ ] Create `res/drawable/circle_red.xml`:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <shape xmlns:android="http://schemas.android.com/apk/res/android"
      android:shape="oval">
      <solid android:color="#e63946" />
  </shape>
  ```

- [ ] Create `res/drawable/circle_green.xml`:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <shape xmlns:android="http://schemas.android.com/apk/res/android"
      android:shape="oval">
      <solid android:color="#2d6a4f" />
  </shape>
  ```

### Step 3.6: Implement MainActivity (1 hour)

- [ ] Replace `MainActivity.java`:
  ```java
  package com.example.iotcarcontroller;

  import android.os.Bundle;
  import android.view.MotionEvent;
  import android.view.View;
  import android.widget.Button;
  import android.widget.TextView;
  import androidx.appcompat.app.AppCompatActivity;

  public class MainActivity extends AppCompatActivity implements MqttManager.MqttCallback {
      
      private MqttManager mqttManager;
      
      private View connectionIndicator;
      private TextView connectionStatus;
      private TextView batteryText;
      private TextView distanceText;
      private Button btnConnect;
      
      private boolean isConnected = false;
      
      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);
          
          // Initialize views
          connectionIndicator = findViewById(R.id.connectionIndicator);
          connectionStatus = findViewById(R.id.connectionStatus);
          batteryText = findViewById(R.id.batteryText);
          distanceText = findViewById(R.id.distanceText);
          btnConnect = findViewById(R.id.btnConnect);
          
          // Initialize MQTT
          mqttManager = new MqttManager(this, this);
          
          // Connect button
          btnConnect.setOnClickListener(v -> {
              if (!isConnected) {
                  mqttManager.connect();
                  btnConnect.setText("Connecting...");
              } else {
                  mqttManager.disconnect();
                  onDisconnected();
              }
          });
          
          // Control buttons with touch hold
          setupControlButton(R.id.btnForward, "move", "forward");
          setupControlButton(R.id.btnBackward, "move", "backward");
          setupControlButton(R.id.btnLeft, "move", "left");
          setupControlButton(R.id.btnRight, "move", "right");
          
          // Stop button
          findViewById(R.id.btnStop).setOnClickListener(v -> {
              mqttManager.sendCommand("stop", "", 0);
          });
      }
      
      private void setupControlButton(int buttonId, String action, String direction) {
          Button button = findViewById(buttonId);
          
          button.setOnTouchListener((v, event) -> {
              switch (event.getAction()) {
                  case MotionEvent.ACTION_DOWN:
                      // Start moving (duration 0 = continuous)
                      mqttManager.sendCommand(action, direction, 0);
                      break;
                      
                  case MotionEvent.ACTION_UP:
                  case MotionEvent.ACTION_CANCEL:
                      // Stop when released
                      mqttManager.sendCommand("stop", "", 0);
                      break;
              }
              return false;  // Allow click events too
          });
      }
      
      @Override
      public void onConnected() {
          runOnUiThread(() -> {
              isConnected = true;
              connectionIndicator.setBackgroundResource(R.drawable.circle_green);
              connectionStatus.setText("Connected");
              btnConnect.setText("Disconnect");
          });
      }
      
      @Override
      public void onDisconnected() {
          runOnUiThread(() -> {
              isConnected = false;
              connectionIndicator.setBackgroundResource(R.drawable.circle_red);
              connectionStatus.setText("Disconnected");
              btnConnect.setText("Connect");
              batteryText.setText("---%");
              distanceText.setText("---cm");
          });
      }
      
      @Override
      public void onTelemetryReceived(int battery, int distance) {
          runOnUiThread(() -> {
              batteryText.setText(battery + "%");
              distanceText.setText(distance + "cm");
          });
      }
      
      @Override
      public void onError(String message) {
          runOnUiThread(() -> {
              connectionStatus.setText("Error: " + message);
              btnConnect.setText("Connect");
          });
      }
      
      @Override
      protected void onDestroy() {
          super.onDestroy();
          mqttManager.disconnect();
      }
  }
  ```

### Step 3.7: Test the App (30 min)

- [ ] Update MQTT credentials in `MqttManager.java`
- [ ] Run the app on your Android phone (USB debugging)
- [ ] Press Connect
- [ ] Verify:
  - [ ] Connection indicator turns green
  - [ ] Battery and distance values update
  - [ ] Control buttons move the car
  - [ ] Car stops when you release the button

### ✅ Phase 3 Checkpoint

Before proceeding, verify:
- [ ] App connects to HiveMQ Cloud
- [ ] Telemetry displays in real-time
- [ ] All 4 direction buttons work
- [ ] Stop button works
- [ ] Car stops when button is released

---

## 🛡️ Phase 4: Polish & Safety (Days 11-12)

> **Goal:** Make it reliable and safe.

### Step 4.1: Add Obstacle Auto-Stop (30 min)

- [ ] Update ESP32 main loop:
  ```cpp
  #define OBSTACLE_DISTANCE_CM 30  // Stop if closer than 30cm
  
  bool obstacleDetected = false;
  bool currentlyMovingForward = false;
  
  void loop() {
    if (!mqttClient.connected()) {
      connectMQTT();
    }
    mqttClient.loop();
    
    // Check for obstacles
    int distance = getDistanceCm();
    
    if (distance < OBSTACLE_DISTANCE_CM && currentlyMovingForward) {
      stopMotors();
      obstacleDetected = true;
      
      // Notify via MQTT
      StaticJsonDocument<128> doc;
      doc["device_id"] = DEVICE_ID;
      doc["event"] = "obstacle_detected";
      doc["distance"] = distance;
      
      String output;
      serializeJson(doc, output);
      mqttClient.publish("iot-car/car-001/event", output.c_str());
    }
    
    // Send telemetry periodically
    if (millis() - lastTelemetry > telemetryInterval) {
      sendTelemetry();
      lastTelemetry = millis();
    }
  }
  
  // Update moveForward to track state
  void moveForward() {
    if (!obstacleDetected) {
      digitalWrite(MOTOR_A_IN1, HIGH);
      // ... rest of motor code
      currentlyMovingForward = true;
    }
  }
  ```

### Step 4.2: Add Connection Watchdog (30 min)

- [ ] Add to ESP32 to stop if MQTT disconnects:
  ```cpp
  unsigned long lastMqttActivity = 0;
  #define MQTT_TIMEOUT_MS 5000  // 5 seconds
  
  void loop() {
    // ... existing code ...
    
    if (mqttClient.connected()) {
      lastMqttActivity = millis();
    } else {
      // If disconnected for too long, stop for safety
      if (millis() - lastMqttActivity > MQTT_TIMEOUT_MS) {
        stopMotors();
      }
    }
  }
  ```

### Step 4.3: Add Reconnection Logic (30 min)

Already covered in `connectMQTT()` but improve:

- [ ] Add exponential backoff:
  ```cpp
  int reconnectAttempts = 0;
  
  void connectMQTT() {
    while (!mqttClient.connected()) {
      // ... existing connect code ...
      
      if (!mqttClient.connect(...)) {
        reconnectAttempts++;
        int delayTime = min(30000, 1000 * (1 << reconnectAttempts));  // Max 30s
        Serial.printf("Retry in %d ms...\n", delayTime);
        delay(delayTime);
      } else {
        reconnectAttempts = 0;  // Reset on success
      }
    }
  }
  ```

### ✅ Phase 4 Checkpoint

Before proceeding, verify:
- [ ] Car stops automatically near obstacles
- [ ] Car stops if MQTT disconnects
- [ ] Reconnection works reliably

---

## 🎉 Phase 5: Final Testing & Demo (Days 13-14)

> **Goal:** Create a polished demo for your portfolio.

### Step 5.1: End-to-End Testing Checklist

- [ ] **Connection Tests**
  - [ ] Car connects on power-up
  - [ ] App connects to car
  - [ ] Reconnects after WiFi loss
  - [ ] Reconnects after broker restart

- [ ] **Control Tests**
  - [ ] Forward movement works
  - [ ] Backward movement works
  - [ ] Left turn works
  - [ ] Right turn works
  - [ ] Stop command works
  - [ ] Button release stops car

- [ ] **Safety Tests**
  - [ ] Obstacle detection stops car
  - [ ] Disconnect detection stops car

- [ ] **Telemetry Tests**
  - [ ] Distance readings are accurate
  - [ ] Data updates in app

### Step 5.2: Record Demo Video (1 hour)

- [ ] Setup: Clean area, good lighting
- [ ] Record scenes:
  1. App connection (screen record)
  2. Remote control in action
  3. Obstacle avoidance demo
  4. Telemetry display updating
- [ ] Edit into 2-3 minute demo

### Step 5.3: Create README (1 hour)

- [ ] Create `README.md` in project root with:
  - Project overview
  - Architecture diagram
  - Features list
  - Setup instructions
  - Screenshots/GIFs
  - Future enhancements

---

## 📊 Progress Tracker

Use this to track your progress:

```
Phase 0: Environment Setup     [░░░░░░░░░░] 0%
Phase 1: MQTT Foundation       [░░░░░░░░░░] 0%
Phase 2: Hardware Integration  [░░░░░░░░░░] 0%
Phase 3: Android App           [░░░░░░░░░░] 0%
Phase 4: Polish & Safety       [░░░░░░░░░░] 0%
Phase 5: Testing & Demo        [░░░░░░░░░░] 0%

Overall Progress: 0%
```

---

## 🆘 Troubleshooting Quick Reference

| Problem | Likely Cause | Solution |
|---------|--------------|----------|
| ESP32 won't connect to WiFi | Wrong credentials | Double-check SSID/password |
| MQTT connection fails | Wrong port or no TLS | Use port 8883 with `WiFiClientSecure` |
| Motors don't move | Wiring issue | Check L298N connections |
| Motors move wrong direction | Swap motor wires | Swap OUT1/OUT2 or IN1/IN2 |
| Ultrasonic reads 0 | Wrong pins | Verify TRIG/ECHO wiring |
| App doesn't connect | Firewall or credentials | Check HiveMQ user permissions |
| Commands not received | Topic mismatch | Print topics on both sides |

---

## 💡 Tips for Staying on Track

1. **Do ONE step at a time** - Don't skip ahead
2. **Test after each step** - Catch problems early
3. **Use the checkboxes** - Visual progress motivation!
4. **Take breaks** - Frustration = mistakes
5. **Ask for help** - Don't get stuck for days

---

> **You've got this! 🚗💨**
> 
> Remember: A working MVP with 7 features beats a broken project trying to do 20 things.
