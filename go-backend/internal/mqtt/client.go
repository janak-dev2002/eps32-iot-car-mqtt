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
		return fmt.Errorf("failed to connect to MQTT broker: %w", token.Error())
	}

	return nil
}

// Disconnect gracefully disconnects from the MQTT broker
func (c *Client) Disconnect() {
	if c.client != nil && c.client.IsConnected() {
		c.client.Disconnect(250)
		log.Println("📴 Disconnected from MQTT broker")
	}
}

// Publish sends a message to the specified topic
func (c *Client) Publish(topic string, qos byte, retained bool, payload any) error {
	token := c.client.Publish(topic, qos, retained, payload)
	token.Wait()
	return token.Error()
}

// onConnect is called when connection to broker is established
func (c *Client) onConnect(client mqtt.Client) {
	log.Println("✅ Connected to MQTT broker")

	// Subscribe to topics
	topics := map[string]byte{
		c.config.TopicTelemetry: 1, // QoS level 1
		c.config.TopicStatus:    1, // QoS level 1
	}

	if token := client.SubscribeMultiple(topics, nil); token.Wait() && token.Error() != nil {
		log.Printf("❌ Failed to subscribe to topics: %v", token.Error())
		return
	}

	log.Println("📡 Subscribed to topics:")
	for topic := range topics {
		log.Printf("   - %s", topic)
	}
}

// onConnectionLost is called when connection to broker is lost
func (c *Client) onConnectionLost(client mqtt.Client, err error) {
	log.Printf("⚠️ Connection lost: %v", err)
}

// onMessage is called when a message is received
func (c *Client) onMessage(client mqtt.Client, msg mqtt.Message) {
	topic := msg.Topic()
	payload := msg.Payload()

	log.Printf("📨 Received message on topic [%s]", topic)

	// Route message to appropriate handler
	switch {
	case matchTopic(topic, c.config.TopicTelemetry):
		c.telemetryHandler.Handle(payload)
	case matchTopic(topic, c.config.TopicStatus):
		c.statusHandler.Handle(payload)
	default:
		log.Printf("⚠️ Unknown topic: %s", topic)
	}
}

// matchTopic checks if a topic matches a pattern with wildcards
// Supports + (single level) and # (multi level) wildcards
func matchTopic(topic, pattern string) bool {
	topicParts := strings.Split(topic, "/")
	patternParts := strings.Split(pattern, "/")

	for i, patternPart := range patternParts {
		if patternPart == "#" {
			return true // # matches everything from this point
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
