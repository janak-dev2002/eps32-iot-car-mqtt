package models

// Telemetry represents sensor data from the IoT car
type Telemetry struct {
	DeviceID      string `json:"device_id"`
	Timestamp     int64  `json:"timestamp"`
	Battery       int    `json:"battery"`
	DistanceFront int    `json:"distance_front"`
	Temperature   int    `json:"temperature"`
	CurrentAction string `json:"current_action"`
	WiFiRSSI      int    `json:"wifi_rssi"`
}
