package models

// Command represents a control command to send to the IoT car
type Command struct {
	DeviceID  string `json:"device_id"`
	Action    string `json:"action"`
	Timestamp int64  `json:"timestamp"`
}
