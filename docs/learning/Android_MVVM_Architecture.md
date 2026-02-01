# Android MVVM Architecture

> Understanding the architecture pattern used in the IoT Car Android app

## What is MVVM?

**MVVM** (Model-View-ViewModel) separates UI logic from business logic.

```
┌─────────────┐      ┌───────────────┐      ┌──────────────┐
│    View     │ ◄──► │   ViewModel   │ ◄──► │    Model     │
│ (Activity)  │      │   (LiveData)  │      │ (Data Layer) │
└─────────────┘      └───────────────┘      └──────────────┘
```

## Project Structure

```
com.jdev.mqtt_car/
├── ui/
│   ├── main/
│   │   ├── MainActivity.java      # View
│   │   └── MainViewModel.java     # ViewModel
│   └── login/
│       ├── LoginActivity.java
│       └── LoginViewModel.java
├── data/
│   └── source/
│       ├── MqttManager.java       # MQTT client wrapper
│       └── MqttPreferences.java   # SharedPreferences
└── model/
    ├── CarCommand.java            # Command model
    ├── CarStatus.java             # Status model
    ├── TelemetryData.java         # Telemetry model
    └── MqttConnectionState.java   # Enum
```

## Components in This App

| Component | Class | Purpose |
|-----------|-------|---------|
| **View** | `MainActivity` | Joystick UI, displays telemetry |
| **ViewModel** | `MainViewModel` | Holds UI state, handles callbacks |
| **Data Source** | `MqttManager` | MQTT connection & messaging |
| **Models** | `TelemetryData`, `CarCommand`, etc. | Data objects |

## Key Architecture Patterns

### 1. ViewModel with Callback Interface

`MainViewModel` implements `MqttManager.MqttCallback` to receive async events:

```java
public class MainViewModel extends AndroidViewModel 
        implements MqttManager.MqttCallback {
    
    private final MqttManager mqttManager;
    private final MutableLiveData<TelemetryData> telemetry = new MutableLiveData<>();
    private final MutableLiveData<MqttConnectionState> connectionState = new MutableLiveData<>();
    
    public MainViewModel(Application application) {
        super(application);
        mqttManager = new MqttManager(application.getApplicationContext(), this);
    }
    
    // Callback from MqttManager (background thread)
    @Override
    public void onTelemetryReceived(TelemetryData data) {
        telemetry.postValue(data);  // postValue for background threads
    }
}
```

### 2. LiveData for Reactive UI

Activity observes LiveData from ViewModel:

```java
// In MainActivity
viewModel.getTelemetryDataLiveData().observe(this, telemetry -> {
    distanceText.setText(telemetry.getDistanceFront() + " cm");
    batteryText.setText(telemetry.getBattery() + "%");
});

viewModel.getMqttConnectionStateLiveData().observe(this, state -> {
    updateConnectionUI(state);
});
```

### 3. MqttManager as Data Source

Handles all MQTT communication with callback interface:

```java
public class MqttManager {
    
    public interface MqttCallback {
        void onConnected();
        void onDisconnected();
        void onTelemetryReceived(TelemetryData data);
        void onCarStatusReceived(CarStatus carStatus);
        void onError(String message);
    }
    
    public void connect() { ... }
    public void disconnect() { ... }
    public void sendCommand(String action) { ... }
}
```

## Data Flow

```
User taps "Forward"
       │
       ▼
MainActivity.onClick()
       │
       ▼
MainViewModel.sendCommand("forward")
       │
       ▼
MqttManager.sendCommand("forward")
       │
       ▼
MQTT Broker → ESP32
       │
       ▼
ESP32 sends telemetry
       │
       ▼
MqttManager.onTelemetryReceived()
       │
       ▼
MainViewModel.onTelemetryReceived() → LiveData.postValue()
       │
       ▼
MainActivity observes → UI updated
```

## Why This Architecture?

✅ **AndroidViewModel** - Survives configuration changes, has Application context  
✅ **Callback Interface** - Clean async communication from MqttManager  
✅ **LiveData** - Lifecycle-aware, auto UI updates  
✅ **postValue()** - Safe for background thread callbacks  
✅ **No Repository** - Direct data source for simplicity in this project

## Learn More

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Documentation](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData Documentation](https://developer.android.com/topic/libraries/architecture/livedata)
