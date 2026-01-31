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

	log.Printf("📊 Telemetry from %s: Battery=%d%%, Distance=%dcm, Action=%s",
		telemetry.DeviceID,
		telemetry.Battery,
		telemetry.DistanceFront,
		telemetry.CurrentAction)

	// TODO: Save to database
	// TODO: Trigger alerts if battery low or obstacle detected
	// TODO: Send to WebSocket clients for real-time updates
}
