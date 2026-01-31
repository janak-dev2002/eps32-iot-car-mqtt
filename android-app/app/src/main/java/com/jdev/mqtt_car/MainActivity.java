package com.jdev.mqtt_car;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jdev.mqtt_car.mqtt.MqttManager;
import com.jdev.mqtt_car.mqtt.MqttPreferences;

public class MainActivity extends AppCompatActivity implements MqttManager.MqttCallback {

    private MqttManager mqttManager;

    // Status indicators
    private View MqttIndicatorView, carIndicatorView;

    // Telemetry displays
    private TextView batteryText;
    private TextView distanceText;
    private TextView rssiText;
    private TextView tempText;
    private TextView actionText;

    // Buttons
    private Button btnConnect;
    private Button btnSettings;

    // Animations
    private Animation pulseAnimation;
    private Animation buttonPressAnimation;
    private Animation buttonReleaseAnimation;

    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load animations
        loadAnimations();

        // Initialize views
        initializeViews();

        // Initialize MQTT
        mqttManager = new MqttManager(this, this);

        // Setup control buttons with touch-hold behavior
        setupControlButtons();
    }

    /**
     * Load all animation resources
     */
    private void loadAnimations() {
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_glow);
        buttonPressAnimation = AnimationUtils.loadAnimation(this, R.anim.button_press);
        buttonReleaseAnimation = AnimationUtils.loadAnimation(this, R.anim.button_release);
    }

    /**
     * Initialize all UI views
     */
    private void initializeViews() {
        // Status indicators
        MqttIndicatorView = findViewById(R.id.MqttIndicator);
        carIndicatorView = findViewById(R.id.carIndicator);

        // Telemetry displays
        batteryText = findViewById(R.id.batteryText);
        distanceText = findViewById(R.id.distanceText);
        rssiText = findViewById(R.id.rssiText);
        tempText = findViewById(R.id.tempText);
        actionText = findViewById(R.id.actionText);

        // Buttons
        btnConnect = findViewById(R.id.btnConnect);
        btnSettings = findViewById(R.id.btnSettings);

        // Settings button - navigate to LoginActivity for reconfiguration
        btnSettings.setOnClickListener(v -> {
            // Disconnect if connected
            if (isConnected) {
                mqttManager.disconnect();
            }
            // Clear "remember" flag so login screen shows
            MqttPreferences prefs = new MqttPreferences(this);
            prefs.setRememberCredentials(false);
            // Navigate to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Connect button click handler
        btnConnect.setOnClickListener(v -> {
            animateButtonPress(v);
            if (!isConnected) {
                mqttManager.connect();
                btnConnect.setText("⚡ CONNECTING... ⚡");
            } else {
                mqttManager.disconnect();
                onDisconnected();
            }
        });
    }

    /**
     * Setup control buttons with touch-and-hold behavior and animations
     */
    private void setupControlButtons() {
        // Direction buttons - commands match ESP32: forward, backward, left, right,
        // stop
        setupControlButton(R.id.btnForward, "forward");
        setupControlButton(R.id.btnBackward, "backward");
        setupControlButton(R.id.btnLeft, "left");
        setupControlButton(R.id.btnRight, "right");

        // Stop button with special handling
        Button btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(v -> {
            animateButtonPress(v);
            mqttManager.sendCommand("stop");
            updateActionDisplay("STOP");
        });
    }

    /**
     * Setup a control button with touch-and-hold behavior.
     * Sends the specified action on press, and "stop" on release.
     *
     * @param buttonId The button resource ID
     * @param action   The action to send (forward, backward, left, right)
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupControlButton(int buttonId, String action) {
        Button button = findViewById(buttonId);

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Visual feedback
                    v.startAnimation(buttonPressAnimation);
                    // Start moving - send action directly to ESP32
                    mqttManager.sendCommand(action);
                    updateActionDisplay(action.toUpperCase());
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Visual feedback
                    v.startAnimation(buttonReleaseAnimation);
                    // Stop when released
                    mqttManager.sendCommand("stop");
                    updateActionDisplay("IDLE");
                    break;
            }
            return false; // Allow click events too
        });
    }

    /**
     * Animate button press with scale effect
     */
    private void animateButtonPress(View view) {
        view.startAnimation(buttonPressAnimation);
    }

    /**
     * Update the action display text
     */
    private void updateActionDisplay(String action) {
        runOnUiThread(() -> actionText.setText(action));
    }

    /**
     * Start pulse animation on a status indicator
     */
    private void startPulseAnimation(View indicator) {
        indicator.startAnimation(pulseAnimation);
    }

    /**
     * Stop any running animation on a view
     */
    private void stopAnimation(View view) {
        view.clearAnimation();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            isConnected = true;
            MqttIndicatorView.setBackgroundResource(R.drawable.circle_green);
            startPulseAnimation(MqttIndicatorView);
            btnConnect.setText("⚡ DISCONNECT ⚡");
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            isConnected = false;
            MqttIndicatorView.setBackgroundResource(R.drawable.circle_red);
            stopAnimation(MqttIndicatorView);
            carIndicatorView.setBackgroundResource(R.drawable.circle_red);
            stopAnimation(carIndicatorView);
            btnConnect.setText("⚡ CONNECT ⚡");

            // Reset telemetry displays
            batteryText.setText("---%");
            distanceText.setText("---cm");
            rssiText.setText("--- dBm");
            tempText.setText("---°C");
            actionText.setText("IDLE");
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTelemetryReceived(int battery, int distance, int temperature,
            String currentAction, int wifiRssi, int freeHeap) {
        runOnUiThread(() -> {
            // Update all telemetry displays
            batteryText.setText(battery + "%");
            distanceText.setText(distance + "cm");
            rssiText.setText(wifiRssi + " dBm");
            tempText.setText(temperature + "°C");

            // Format action text nicely
            String displayAction = currentAction.toUpperCase();
            if (displayAction.equals("STOP")) {
                displayAction = "IDLE";
            }
            actionText.setText(displayAction);
        });
    }

    @Override
    public void onStatusReceived(String status) {
        runOnUiThread(() -> {
            if (status.equals("online")) {
                carIndicatorView.setBackgroundResource(R.drawable.circle_green);
                startPulseAnimation(carIndicatorView);
            } else {
                carIndicatorView.setBackgroundResource(R.drawable.circle_red);
                stopAnimation(carIndicatorView);
            }
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            btnConnect.setText("⚡ CONNECT ⚡");
            stopAnimation(MqttIndicatorView);
            stopAnimation(carIndicatorView);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttManager.disconnect();
    }
}