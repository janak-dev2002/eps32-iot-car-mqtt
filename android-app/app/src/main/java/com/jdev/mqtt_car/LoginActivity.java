package com.jdev.mqtt_car;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jdev.mqtt_car.mqtt.MqttPreferences;

/**
 * Login/Configuration Activity for MQTT Server settings.
 * Allows users to enter broker IP, port, and device ID.
 * Future: Will support username/password authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editBrokerIp;
    private EditText editBrokerPort;
    private EditText editDeviceId;
    private CheckBox checkRemember;
    private TextView errorText;
    private Button btnConnect;

    private MqttPreferences mqttPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize preferences
        mqttPrefs = new MqttPreferences(this);

        // Check if already configured and should auto-login
        if (mqttPrefs.isConfigured() && mqttPrefs.shouldRemember()) {
            // Skip login, go directly to MainActivity
            navigateToMain();
            return;
        }

        // Initialize views
        initializeViews();

        // Load saved values if any
        loadSavedConfig();
    }

    private void initializeViews() {
        editBrokerIp = findViewById(R.id.editBrokerIp);
        editBrokerPort = findViewById(R.id.editBrokerPort);
        editDeviceId = findViewById(R.id.editDeviceId);
        checkRemember = findViewById(R.id.checkRemember);
        errorText = findViewById(R.id.errorText);
        btnConnect = findViewById(R.id.btnConnect);

        // Connect button click
        btnConnect.setOnClickListener(v -> validateAndConnect());
    }

    private void loadSavedConfig() {
        if (mqttPrefs.isConfigured()) {
            editBrokerIp.setText(mqttPrefs.getBrokerIp());
            editBrokerPort.setText(String.valueOf(mqttPrefs.getBrokerPort()));
            editDeviceId.setText(mqttPrefs.getDeviceId());
            checkRemember.setChecked(mqttPrefs.shouldRemember());
        }
    }

    private void validateAndConnect() {
        hideError();

        // Get input values
        String brokerIp = editBrokerIp.getText().toString().trim();
        String portStr = editBrokerPort.getText().toString().trim();
        String deviceId = editDeviceId.getText().toString().trim();

        // Validate IP
        if (TextUtils.isEmpty(brokerIp)) {
            showError("Please enter server IP address");
            editBrokerIp.requestFocus();
            return;
        }

        // Validate port
        int port;
        if (TextUtils.isEmpty(portStr)) {
            port = 1883; // Default MQTT port
        } else {
            try {
                port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) {
                    showError("Port must be between 1 and 65535");
                    editBrokerPort.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Invalid port number");
                editBrokerPort.requestFocus();
                return;
            }
        }

        // Validate device ID
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "car-001"; // Default device ID
        }

        // Save configuration
        mqttPrefs.saveBrokerConfig(brokerIp, port, deviceId);
        mqttPrefs.setRememberCredentials(checkRemember.isChecked());

        // Navigate to main activity
        navigateToMain();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }
}
