# ESP32 Basics for IoT Projects

> Quick reference for ESP32 development with PlatformIO

## What is ESP32?

ESP32 is a low-cost, low-power microcontroller with integrated WiFi and Bluetooth. Perfect for IoT applications.

| Feature | Specification |
|---------|---------------|
| CPU | Dual-core 240MHz |
| RAM | 520 KB |
| Flash | 4 MB |
| WiFi | 802.11 b/g/n |
| Bluetooth | BLE 4.2 |

## PlatformIO Setup

1. Install VS Code
2. Install PlatformIO extension
3. Create new project → Select ESP32

### Project Structure
```
project/
├── src/main.cpp      # Main code
├── include/          # Header files
├── lib/              # Libraries
├── platformio.ini    # Configuration
└── test/             # Unit tests
```

## GPIO Basics

```cpp
// Digital Output
pinMode(LED_PIN, OUTPUT);
digitalWrite(LED_PIN, HIGH);

// Digital Input
pinMode(BUTTON_PIN, INPUT_PULLUP);
int state = digitalRead(BUTTON_PIN);

// Analog Read (ADC)
int value = analogRead(SENSOR_PIN);  // 0-4095
```

## WiFi Connection

```cpp
#include <WiFi.h>

void setup() {
    WiFi.begin("SSID", "password");
    
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
    }
    
    Serial.println(WiFi.localIP());
}
```

## Common Libraries

| Library | Purpose |
|---------|---------|
| PubSubClient | MQTT client |
| ArduinoJson | JSON parsing |
| WiFi | Network connectivity |
| NewPing | Ultrasonic sensors |

## Learn More

- [ESP32 Official Docs](https://docs.espressif.com/projects/esp-idf/)
- [PlatformIO Docs](https://docs.platformio.org/)
- [Random Nerd Tutorials](https://randomnerdtutorials.com/)
