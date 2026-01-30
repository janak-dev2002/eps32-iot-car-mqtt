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
		log.Fatalf("❌ Failed to connect to MQTT broker: %v", err)
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
	log.Println("🔄 Shutting down gracefully...")

	mqttClient.Disconnect()
	log.Println("👋 IoT Car Backend stopped")
}
