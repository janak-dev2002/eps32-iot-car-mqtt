package models

// Status represents the online/offline status of a device
type Status struct {
	DeviceID  string `json:"device_id"`
	Status    string `json:"status"`
	Timestamp int64  `json:"timestamp"`
}
