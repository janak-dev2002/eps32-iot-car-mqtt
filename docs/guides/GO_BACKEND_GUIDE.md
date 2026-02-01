# 🚗 IoT Car Go Backend - Complete Developer Guide

> A comprehensive guide to understanding Go programming through a real-world IoT backend project

## 📖 Table of Contents

1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Go Fundamentals](#go-fundamentals)
4. [Package System Deep Dive](#package-system-deep-dive)
5. [Code Walkthrough](#code-walkthrough)
6. [How Packages Connect](#how-packages-connect)
7. [Design Patterns Used](#design-patterns-used)
8. [Best Practices](#best-practices)
9. [Running the Project](#running-the-project)
10. [Extending the Project](#extending-the-project)

---

## Introduction

This project is a **Go backend server** for an IoT Car system. It connects to an MQTT broker to receive telemetry data and status updates from ESP32-powered cars. This guide will teach you Go programming concepts using real code from this project.

### What You'll Learn

- Go project structure and conventions
- Package organization and imports
- Structs, interfaces, and methods
- Error handling patterns
- Concurrency with goroutines and channels
- Configuration management
- Clean architecture principles

---

## Project Structure

```
go-backend/
├── cmd/                        # Application entry points
│   └── server/
│       └── main.go             # Main application entry
├── internal/                   # Private application code
│   ├── config/
│   │   └── config.go           # Configuration management
│   ├── handlers/
│   │   ├── telemetry_handler.go
│   │   └── status_handler.go
│   ├── models/
│   │   ├── telemetry.go
│   │   ├── status.go
│   │   └── command.go
│   └── mqtt/
│       └── client.go           # MQTT client wrapper
├── go.mod                      # Module definition
└── go.sum                      # Dependency checksums
```

### Why This Structure?

| Directory | Purpose | Go Convention |
|-----------|---------|---------------|
| `cmd/` | Entry points for executables | Standard for multi-binary projects |
| `internal/` | Private packages (cannot be imported by other modules) | Go enforces this! |
| `models/` | Data structures (DTOs) | Clean architecture pattern |
| `handlers/` | Business logic for processing messages | Separation of concerns |
| `config/` | Configuration loading | Centralized config management |

---

## Go Fundamentals

### 1. Modules and go.mod

Every Go project starts with a **module**. The `go.mod` file defines your module:

```go
module iot-car-backend    // Module name (used in imports)

go 1.25.3                 // Go version

require github.com/eclipse/paho.mqtt.golang v1.5.1  // Dependencies
```

**Key Concepts:**
- **Module name** (`iot-car-backend`) is used as the base path for all imports
- Dependencies are automatically managed with `go mod tidy`
- `go.sum` contains checksums for security

### 2. Packages

Every `.go` file belongs to a **package**. The package name is declared at the top:

```go
package models  // This file belongs to the "models" package
```

**Rules:**
- All files in a directory must have the same package name
- Package name should match the directory name (convention)
- `package main` is special - it creates an executable

### 3. Imports

```go
import (
    "log"           // Standard library
    "os"            // Standard library
    
    "iot-car-backend/internal/config"    // Internal package
    "iot-car-backend/internal/mqtt"      // Internal package
    
    mqtt "github.com/eclipse/paho.mqtt.golang"  // External with alias
)
```

**Import Types:**
1. **Standard library** - Built into Go (no prefix)
2. **Internal packages** - Your code (module name prefix)
3. **External packages** - Third-party (full URL)
4. **Aliased imports** - Use `alias "path"` to rename

### 4. Exported vs Unexported

Go uses **capitalization** for visibility:

```go
// Exported (public) - starts with uppercase
func NewClient() *Client { }    // ✅ Accessible from other packages
type MQTTConfig struct { }      // ✅ Accessible from other packages

// Unexported (private) - starts with lowercase
func loadMQTTConfig() { }       // ❌ Only accessible within same package
func getEnv() { }               // ❌ Only accessible within same package
```

---

## Package System Deep Dive

### Package: `internal/models`

**Purpose:** Define data structures (DTOs - Data Transfer Objects)

```go
// models/telemetry.go
package models

// Telemetry represents sensor data from the IoT car
type Telemetry struct {
    DeviceID      string `json:"device_id"`      // JSON tag for serialization
    Timestamp     int64  `json:"timestamp"`
    Battery       int    `json:"battery"`
    DistanceFront int    `json:"distance_front"`
    Temperature   int    `json:"temperature"`
    CurrentAction string `json:"current_action"`
    WiFiRSSI      int    `json:"wifi_rssi"`
}
```

**Key Concepts:**

1. **Struct Tags** - Metadata for JSON serialization:
   ```go
   DeviceID string `json:"device_id"`
   //                    ↑ JSON field name
   ```

2. **Why separate models?**
   - Single source of truth for data structures
   - Easy to share between packages
   - Clear contract for data

---

### Package: `internal/config`

**Purpose:** Load and manage application configuration

```go
// config/config.go
package config

import "os"

// MQTTConfig holds MQTT broker configuration
type MQTTConfig struct {
    BrokerAddress       string
    ClientID            string
    TopicTelemetry      string
    TopicStatus         string
    TopicFleetBroadcast string
}

// AppConfig holds all application configuration
type AppConfig struct {
    MQTT *MQTTConfig    // Pointer to MQTTConfig
}

// Load loads configuration from environment variables with defaults
func Load() *AppConfig {
    return &AppConfig{
        MQTT: loadMQTTConfig(),    // Call private function
    }
}

// loadMQTTConfig loads MQTT configuration (unexported - private)
func loadMQTTConfig() *MQTTConfig {
    return &MQTTConfig{
        BrokerAddress:  getEnv("MQTT_BROKER_ADDRESS", "localhost:1883"),
        ClientID:       getEnv("MQTT_CLIENT_ID", "go-backend"),
        TopicTelemetry: getEnv("MQTT_TOPIC_TELEMETRY", "iot-car/+/telemetry"),
        TopicStatus:    getEnv("MQTT_TOPIC_STATUS", "iot-car/+/status"),
    }
}

// getEnv gets an environment variable or returns a default value
func getEnv(key, defaultValue string) string {
    if value := os.Getenv(key); value != "" {
        return value
    }
    return defaultValue
}
```

**Key Concepts:**

1. **Pointers (`*`):**
   ```go
   MQTT *MQTTConfig  // Pointer to MQTTConfig struct
   ```
   - `*` means "pointer to"
   - Pointers allow passing by reference (efficient for large structs)
   - `nil` is possible (empty pointer)

2. **Short variable declaration with `if`:**
   ```go
   if value := os.Getenv(key); value != "" {
       return value
   }
   ```
   - Declares `value` in the `if` scope
   - Cleaner than separate declaration

3. **Return pointer with `&`:**
   ```go
   return &MQTTConfig{ ... }
   //     ↑ "address of" - creates pointer
   ```

---

### Package: `internal/handlers`

**Purpose:** Process incoming MQTT messages

```go
// handlers/telemetry_handler.go
package handlers

import (
    "encoding/json"
    "log"
    "iot-car-backend/internal/models"
)

// TelemetryHandler processes incoming telemetry messages from IoT cars
type TelemetryHandler struct {
    // Add dependencies here (e.g., database, cache, etc.)
}

// NewTelemetryHandler creates a new TelemetryHandler instance
func NewTelemetryHandler() *TelemetryHandler {
    return &TelemetryHandler{}
}

// Handle processes the telemetry payload
func (h *TelemetryHandler) Handle(payload []byte) {
    var telemetry models.Telemetry

    if err := json.Unmarshal(payload, &telemetry); err != nil {
        log.Printf("❌ Telemetry JSON parse error: %v", err)
        return
    }

    log.Printf("📊 Telemetry from %s: Battery=%d%%", 
        telemetry.DeviceID, telemetry.Battery)
}
```

**Key Concepts:**

1. **Method Receiver:**
   ```go
   func (h *TelemetryHandler) Handle(payload []byte)
   //    ↑ receiver         ↑ method name
   ```
   - `(h *TelemetryHandler)` is the **receiver**
   - Makes `Handle` a method of `TelemetryHandler`
   - `h` is like `this` or `self` in other languages

2. **Constructor Pattern:**
   ```go
   func NewTelemetryHandler() *TelemetryHandler {
       return &TelemetryHandler{}
   }
   ```
   - Go doesn't have constructors
   - Convention: `NewTypeName()` function

3. **JSON Unmarshaling:**
   ```go
   var telemetry models.Telemetry
   err := json.Unmarshal(payload, &telemetry)
   //                             ↑ pass pointer to fill the struct
   ```

4. **Error Handling:**
   ```go
   if err := json.Unmarshal(...); err != nil {
       log.Printf("Error: %v", err)
       return  // Early return on error
   }
   ```
   - Go uses explicit error returns
   - Always check errors!

---

### Package: `internal/mqtt`

**Purpose:** MQTT client wrapper with IoT-specific functionality

```go
// mqtt/client.go
package mqtt

import (
    "fmt"
    "log"
    "strings"
    "time"

    "iot-car-backend/internal/config"
    "iot-car-backend/internal/handlers"

    mqtt "github.com/eclipse/paho.mqtt.golang"
)

// Client wraps the MQTT client with IoT car specific functionality
type Client struct {
    client           mqtt.Client
    config           *config.MQTTConfig
    telemetryHandler *handlers.TelemetryHandler
    statusHandler    *handlers.StatusHandler
}

// NewClient creates a new MQTT client with the given configuration
func NewClient(cfg *config.MQTTConfig) *Client {
    return &Client{
        config:           cfg,
        telemetryHandler: handlers.NewTelemetryHandler(),
        statusHandler:    handlers.NewStatusHandler(),
    }
}

// Connect establishes connection to the MQTT broker
func (c *Client) Connect() error {
    opts := mqtt.NewClientOptions()
    opts.AddBroker(fmt.Sprintf("tcp://%s", c.config.BrokerAddress))
    opts.SetClientID(c.config.ClientID)
    opts.SetDefaultPublishHandler(c.onMessage)
    opts.SetOnConnectHandler(c.onConnect)
    opts.SetConnectionLostHandler(c.onConnectionLost)
    opts.SetAutoReconnect(true)
    opts.SetKeepAlive(30 * time.Second)

    c.client = mqtt.NewClient(opts)

    if token := c.client.Connect(); token.Wait() && token.Error() != nil {
        return fmt.Errorf("failed to connect: %w", token.Error())
    }

    return nil
}

// onMessage is called when a message is received
func (c *Client) onMessage(client mqtt.Client, msg mqtt.Message) {
    topic := msg.Topic()
    payload := msg.Payload()

    switch {
    case matchTopic(topic, c.config.TopicTelemetry):
        c.telemetryHandler.Handle(payload)
    case matchTopic(topic, c.config.TopicStatus):
        c.statusHandler.Handle(payload)
    }
}

// matchTopic checks if a topic matches a pattern with wildcards
func matchTopic(topic, pattern string) bool {
    topicParts := strings.Split(topic, "/")
    patternParts := strings.Split(pattern, "/")

    for i, patternPart := range patternParts {
        if patternPart == "#" {
            return true
        }
        if i >= len(topicParts) {
            return false
        }
        if patternPart != "+" && patternPart != topicParts[i] {
            return false
        }
    }

    return len(topicParts) == len(patternParts)
}
```

**Key Concepts:**

1. **Dependency Injection:**
   ```go
   type Client struct {
       config           *config.MQTTConfig           // Injected config
       telemetryHandler *handlers.TelemetryHandler   // Injected handler
       statusHandler    *handlers.StatusHandler      // Injected handler
   }
   ```
   - Dependencies are passed in, not created internally
   - Makes testing easier

2. **Error Wrapping:**
   ```go
   return fmt.Errorf("failed to connect: %w", token.Error())
   //                                   ↑ %w wraps the original error
   ```
   - `%w` preserves the error chain
   - Allows `errors.Is()` and `errors.As()` to work

3. **Switch without condition:**
   ```go
   switch {
   case matchTopic(topic, c.config.TopicTelemetry):
       c.telemetryHandler.Handle(payload)
   case matchTopic(topic, c.config.TopicStatus):
       c.statusHandler.Handle(payload)
   }
   ```
   - Cleaner than multiple `if-else`

4. **Callback Functions:**
   ```go
   opts.SetOnConnectHandler(c.onConnect)
   //                       ↑ passing method as callback
   ```
   - Functions are first-class citizens in Go
   - Methods can be passed as function values

---

### Package: `cmd/server` (main)

**Purpose:** Application entry point

```go
// cmd/server/main.go
package main

import (
    "log"
    "os"
    "os/signal"
    "syscall"

    "iot-car-backend/internal/config"
    "iot-car-backend/internal/mqtt"
)

func main() {
    log.Println("🚗 IoT Car Backend Starting...")

    // Load configuration
    cfg := config.Load()
    log.Printf("📋 Configuration loaded - Broker: %s", cfg.MQTT.BrokerAddress)

    // Create and connect MQTT client
    mqttClient := mqtt.NewClient(cfg.MQTT)
    if err := mqttClient.Connect(); err != nil {
        log.Fatalf("❌ Failed to connect: %v", err)
    }

    log.Println("🚀 IoT Car Backend is running!")

    // Wait for shutdown signal
    waitForShutdown(mqttClient)
}

// waitForShutdown blocks until an interrupt signal is received
func waitForShutdown(mqttClient *mqtt.Client) {
    sigChan := make(chan os.Signal, 1)
    signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)

    sig := <-sigChan
    log.Printf("🛑 Received signal: %v", sig)

    mqttClient.Disconnect()
    log.Println("👋 IoT Car Backend stopped")
}
```

**Key Concepts:**

1. **`package main` and `func main()`:**
   - Only `package main` creates an executable
   - `func main()` is the entry point

2. **Channels:**
   ```go
   sigChan := make(chan os.Signal, 1)
   //              ↑ create channel of os.Signal with buffer of 1
   
   sig := <-sigChan
   //     ↑ receive from channel (blocks until value arrives)
   ```

3. **Graceful Shutdown:**
   ```go
   signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)
   ```
   - Listens for Ctrl+C or kill signal
   - Allows cleanup before exit

---

## How Packages Connect

```
┌─────────────────────────────────────────────────────────────────┐
│                        cmd/server/main.go                       │
│                         (Entry Point)                           │
└─────────────────────────────────────────────────────────────────┘
                    │                    │
                    │ imports            │ imports
                    ▼                    ▼
┌───────────────────────────┐    ┌───────────────────────────────┐
│    internal/config        │    │       internal/mqtt           │
│    (Configuration)        │    │      (MQTT Client)            │
└───────────────────────────┘    └───────────────────────────────┘
                                          │
                                          │ imports
                        ┌─────────────────┼─────────────────┐
                        │                 │                 │
                        ▼                 ▼                 ▼
            ┌───────────────────┐ ┌───────────────┐ ┌───────────────┐
            │ internal/handlers │ │internal/config│ │internal/models│
            │ (Message Logic)   │ │(Configuration)│ │ (Data Types)  │
            └───────────────────┘ └───────────────┘ └───────────────┘
                        │
                        │ imports
                        ▼
            ┌───────────────────┐
            │  internal/models  │
            │   (Data Types)    │
            └───────────────────┘
```

### Data Flow

```
1. ESP32 Car sends JSON → MQTT Broker
                              ↓
2. MQTT Broker → internal/mqtt/client.go (onMessage callback)
                              ↓
3. client.go routes to → internal/handlers/*_handler.go
                              ↓
4. Handler parses using → internal/models/*.go (structs)
                              ↓
5. Handler processes data (log, save to DB, etc.)
```

---

## Design Patterns Used

### 1. Factory Pattern (Constructor Functions)

```go
// NewClient is a factory function
func NewClient(cfg *config.MQTTConfig) *Client {
    return &Client{
        config:           cfg,
        telemetryHandler: handlers.NewTelemetryHandler(),
        statusHandler:    handlers.NewStatusHandler(),
    }
}
```

### 2. Dependency Injection

```go
// Config is injected, not created inside
func NewClient(cfg *config.MQTTConfig) *Client
```

### 3. Handler Pattern

```go
// Each handler has a Handle method
func (h *TelemetryHandler) Handle(payload []byte)
func (h *StatusHandler) Handle(payload []byte)
```

### 4. Configuration Pattern

```go
// Centralized config with environment variable support
cfg := config.Load()
```

---

## Best Practices

### ✅ DO

1. **Use `internal/` for private packages**
   - Go enforces that `internal/` packages can't be imported externally

2. **Return errors, don't panic**
   ```go
   func Connect() error {  // Return error
       if err != nil {
           return fmt.Errorf("failed: %w", err)
       }
       return nil
   }
   ```

3. **Use meaningful names**
   ```go
   // Good
   type TelemetryHandler struct{}
   func (h *TelemetryHandler) Handle(payload []byte)
   
   // Bad
   type TH struct{}
   func (t *TH) H(p []byte)
   ```

4. **Keep main.go minimal**
   - Only orchestration, no business logic

5. **Use constructor functions**
   ```go
   client := mqtt.NewClient(cfg)  // Not &mqtt.Client{}
   ```

6. **Document exported functions**
   ```go
   // NewClient creates a new MQTT client with the given configuration
   func NewClient(cfg *config.MQTTConfig) *Client
   ```

### ❌ DON'T

1. **Don't use global variables**
   - Pass dependencies explicitly

2. **Don't ignore errors**
   ```go
   // Bad
   json.Unmarshal(data, &obj)
   
   // Good
   if err := json.Unmarshal(data, &obj); err != nil {
       return err
   }
   ```

3. **Don't put everything in one package**
   - Separate by responsibility

4. **Don't import `internal/` in tests from outside**
   - Won't compile!

---

## Running the Project

### Prerequisites

1. **Install Go** (1.20+): https://golang.org/dl/
2. **MQTT Broker** running (mosquitto)

### Commands

```bash
# Navigate to project
cd go-backend

# Download dependencies
go mod tidy

# Build the application
go build -o server ./cmd/server

# Run the application
go run ./cmd/server

# Or run the built binary
./server
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MQTT_BROKER_ADDRESS` | `localhost:1883` | MQTT broker address |
| `MQTT_CLIENT_ID` | `go-backend` | Client identifier |
| `MQTT_TOPIC_TELEMETRY` | `iot-car/+/telemetry` | Telemetry subscription |
| `MQTT_TOPIC_STATUS` | `iot-car/+/status` | Status subscription |

```bash
# Run with custom config
MQTT_BROKER_ADDRESS=192.168.1.100:1883 go run ./cmd/server
```

---

## Extending the Project

### Adding a New Handler

1. **Create the model** (`internal/models/alert.go`):
   ```go
   package models
   
   type Alert struct {
       DeviceID string `json:"device_id"`
       Type     string `json:"type"`
       Message  string `json:"message"`
   }
   ```

2. **Create the handler** (`internal/handlers/alert_handler.go`):
   ```go
   package handlers
   
   import (
       "encoding/json"
       "log"
       "iot-car-backend/internal/models"
   )
   
   type AlertHandler struct{}
   
   func NewAlertHandler() *AlertHandler {
       return &AlertHandler{}
   }
   
   func (h *AlertHandler) Handle(payload []byte) {
       var alert models.Alert
       if err := json.Unmarshal(payload, &alert); err != nil {
           log.Printf("Alert parse error: %v", err)
           return
       }
       log.Printf("🚨 Alert from %s: %s", alert.DeviceID, alert.Message)
   }
   ```

3. **Add to MQTT client** (`internal/mqtt/client.go`):
   ```go
   type Client struct {
       // ... existing fields
       alertHandler *handlers.AlertHandler  // Add this
   }
   
   func NewClient(cfg *config.MQTTConfig) *Client {
       return &Client{
           // ... existing
           alertHandler: handlers.NewAlertHandler(),  // Add this
       }
   }
   ```

4. **Add topic to config** and subscribe

### Adding Database Support

```go
// internal/database/db.go
package database

import "database/sql"

type DB struct {
    conn *sql.DB
}

func New(connectionString string) (*DB, error) {
    conn, err := sql.Open("postgres", connectionString)
    if err != nil {
        return nil, err
    }
    return &DB{conn: conn}, nil
}

func (db *DB) SaveTelemetry(t models.Telemetry) error {
    // Implementation
}
```

Then inject into handlers:

```go
type TelemetryHandler struct {
    db *database.DB  // Add dependency
}

func NewTelemetryHandler(db *database.DB) *TelemetryHandler {
    return &TelemetryHandler{db: db}
}
```

---

## Quick Reference

### Common Go Commands

```bash
go mod init <module-name>  # Create new module
go mod tidy                # Sync dependencies
go build ./...             # Build all packages
go run ./cmd/server        # Run application
go test ./...              # Run all tests
go fmt ./...               # Format code
go vet ./...               # Check for issues
```

### Import Cheat Sheet

```go
import (
    // Standard library (alphabetical)
    "encoding/json"
    "fmt"
    "log"
    
    // Internal packages (alphabetical)
    "iot-car-backend/internal/config"
    "iot-car-backend/internal/handlers"
    
    // External packages (alphabetical)
    mqtt "github.com/eclipse/paho.mqtt.golang"
)
```

### Struct Tag Reference

```go
type Example struct {
    Field string `json:"field_name"`           // JSON key
    Field string `json:"field,omitempty"`      // Omit if empty
    Field string `json:"-"`                    // Ignore field
    Field string `json:"field" db:"field"`    // Multiple tags
}
```

---

## Summary

This project demonstrates:

| Concept | Where to Find |
|---------|--------------|
| Project structure | `cmd/`, `internal/` directories |
| Package organization | Each `internal/` subdirectory |
| Structs & methods | `models/`, `handlers/` |
| Configuration | `config/config.go` |
| Error handling | Throughout, especially `mqtt/client.go` |
| Dependency injection | `NewClient()`, `NewHandler()` |
| Channels | `waitForShutdown()` in `main.go` |
| JSON handling | `handlers/*_handler.go` |

Happy coding! 🚀
