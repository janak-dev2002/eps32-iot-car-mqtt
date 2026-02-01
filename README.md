# 🚗 ESP32 IoT Car - MQTT Remote Control System

> A complete full-stack IoT project demonstrating embedded systems, mobile development, and backend integration

![ESP32](https://img.shields.io/badge/ESP32-PlatformIO-orange)
![Android](https://img.shields.io/badge/Android-Kotlin-green)
![Go](https://img.shields.io/badge/Backend-Go-blue)
![MQTT](https://img.shields.io/badge/Protocol-MQTT-purple)

## ✨ Features

- 📱 **Android App** - Kotlin with MVVM architecture, joystick control
- 🔌 **ESP32 Firmware** - Motor control, ultrasonic sensor, MQTT telemetry
- 🐹 **Go Backend** - Telemetry processing, fleet management
- 🐳 **Docker MQTT** - Mosquitto broker infrastructure
- 🎮 **Wokwi Simulation** - Test without hardware!

## 🏗️ Architecture

```
┌─────────────┐      MQTT       ┌──────────────┐      MQTT       ┌─────────────┐
│ Android App │ ──────────────► │   Mosquitto  │ ◄────────────── │   ESP32     │
│  (Kotlin)   │   Commands      │    Broker    │   Telemetry     │  Firmware   │
└─────────────┘                 └──────────────┘                 └─────────────┘
                                       │
                                       │ Subscribe
                                       ▼
                                ┌──────────────┐
                                │  Go Backend  │
                                │  (Monitor)   │
                                └──────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Docker Desktop
- Android Studio
- PlatformIO (VS Code extension)
- Go 1.25+

### 1. Start MQTT Broker
```bash
cd mqtt-broker
docker-compose up -d
```

### 2. Run Go Backend
```bash
cd go-backend
go run cmd/server/main.go
```

### 3. Run ESP32 (Wokwi Simulation)
```bash
cd firmware/iot-car-esp32
# Open in VS Code with PlatformIO + Wokwi extension
# or use wokwi.com with diagram.json
```

### 4. Run Android App
```bash
# Open android-app in Android Studio
# Build and run on emulator/device
```

## 📁 Project Structure

```
├── firmware/           # ESP32 PlatformIO project
├── android-app/        # Kotlin Android app (MVVM)
├── go-backend/         # Go telemetry server
├── mqtt-broker/        # Docker Mosquitto setup
└── docs/               # Documentation & guides
```

## 📚 Documentation

| Guide | Description |
|-------|-------------|
| [Wokwi Simulation Guide](docs/guides/Wokwi_ESP32_Simulation_Guide.md) | Set up virtual ESP32 |
| [MQTT Complete Guide](docs/guides/MQTT_Complete_Guide.md) | Learn MQTT protocol |
| [Go Backend Guide](docs/guides/GO_BACKEND_GUIDE.md) | Understand the backend |

### Learning Materials
| Topic | Description |
|-------|-------------|
| [ESP32 Basics](docs/learning/ESP32_Basics.md) | GPIO, WiFi, PlatformIO |
| [MQTT Overview](docs/learning/MQTT_Protocol_Overview.md) | Protocol fundamentals |
| [Android MVVM](docs/learning/Android_MVVM_Architecture.md) | Architecture patterns |
| [Why Go for IoT](docs/learning/Why_Go_For_IoT.md) | Go in IoT projects |

## 🎓 Educational Value

This project teaches:
- **Embedded Systems** - ESP32 programming, sensors, GPIO
- **IoT Protocols** - MQTT pub/sub, QoS, topics
- **Mobile Development** - Android, Kotlin, MVVM
- **Backend Development** - Go, concurrency, MQTT clients
- **DevOps** - Docker, containerization

## 📸 Demo

<!-- Add your demo GIF/screenshots here -->
<!-- ![Demo](docs/media/demo.gif) -->

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.

## 👤 Author

**Janak** - [GitHub Profile](https://github.com/janak-dev2002)

---

⭐ Star this repo if you find it helpful!
