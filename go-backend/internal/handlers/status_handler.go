package handlers

import (
	"encoding/json"
	"log"

	"iot-car-backend/internal/models"
)

// StatusHandler processes device status messages (online/offline)
type StatusHandler struct {
	// Add dependencies here (e.g., database, notification service, etc.)
}

// NewStatusHandler creates a new StatusHandler instance
func NewStatusHandler() *StatusHandler {
	return &StatusHandler{}
}

// Handle processes the status payload
func (h *StatusHandler) Handle(payload []byte) {
	var status models.Status

	if err := json.Unmarshal(payload, &status); err != nil {
		log.Printf("❌ Status JSON parse error: %v", err)
		return
	}

	emoji := "🟢"
	if status.Status == "offline" {
		emoji = "🔴"
	}

	log.Printf("%s Device %s is %s", emoji, status.DeviceID, status.Status)

	// TODO: Update device status in database
	// TODO: Send notification if device goes offline unexpectedly
}
