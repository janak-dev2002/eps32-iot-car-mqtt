package com.jdev.mqtt_car.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;

import com.jdev.mqtt_car.model.MqttConnectionState;
import com.jdev.mqtt_car.ui.login.LoginActivity;
import com.jdev.mqtt_car.R;
import com.jdev.mqtt_car.data.source.MqttPreferences;

public class MainActivity extends AppCompatActivity {

//    private MqttManager mqttManager;

    private MainViewModel mainViewModel;// Status indicators
    private View MqttIndicatorView, carIndicatorView;

    // Telemetry displays
    private TextView batteryText, distanceText, rssiText, tempText, actionText;

    // Buttons
    private Button btnConnect,btnSettings;

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


        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        // Initialize Observers......
        setUpObservers();

        // Load animations
        loadAnimations();

        // Initialize views
        initializeViews();

        // Setup control buttons with touch-hold behavior
        setupControlButtons();
    }
    @SuppressLint("SetTextI18n")
    private void setUpObservers() {

        mainViewModel.getTelemetryDataLiveData().observe(this, data -> {
            batteryText.setText(data.getBatteryDisplay());
            distanceText.setText(data.getDistanceDisplay());
            rssiText.setText(data.getRssiDisplay());
            tempText.setText(data.getTemperatureDisplay());
            updateActionDisplay(data.getCurrentAction().toUpperCase());


        });

        mainViewModel.getActionText().observe(this, data -> {
            // Format action text nicely
            updateActionDisplay(data.toUpperCase());
        });

        mainViewModel.getCarStatusLiveData().observe(this, data -> {
            if (data.getStatus().equals("online")) {
                carIndicatorView.setBackgroundResource(R.drawable.circle_green);
                startPulseAnimation(carIndicatorView);
            } else {
                carIndicatorView.setBackgroundResource(R.drawable.circle_red);
                stopAnimation(carIndicatorView);
            }
        });

        mainViewModel.getMqttConnectionStateLiveData().observe(this, data -> {

            if (data.equals(MqttConnectionState.CONNECTING)) {

                btnConnect.setText("⚡ CONNECTING ⚡");
            } else if (data.equals(MqttConnectionState.CONNECTED)) {
                isConnected = true;
                MqttIndicatorView.setBackgroundResource(R.drawable.circle_green);
                startPulseAnimation(MqttIndicatorView);
                btnConnect.setText("⚡ DISCONNECT ⚡");
            } else {
                isConnected = false;
                MqttIndicatorView.setBackgroundResource(R.drawable.circle_red);
                stopAnimation(MqttIndicatorView);
                btnConnect.setText("⚡ CONNECT ⚡");
            }
        });

        mainViewModel.getErrorMessage().observe(this, data -> {
            btnConnect.setText("⚡ CONNECT ⚡");
            stopAnimation(MqttIndicatorView);
            stopAnimation(carIndicatorView);
        });

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
                mainViewModel.disconnect();
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
                mainViewModel.connect();
            } else {
                mainViewModel.disconnect();
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
            mainViewModel.sendCommand("stop");
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
            Log.d("MainActivity", "Touch event: " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Visual feedback
                    v.startAnimation(buttonPressAnimation);
                    // Start moving - send action directly to ESP32
                    mainViewModel.sendCommand(action);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Visual feedback
                    v.startAnimation(buttonReleaseAnimation);
                    // Stop when released
                    mainViewModel.sendCommand("stop");
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
        String displayAction = action;
        if (displayAction.equals("STOP")) {
            displayAction = "IDLE";
        }
        actionText.setText(displayAction);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainViewModel.disconnect();
    }
}