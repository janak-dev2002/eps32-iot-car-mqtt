package config

import (
	"os"
)

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
	MQTT *MQTTConfig
}

// Load loads configuration from environment variables with defaults
func Load() *AppConfig {
	return &AppConfig{
		MQTT: loadMQTTConfig(),
	}
}

// loadMQTTConfig loads MQTT configuration
func loadMQTTConfig() *MQTTConfig {
	return &MQTTConfig{
		BrokerAddress:       getEnv("MQTT_BROKER_ADDRESS", "localhost:1883"),
		ClientID:            getEnv("MQTT_CLIENT_ID", "go-backend"),
		TopicTelemetry:      getEnv("MQTT_TOPIC_TELEMETRY", "iot-car/+/telemetry"),
		TopicStatus:         getEnv("MQTT_TOPIC_STATUS", "iot-car/+/status"),
		TopicFleetBroadcast: getEnv("MQTT_TOPIC_FLEET_BROADCAST", "iot-car/fleet/broadcast"),
	}
}

// getEnv gets an environment variable or returns a default value
func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
