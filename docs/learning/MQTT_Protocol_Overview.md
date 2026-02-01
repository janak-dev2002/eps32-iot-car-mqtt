# MQTT Protocol Overview

> Quick introduction to MQTT for IoT communication

## What is MQTT?

**MQTT** (Message Queuing Telemetry Transport) is a lightweight publish-subscribe messaging protocol designed for IoT devices.

```
Publisher ──► Broker ──► Subscriber(s)
```

## Key Concepts

| Concept | Description |
|---------|-------------|
| **Broker** | Central server that routes messages |
| **Client** | Any device that connects (publisher or subscriber) |
| **Topic** | Channel/path for messages (e.g., `home/sensor/temp`) |
| **Publish** | Send a message to a topic |
| **Subscribe** | Listen for messages on a topic |

## Why MQTT for IoT?

✅ **Lightweight** - Minimal overhead, works on constrained devices  
✅ **Reliable** - QoS levels ensure delivery  
✅ **Scalable** - One-to-many communication  
✅ **Low bandwidth** - Small packet size  

## QoS Levels

| QoS | Name | Use Case |
|-----|------|----------|
| 0 | At most once | Sensor data (loss acceptable) |
| 1 | At least once | Commands (must deliver) |
| 2 | Exactly once | Critical operations |

## Topic Structure

```
iot-car/car-001/command     # Commands to car
iot-car/car-001/telemetry   # Sensor data from car
iot-car/car-001/status      # Online/offline status
```

## Quick Example

```cpp
// ESP32 publishing
client.publish("iot-car/car-001/telemetry", "{\"battery\": 85}");

// ESP32 subscribing
client.subscribe("iot-car/car-001/command");
```

---

## 📚 Complete Guide

For in-depth MQTT learning with examples, see:

➡️ **[MQTT Complete Guide](../guides/MQTT_Complete_Guide.md)**

Covers:
- Broker setup (Mosquitto)
- Security & authentication
- Wildcards & topic patterns
- Best practices
