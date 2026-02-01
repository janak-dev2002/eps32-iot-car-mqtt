package com.jdev.mqtt_car.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.jdev.mqtt_car.R;
import com.jdev.mqtt_car.ui.main.MainActivity;

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

    private LoginViewModel viewModel;

//    private MqttPreferences mqttPrefs;

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
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupObservers();
        // Check if already configured and should auto-login
        if (viewModel.isConfigured() && viewModel.getShouldRemember()) {
            // Skip login, go directly to MainActivity
            navigateToMain();
            return;
        }

        // Initialize views
        initializeViews();

        // Load saved values if any
        loadSavedConfig();
    }

    private void setupObservers() {

        viewModel.isLoginSuccess.observe(this, success -> {
            if (success != null && success) {
                navigateToMain();
            }
        });

        viewModel.hasErrorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                //Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

//        viewModel.isLoading.observe(this, loading -> {
//            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
//            binding.btnConnect.setEnabled(!loading);
//        });

    }

    private void initializeViews() {
        editBrokerIp = findViewById(R.id.editBrokerIp);
        editBrokerPort = findViewById(R.id.editBrokerPort);
        editDeviceId = findViewById(R.id.editDeviceId);
        checkRemember = findViewById(R.id.checkRemember);
        errorText = findViewById(R.id.errorText);
        btnConnect = findViewById(R.id.btnConnect);

        // Connect button click
        btnConnect.setOnClickListener(v -> appLogin());
    }

    private void loadSavedConfig() {
        if (viewModel.isConfigured()) {
            editBrokerIp.setText(viewModel.getSavedBrokerIp());
            editBrokerPort.setText(String.valueOf(viewModel.getSavedBrokerPort()));
            editDeviceId.setText(viewModel.getSavedDeviceId());
            checkRemember.setChecked(viewModel.getShouldRemember());
        }
    }

    private void appLogin() {

        hideError();

        String brokerIp = editBrokerIp.getText().toString().trim();
        String portStr = editBrokerPort.getText().toString().trim();
        String deviceId = editDeviceId.getText().toString().trim();

        viewModel.login(brokerIp, portStr, deviceId, "", "", checkRemember);
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
        finish(); // Close login activity, then you cant back to the login screen again
    }
}
