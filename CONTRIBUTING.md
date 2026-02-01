# Contributing to ESP32 IoT Car

Thank you for your interest in contributing! 🎉

## 🚀 Getting Started

1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feature/your-feature`

## 📦 Development Setup

### ESP32 Firmware
```bash
cd firmware/iot-car-esp32
# Install PlatformIO CLI or use VS Code extension
pio run  # Build
```

### Android App
- Open `android-app/` in Android Studio
- Sync Gradle and build

### Go Backend
```bash
cd go-backend
go mod tidy
go run cmd/server/main.go
```

### MQTT Broker
```bash
cd mqtt-broker
docker-compose up -d
```

## 📝 Code Style

- **C++**: Follow Arduino/ESP-IDF conventions
- **Kotlin**: Use Android Kotlin style guide
- **Go**: Run `go fmt` before committing

## 🔀 Pull Request Process

1. Update documentation if needed
2. Test your changes
3. Create a PR with a clear description
4. Wait for review

## 🐛 Reporting Issues

- Use GitHub Issues
- Include steps to reproduce
- Add relevant logs/screenshots

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.
