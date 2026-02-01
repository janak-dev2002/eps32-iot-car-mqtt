package com.jdev.mqtt_car.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jdev.mqtt_car.model.CarStatus;
import com.jdev.mqtt_car.model.MqttConnectionState;
import com.jdev.mqtt_car.model.TelemetryData;
import com.jdev.mqtt_car.data.source.MqttManager;

import org.jetbrains.annotations.NotNull;

public class MainViewModel extends AndroidViewModel implements MqttManager.MqttCallback {

    private final MqttManager mqttManager;
    private final MutableLiveData<TelemetryData> telemetry = new MutableLiveData<>(TelemetryData.empty());
    private final MutableLiveData<MqttConnectionState> connectionState = new MutableLiveData<>(MqttConnectionState.DISCONNECTED);
    private final MutableLiveData<CarStatus> carStatus = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> actionText = new MutableLiveData<>("Idle");


    public MainViewModel(@NotNull Application application){
        super(application);
        mqttManager = new MqttManager(application.getApplicationContext(), this);
    }


    // UI eka observe karan inna methods tika thama me.............

    public LiveData<TelemetryData> getTelemetryDataLiveData(){
        return telemetry;
    }

    public LiveData<MqttConnectionState> getMqttConnectionStateLiveData(){
        return connectionState;
    }

    public LiveData<CarStatus> getCarStatusLiveData(){
        return carStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getActionText(){
        return actionText;
    }


    //UI ekata access karanna ona wena methods tika....................

    public void connect(){
        //LiveData wala setValue method eka use karanna puluwan wenne UI thread ekata witharai, any other background thread cant use this, it will occur IllegalStateException
        connectionState.setValue(MqttConnectionState.CONNECTING);
        mqttManager.connect();
    }

    public void disconnect(){
        mqttManager.disconnect();
        carStatus.setValue(CarStatus.defaultStatus());
        telemetry.setValue(TelemetryData.empty());
        connectionState.setValue(MqttConnectionState.DISCONNECTED);
    }


    public void sendCommand(String command){
        mqttManager.sendCommand(command);
        actionText.setValue(command);
    }

    public boolean isConnected(){
        return connectionState.getValue() == MqttConnectionState.CONNECTED;
    }

    @Override
    public void onConnected() {
        //LiveData wala postValue() method eka use karanna puluwan wenne callBacks,
        // services and any background threads walin (Man hithanne ee unata unama thread ekakin post eka call karanna puluwan setValue eka thama beri)
        connectionState.postValue(MqttConnectionState.CONNECTED);
    }

    @Override
    public void onDisconnected() {

        connectionState.postValue(MqttConnectionState.DISCONNECTED);
        carStatus.postValue(CarStatus.defaultStatus());
        telemetry.postValue(TelemetryData.empty());
    }

    @Override
    public void onTelemetryReceived(int battery, int distance, int temperature, String currentAction, int wifiRssi, int freeHeap) {

        telemetry.postValue(new TelemetryData(battery,distance,temperature,currentAction,wifiRssi,freeHeap));

    }

    @Override
    public void onCarStatusReceived(String device_id, String status, String firmware) {
        carStatus.postValue(new CarStatus(device_id,status,firmware));
    }

    @Override
    public void onError(String message) {
        errorMessage.postValue(message);
        connectionState.postValue(MqttConnectionState.DISCONNECTED);
    }

    // Cleanup when ViewModel is destroyed
    @Override
    protected void onCleared() {
        super.onCleared();
        mqttManager.disconnect();
    }
}
