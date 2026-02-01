package com.jdev.mqtt_car.ui.login;


import android.app.Application;
import android.text.TextUtils;
import android.widget.CheckBox;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jdev.mqtt_car.data.source.MqttPreferences;

import org.jetbrains.annotations.NotNull;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private MqttPreferences mqttPrefs;

    public LiveData<Boolean> isLoginSuccess = loginSuccess;
    public LiveData<String> hasErrorMessage = errorMessage;
    public LiveData<Boolean> isLoading = loading;

    public LoginViewModel(@NotNull Application application){
        super(application);
        // Initialize preferences
        mqttPrefs = new MqttPreferences(application.getApplicationContext());
    }

    public void login(String brokerIp, String portStr, String deviceId, String userName, String password, CheckBox checkRemember){

        // Validate IP
        if (TextUtils.isEmpty(brokerIp)) {
            errorMessage.setValue("Please enter server IP address");
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
                    errorMessage.setValue("Port must be between 1 and 65535");
                    return;
                }
            } catch (NumberFormatException e) {
                errorMessage.setValue("Invalid port number");
                return;
            }
        }

        // Validate device ID
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "car-001"; // Default device ID
        }


        loading.setValue(true);

        // Save configuration
        mqttPrefs.saveBrokerConfig(brokerIp, port, deviceId);
        mqttPrefs.setRememberCredentials(checkRemember.isChecked());

        //save creden here after


        loading.setValue(false);
        loginSuccess.setValue(true);

    }

    public String getSavedBrokerIp(){
        return mqttPrefs.getBrokerIp();
    }

    public int getSavedBrokerPort(){
        return mqttPrefs.getBrokerPort();
    }

    public String getSavedDeviceId(){
        return mqttPrefs.getDeviceId();
    }

    public boolean getShouldRemember(){
        return mqttPrefs.shouldRemember();
    }

    public boolean isConfigured(){
        return mqttPrefs.isConfigured();
    }

}
