# 🎮 Wokwi ESP32 Simulation Guide

> **Goal:** Simulate your IoT Car ESP32 firmware without physical hardware, while connecting to real MQTT broker and controlling via your Android app.
> **Time Required:** ~30 minutes setup

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Step 1: Setup Mosquitto MQTT Broker with Docker](#step-1-setup-mosquitto-mqtt-broker-with-docker)
4. [Step 2: Install Wokwi Extension](#step-2-install-wokwi-vs-code-extension)
5. [Step 3: Configure PlatformIO Project](#step-3-configure-platformio-project-for-wokwi)
6. [Step 4: Create Virtual Hardware Diagram](#step-4-create-virtual-hardware-diagram)
7. [Step 5: Configure WiFi Gateway](#step-5-configure-wifi-gateway-for-mqtt)
8. [Step 6: Run Simulation](#step-6-run-the-simulation)
9. [Step 7: Connect Android App](#step-7-connect-android-app-to-virtual-car)
10. [Troubleshooting](#troubleshooting)

---

## Overview

### Architecture

```
┌────────────────────────────────────────────────────────────────────────────┐
│                           YOUR PC (Windows)                                │
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                    VS CODE + WOKWI SIMULATOR                         │  │
│  │                                                                      │  │
│  │   ┌────────────────────┐        ┌─────────────────────────────────┐  │  │
│  │   │  Virtual ESP32     │        │     Wokwi WiFi Gateway          │  │  │
│  │   │                    │        │                                 │  │  │
│  │   │  🔧 Your main.cpp  │◄──────►│  Virtual WiFi ──► Real Network  │  │  │
│  │   │  🔲 Virtual Motors │        │  (Bridges to localhost)         │  │  │
│  │   │  📏 Virtual HC-SR04│        │                                 │  │  │
│  │   └────────────────────┘        └─────────────────────────────────┘   │  │
│  │                                              │                        │  │
│  └──────────────────────────────────────────────┼────────────────────────┘  │
│                                                 │                           │
│                                                 ▼                           │
│                                    ┌─────────────────────┐                  │
│                                    │ 🐳 Docker Container │                  │
│                                    │   Mosquitto Broker  │                  │
│                                    │   localhost:1883    │                  │
│                                    └─────────────────────┘                  │
│                                                 ▲                           │
└─────────────────────────────────────────────────┼───────────────────────────┘
                                                  │ WiFi (Same Network)
                                                  │
                                    ┌─────────────┴───────────────┐
                                    │       Android Phone          │
                                    │    IoT Car Controller App    │
                                    │  Connects to <PC_IP>:1883    │
                                    └──────────────────────────────┘
```

### What Gets Simulated?

| Component | Real/Virtual | Description |
|-----------|--------------|-------------|
| ESP32 Microcontroller | 🔵 Virtual | Runs your actual `main.cpp` code |
| Motor Driver (L298N) | 🔵 Virtual | Animated motor outputs |
| Ultrasonic Sensor | 🔵 Virtual | Interactive distance simulation |
| WiFi Connection | 🔵 Virtual → 🟢 Real | Bridges to real network via Gateway |
| Mosquitto Broker | � Docker Container | Runs in isolated container on your PC |
| Go Backend | 🟢 Real | Receives telemetry |
| Android App | 🟢 Real | Controls virtual car |

---

## Prerequisites

### ✅ Checklist Before Starting

- [ ] **VS Code** installed
- [ ] **PlatformIO Extension** installed in VS Code
- [ ] **Docker Desktop** installed and running - https://www.docker.com/products/docker-desktop/
- [ ] **PlatformIO ESP32 Project** created (as per Implementation Roadmap Phase 1)
- [ ] **Wokwi Account** (free tier available) - https://wokwi.com

> **Why Docker?** Using Docker for Mosquitto provides:
> - 🚀 One-command setup (no installation wizards)
> - 🧹 Clean environment (no system-level changes)
> - 📦 Portable setup across Windows/Mac/Linux
> - 🔄 Easy start/stop/restart
> - 🏷️ Version pinning for consistency

---

## Step 1: Setup Mosquitto MQTT Broker with Docker

### 1.1 Verify Docker is Running

Open PowerShell and verify Docker is installed:

```powershell
docker --version
# Expected: Docker version 24.x.x or higher
```

### 1.2 Create Mosquitto Configuration Directory

Create a folder to store Mosquitto configuration:

```powershell
# Navigate to your project directory
cd "I:\My Drive\Project and docs\R & D\IoT MQTT Car"

# Create mqtt-broker folder structure
mkdir mqtt-broker
mkdir mqtt-broker\config
mkdir mqtt-broker\data
mkdir mqtt-broker\log
```

### 1.3 Create Mosquitto Configuration File

Create `mqtt-broker/config/mosquitto.conf`:

```conf
# Mosquitto Configuration for IoT Car Project
# ============================================

# Persistence settings
persistence true
persistence_location /mosquitto/data/

# Logging
log_dest file /mosquitto/log/mosquitto.log
log_dest stdout
log_type all

# Listener - Allow connections from any IP
listener 1883 0.0.0.0
allow_anonymous true

# WebSocket support (optional - for web clients)
listener 9001
protocol websockets
```

### 1.4 Create Docker Compose File

Create `mqtt-broker/docker-compose.yml`:

```yaml
version: '3.8'

services:
  mosquitto:
    image: eclipse-mosquitto:2.0
    container_name: iot-car-mqtt
    restart: unless-stopped
    ports:
      - "1883:1883"    # MQTT standard port
      - "9001:9001"    # WebSocket port (optional)
    volumes:
      - ./config:/mosquitto/config
      - ./data:/mosquitto/data
      - ./log:/mosquitto/log
    networks:
      - iot-network

networks:
  iot-network:
    driver: bridge
```

### 1.5 Start Mosquitto Container

```powershell
# Navigate to mqtt-broker directory
cd "I:\My Drive\Project and docs\R & D\IoT MQTT Car\mqtt-broker"

# Start Mosquitto in background
docker-compose up -d

# Verify container is running
docker ps
```

**Expected output:**
```
CONTAINER ID   IMAGE                   STATUS         PORTS                                        NAMES
abc123...      eclipse-mosquitto:2.0   Up 10 seconds  0.0.0.0:1883->1883/tcp, 0.0.0.0:9001->9001/tcp   iot-car-mqtt
```

### 1.6 Test MQTT Broker

Open two PowerShell terminals:

**Terminal 1 - Subscribe:**
```powershell
docker exec iot-car-mqtt mosquitto_sub -h localhost -t "test/#" -v
```

**Terminal 2 - Publish:**
```powershell
docker exec iot-car-mqtt mosquitto_pub -h localhost -t "test/hello" -m "Hello from Docker!"
```

You should see the message appear in Terminal 1!

### 1.7 Useful Docker Commands

| Command | Description |
|---------|-------------|
| `docker-compose up -d` | Start Mosquitto in background |
| `docker-compose down` | Stop and remove container |
| `docker-compose restart` | Restart Mosquitto |
| `docker logs iot-car-mqtt` | View Mosquitto logs |
| `docker exec -it iot-car-mqtt sh` | Open shell inside container |

### ✅ Checkpoint
- [ ] Docker Desktop installed and running
- [ ] Mosquitto configuration files created
- [ ] Mosquitto container running (`docker ps`)
- [ ] Test pub/sub working

---

## Step 2: Install Wokwi VS Code Extension

## Step 2: Install Wokwi VS Code Extension

### 2.1 Install the Extension

1. Open **VS Code**
2. Go to **Extensions** (Ctrl+Shift+X)
3. Search for **"Wokwi Simulator"**
4. Click **Install** on "Wokwi Simulator" by Wokwi

![Wokwi Extension](https://docs.wokwi.com/img/vscode/wokwi-extension.png)

### 2.2 Activate Wokwi License

1. Press **F1** → Type **"Wokwi: Request a new License"**
2. Click the link to open Wokwi website
3. Sign in with GitHub/Google
4. Copy the license key
5. Paste in VS Code when prompted

> **Note:** Free tier allows limited simulation time. For extended testing, consider a paid license.

### ✅ Checkpoint
- [ ] Wokwi extension installed
- [ ] License activated

---

## Step 3: Configure PlatformIO Project for Wokwi

### 3.1 Navigate to Your Firmware Project

```
IoT MQTT Car/
└── firmware/
    └── iot-car-esp32/          <── Your PlatformIO project
        ├── src/
        │   └── main.cpp
        ├── include/
        │   └── config.h
        ├── platformio.ini
        ├── wokwi.toml           <── CREATE THIS
        └── diagram.json         <── CREATE THIS
```

### 3.2 Create `wokwi.toml` Configuration

Create a new file `firmware/iot-car-esp32/wokwi.toml`:

```toml
[wokwi]
version = 1
firmware = ".pio/build/esp32dev/firmware.bin"
elf = ".pio/build/esp32dev/firmware.elf"

# Enable WiFi Gateway to connect to real network
[wokwi.network]
enabled = true

# Optional: Set custom gateway settings
# gateway_port = 9012
```

### 3.3 Update `platformio.ini` (Optional Optimization)

Add to your existing `platformio.ini`:

```ini
[env:esp32dev]
platform = espressif32
board = esp32dev
framework = arduino
monitor_speed = 115200

lib_deps =
    knolleary/PubSubClient@^2.8
    bblanchon/ArduinoJson@^6.21.3

; Wokwi optimization - faster builds
build_flags = -DCORE_DEBUG_LEVEL=0
```

### ✅ Checkpoint
- [ ] `wokwi.toml` created
- [ ] `platformio.ini` updated

---

## Step 4: Create Virtual Hardware Diagram

### 4.1 Create `diagram.json`

Create `firmware/iot-car-esp32/diagram.json`:

```json
{
  "version": 1,
  "author": "IoT Car Project",
  "editor": "wokwi",
  "parts": [
    {
      "type": "board-esp32-devkit-c-v4",
      "id": "esp",
      "top": 0,
      "left": 0,
      "attrs": {}
    },
    {
      "type": "wokwi-hc-sr04",
      "id": "ultrasonic",
      "top": -100,
      "left": 50,
      "attrs": {}
    },
    {
      "type": "wokwi-led",
      "id": "led-motor-a-fwd",
      "top": 150,
      "left": -80,
      "attrs": { "color": "green", "label": "Motor A FWD" }
    },
    {
      "type": "wokwi-led",
      "id": "led-motor-a-rev",
      "top": 180,
      "left": -80,
      "attrs": { "color": "red", "label": "Motor A REV" }
    },
    {
      "type": "wokwi-led",
      "id": "led-motor-b-fwd",
      "top": 150,
      "left": 250,
      "attrs": { "color": "green", "label": "Motor B FWD" }
    },
    {
      "type": "wokwi-led",
      "id": "led-motor-b-rev",
      "top": 180,
      "left": 250,
      "attrs": { "color": "red", "label": "Motor B REV" }
    },
    {
      "type": "wokwi-text",
      "id": "label1",
      "top": -130,
      "left": 40,
      "attrs": { "text": "Virtual IoT Car" }
    }
  ],
  "connections": [
    ["esp:GND.1", "ultrasonic:GND", "black", ["v-20", "h-30"]],
    ["esp:5V", "ultrasonic:VCC", "red", ["v-30", "h-30"]],
    ["esp:32", "ultrasonic:TRIG", "blue", ["v-40", "h0"]],
    ["esp:33", "ultrasonic:ECHO", "purple", ["v-50", "h0"]],
    
    ["esp:25", "led-motor-a-fwd:A", "green", ["h-50"]],
    ["esp:GND.2", "led-motor-a-fwd:C", "black", ["h-60"]],
    
    ["esp:26", "led-motor-a-rev:A", "orange", ["h-50"]],
    ["esp:GND.2", "led-motor-a-rev:C", "black", ["h-60"]],
    
    ["esp:27", "led-motor-b-fwd:A", "green", ["h50"]],
    ["esp:GND.3", "led-motor-b-fwd:C", "black", ["h60"]],
    
    ["esp:14", "led-motor-b-rev:A", "orange", ["h50"]],
    ["esp:GND.3", "led-motor-b-rev:C", "black", ["h60"]]
  ]
}
```

### 4.2 Understanding the Diagram

| Component | GPIO Pins | What It Represents |
|-----------|-----------|-------------------|
| HC-SR04 Ultrasonic | TRIG: GPIO32, ECHO: GPIO33 | Front distance sensor |
| Green LED (Motor A) | GPIO25 | Left motor forward |
| Red LED (Motor A) | GPIO26 | Left motor reverse |
| Green LED (Motor B) | GPIO27 | Right motor forward |
| Red LED (Motor B) | GPIO14 | Right motor reverse |

> **Why LEDs?** Real motors can't be simulated in Wokwi, so we use LEDs to visualize motor states. Green ON = Forward, Red ON = Reverse.

### ✅ Checkpoint
- [ ] `diagram.json` created
- [ ] Pin mappings match your `main.cpp` code

---

## Step 5: Configure WiFi Gateway for MQTT

### 5.1 Update `config.h` for Simulation

Modify `include/config.h` to use gateway-compatible settings:

```cpp
#ifndef CONFIG_H
#define CONFIG_H

// WiFi Configuration for Wokwi Simulation
// Wokwi uses special SSID for gateway connection
const char* WIFI_SSID = "Wokwi-GUEST";
const char* WIFI_PASSWORD = "";

// MQTT Broker - Use localhost since gateway bridges to real network
// For Wokwi simulation: use "host.wokwi.internal" to reach your PC
const char* MQTT_BROKER = "host.wokwi.internal";  
const int MQTT_PORT = 1883;

// Device Identity
const char* DEVICE_ID = "car-001";

// MQTT Topics
const char* TOPIC_TELEMETRY = "iot-car/car-001/telemetry";
const char* TOPIC_COMMAND = "iot-car/car-001/command";
const char* TOPIC_STATUS = "iot-car/car-001/status";
const char* TOPIC_RESPONSE = "iot-car/car-001/response";

#endif
```

### 5.2 Key Configuration Points

| Setting | Value | Explanation |
|---------|-------|-------------|
| `WIFI_SSID` | `"Wokwi-GUEST"` | Special SSID that triggers WiFi Gateway |
| `WIFI_PASSWORD` | `""` | Empty for Wokwi-GUEST |
| `MQTT_BROKER` | `"host.wokwi.internal"` | Special hostname that resolves to your PC |

> **Important Clarification:**
> 
> **Two-Layer System:**
> 1. **Layer 1 (Virtual WiFi):** `Wokwi-GUEST` creates a fake WiFi connection so your ESP32 code's `WiFi.begin()` succeeds. The ESP32 gets a virtual IP (10.13.37.x) but is **NOT** on your real home network.
> 
> 2. **Layer 2 (Network Bridge):** `host.wokwi.internal` is a special DNS hostname that Wokwi translates to `127.0.0.1` (your PC's localhost). This allows the virtual ESP32 to reach services on your PC.
> 
> **Why not use real WiFi?** Wokwi simulation can't access your physical WiFi adapter. The WiFi Gateway creates a virtual network and tunnels traffic to your PC instead.
> 
> **Result:** Virtual ESP32 → Virtual WiFi (`Wokwi-GUEST`) → Gateway Bridge → Your PC's localhost:1883 → Docker Mosquitto ✅

### 5.3 Verify Mosquitto Container is Running

Before proceeding, ensure your Docker container is running:

```powershell
# Check container status
docker ps | findstr iot-car-mqtt

# If not running, start it
cd "I:\My Drive\Project and docs\R & D\IoT MQTT Car\mqtt-broker"
docker-compose up -d
```

### ✅ Checkpoint
- [ ] `config.h` updated with Wokwi-specific settings
- [ ] Mosquitto Docker container running

---

## Step 6: Run the Simulation

### 6.1 Build in PlatformIO

1. Open your project in VS Code
2. Click **PlatformIO icon** (alien head) in sidebar
3. Click **Build** (checkmark icon)
4. Wait for successful build

```
Building .pio/build/esp32dev/firmware.bin
========================= [SUCCESS] ===========================
```

### 6.2 Start Wokwi Simulation

1. Press **F1** → Type **"Wokwi: Start Simulator"**
2. Or click the Wokwi icon in the status bar
3. The simulation window will open showing your virtual hardware

### 6.3 Expected Output

**In Wokwi Serial Monitor:**
```
IoT Car Starting...
Connecting to WiFi....
Connected! IP: 10.13.37.x
Connecting to MQTT...connected!
Subscribed to: iot-car/car-001/command
Telemetry sent: {"device_id":"car-001","timestamp":1234,"battery":100,"distance_front":50}
```

**In Wokwi Diagram:**
- You'll see the ESP32 board with connected components
- The HC-SR04 will show a distance slider you can adjust
- LEDs will light up when motors are activated

### 6.4 Test with MQTT CLI (via Docker)

Open a PowerShell terminal and subscribe:
```powershell
docker exec iot-car-mqtt mosquitto_sub -h localhost -t "iot-car/#" -v
```

You should see:
```
iot-car/car-001/status {"device_id":"car-001","status":"online"}
iot-car/car-001/telemetry {"device_id":"car-001","timestamp":xxxx,"battery":100,"distance_front":50}
```

Send a test command:
```powershell
docker exec iot-car-mqtt mosquitto_pub -h localhost -t "iot-car/car-001/command" -m '{\"action\":\"forward\"}'
```

Watch the LEDs in Wokwi light up!

### ✅ Checkpoint
- [ ] Simulation starts successfully
- [ ] ESP32 connects to WiFi (via gateway)
- [ ] ESP32 connects to Mosquitto (Docker container)
- [ ] Telemetry appears in `mosquitto_sub`
- [ ] Commands from CLI control virtual LEDs

---

## Step 7: Connect Android App to Virtual Car

### 7.1 Configure Android App

In your Android app, use your PC's IP address for MQTT:

```java
// MainActivity.java or MqttManager.java
private static final String MQTT_BROKER = "tcp://192.168.1.XXX:1883";  // Your PC's IP
```

To find your PC's IP:
```powershell
ipconfig
# Look for IPv4 Address under your WiFi adapter
```

### 7.2 Ensure Same Network

```
┌─────────────────────────────────────────────────────────────┐
│                    WiFi Router                              │
│                                                             │
│   ┌─────────────────┐              ┌─────────────────┐      │
│   │  Your PC        │              │  Android Phone  │      │
│   │  192.168.1.100  │              │  192.168.1.110  │      │
│   │                 │              │                 │      │
│   │ 🐳 Mosquitto    │◄────────────►│  MQTT Client    │      │
│   │    :1883        │              │                 │      │
│   │  Wokwi Sim      │              │                 │      │
│   └─────────────────┘              └─────────────────┘      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.3 Test Complete Flow

1. **Start Mosquitto container** (if not running)
   ```powershell
   cd "I:\My Drive\Project and docs\R & D\IoT MQTT Car\mqtt-broker"
   docker-compose up -d
   ```
2. **Start Wokwi simulation** in VS Code
3. **Start Go backend** (optional, for logging)
4. **Open Android app** on your phone
5. **Connect** to MQTT broker
6. **Tap control buttons** (Forward, Left, Right, Stop)
7. **Watch Wokwi** - LEDs should light up corresponding to commands!

### ✅ Checkpoint
- [ ] Android app connects to Mosquitto
- [ ] Commands from app reach virtual ESP32
- [ ] LEDs in Wokwi respond to app controls
- [ ] Telemetry from virtual ESP32 appears in app

---

## Troubleshooting

### ❌ "WiFi connection failed" in Wokwi

**Solution:**
- Ensure you're using `WIFI_SSID = "Wokwi-GUEST"`
- Check that WiFi Gateway is enabled in `wokwi.toml`

### ❌ "MQTT connection failed" in Wokwi

**Solution:**
- Use `MQTT_BROKER = "host.wokwi.internal"` (not localhost)
- Ensure Mosquitto container is running: `docker ps | findstr iot-car-mqtt`
- If not running, start it: `docker-compose up -d`
- Check container logs: `docker logs iot-car-mqtt`

### ❌ Android app can't connect to Mosquitto

**Solution:**
- Ensure phone and PC are on the same WiFi network
- Use PC's actual IP (e.g., `192.168.1.100`), not `localhost`
- Check Docker port mapping: `docker ps` should show `0.0.0.0:1883->1883/tcp`
- Check Windows Firewall allows port 1883:
  ```powershell
  netsh advfirewall firewall add rule name="Mosquitto MQTT Docker" dir=in action=allow protocol=tcp localport=1883
  ```

### ❌ Docker container won't start

**Solution:**
- Check Docker Desktop is running
- Check for port conflicts: `netstat -ano | findstr :1883`
- View container logs: `docker logs iot-car-mqtt`
- Remove and recreate: `docker-compose down && docker-compose up -d`

### ❌ Wokwi shows "License expired"

**Solution:**
- Press F1 → "Wokwi: Request a new License"
- Free tier has time limits; restart simulation when expired

### ❌ LEDs don't light up when commands sent

**Solution:**
- Check GPIO pin mappings in `diagram.json` match `motor_control.h`
- Add Serial.println() in command handler to debug
- Verify MQTT topic names match exactly

---

## Quick Reference

### Project Structure (Updated)

```
IoT MQTT Car/
├── mqtt-broker/                    <── NEW: Docker-based MQTT
│   ├── config/
│   │   └── mosquitto.conf
│   ├── data/
│   ├── log/
│   └── docker-compose.yml
├── firmware/
│   └── iot-car-esp32/
│       ├── src/
│       │   └── main.cpp
│       ├── include/
│       │   └── config.h
│       ├── platformio.ini
│       ├── wokwi.toml
│       └── diagram.json
└── ...
```

### Docker Commands Quick Reference

| Command | Description |
|---------|-------------|
| `docker-compose up -d` | Start Mosquitto in background |
| `docker-compose down` | Stop and remove container |
| `docker-compose restart` | Restart Mosquitto |
| `docker logs iot-car-mqtt` | View Mosquitto logs |
| `docker logs -f iot-car-mqtt` | Follow logs in real-time |
| `docker exec iot-car-mqtt mosquitto_sub -t "#" -v` | Subscribe to all topics |
| `docker exec iot-car-mqtt mosquitto_pub -t "topic" -m "msg"` | Publish a message |

### Files to Create/Modify

| File | Location | Purpose |
|------|----------|---------|
| `docker-compose.yml` | `mqtt-broker/` | Docker container definition |
| `mosquitto.conf` | `mqtt-broker/config/` | Mosquitto configuration |
| `wokwi.toml` | `firmware/iot-car-esp32/` | Wokwi configuration |
| `diagram.json` | `firmware/iot-car-esp32/` | Virtual hardware layout |
| `config.h` | `firmware/iot-car-esp32/include/` | Update WiFi/MQTT for simulation |

### Special Wokwi Hostnames

| Hostname | Purpose | What It Does |
|----------|---------|--------------|
| `Wokwi-GUEST` | Virtual WiFi SSID | Tricks ESP32 into thinking it's connected to WiFi (no real network access) |
| `host.wokwi.internal` | DNS hostname | Resolves to your PC's localhost (127.0.0.1) - use this to reach Docker containers |

**Key Concept:**
- `Wokwi-GUEST` = Fake WiFi connection (satisfies `WiFi.begin()`)
- `host.wokwi.internal` = Tunnel to your PC (lets you reach localhost:1883)
- Together they enable: Virtual ESP32 → Virtual WiFi → Gateway Bridge → Your PC's localhost

### MQTT Topics

| Topic | Direction | Description |
|-------|-----------|-------------|
| `iot-car/car-001/telemetry` | ESP32 → App | Sensor data |
| `iot-car/car-001/command` | App → ESP32 | Control commands |
| `iot-car/car-001/status` | ESP32 → All | Online/offline |
| `iot-car/car-001/response` | ESP32 → App | Command ACKs |

---

## Why Docker is Better for This Project

### Comparison: Native Install vs Docker

| Aspect | Native Mosquitto | Docker Mosquitto |
|--------|------------------|------------------|
| **Setup Time** | ~15 mins (download, install, configure) | ~5 mins (one command) |
| **System Impact** | Installs as Windows service | Isolated container |
| **Cleanup** | Uninstall wizard, leftover files | `docker-compose down` |
| **Version Control** | Manual updates | Pin version in compose file |
| **Portability** | Windows-specific | Same on Win/Mac/Linux |
| **Team Setup** | Each person installs manually | Share docker-compose.yml |
| **Conflicts** | May conflict with other services | Isolated environment |
| **Configuration** | Edit system files | Edit local config folder |

### Benefits for IoT Car Project

1. **Reproducible Environment** - Any team member can get identical setup
2. **Easy Reset** - Problems? Just recreate the container
3. **No Pollution** - Your system stays clean
4. **Future Ready** - Easy to add more services (Redis, InfluxDB, Grafana)
5. **CI/CD Friendly** - Same setup works in GitHub Actions

---

## Next Steps

After successful simulation testing:

1. ✅ Develop and debug firmware without physical hardware
2. ✅ Build and test Android app controls
3. ✅ Refine MQTT protocol and message formats
4. 🔜 When ready, switch `config.h` back to real WiFi credentials
5. 🔜 Flash firmware to physical ESP32
6. 🔜 Connect real motors and sensors

---

> **Tip:** Keep two `config.h` versions:
> - `config_wokwi.h` - For simulation
> - `config_real.h` - For physical hardware
> 
> Use `#include` to swap between them easily!

---

*Document Version: 2.0 (Docker Edition)*  
*Last Updated: 2026-01-22*  
*Project: IoT MQTT Car MVP*
