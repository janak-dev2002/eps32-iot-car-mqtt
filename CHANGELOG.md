# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-02-01

### 🚀 Initial Release

#### Android App
- **MVVM Architecture** - Implemented with ViewModel, LiveData, and Repository pattern
- **UI Components**
  - Joystick control for directional commands
  - Real-time connection status indicator
  - Settings screen for MQTT broker configuration
- **MQTT Integration**
  - Paho MQTT client for reliable messaging
  - Automatic reconnection handling
  - QoS 1 for command delivery

#### ESP32 Firmware
- **Motor Control** - 4-directional movement (forward, backward, left, right)
- **Sensor Integration** - HC-SR04 ultrasonic distance sensor
- **MQTT Communication**
  - Telemetry publishing (distance, battery, status)
  - Command subscription and execution
  - Status heartbeat
- **Wokwi Simulation** - Full simulation support with LED indicators

#### Go Backend
- **Telemetry Handler** - JSON parsing and logging
- **Status Monitor** - Device connection tracking
- **Fleet Support** - Multi-device broadcast capability

#### Infrastructure
- **Docker MQTT Broker** - Mosquitto 2.0 with persistence
- **Topic Structure**
  ```
  iot-car/{device-id}/command    # App → ESP32
  iot-car/{device-id}/telemetry  # ESP32 → Backend
  iot-car/{device-id}/status     # ESP32 → All
  ```

---

## Future Roadmap

- [ ] Camera streaming integration
- [ ] Autonomous obstacle avoidance
- [ ] Multi-car fleet dashboard
- [ ] iOS app support
