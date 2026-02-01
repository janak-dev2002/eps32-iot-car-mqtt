# Android MVVM Architecture

> Understanding the architecture pattern used in this IoT Car Android app

## What is MVVM?

**MVVM** (Model-View-ViewModel) separates UI logic from business logic.

```
┌─────────┐      ┌───────────┐      ┌─────────┐
│  View   │ ◄──► │ ViewModel │ ◄──► │  Model  │
│  (UI)   │      │ (Logic)   │      │ (Data)  │
└─────────┘      └───────────┘      └─────────┘
```

## Components

| Component | Purpose | In This App |
|-----------|---------|-------------|
| **View** | UI (Activities, Fragments) | `MainActivity`, `SettingsActivity` |
| **ViewModel** | UI state & logic | `MainViewModel`, `SettingsViewModel` |
| **Model** | Data layer | MQTT repository, preferences |

## Why MVVM for Android?

✅ **Lifecycle-aware** - Survives configuration changes  
✅ **Testable** - Business logic separated from UI  
✅ **Clean** - Clear separation of concerns  
✅ **Reactive** - LiveData updates UI automatically  

## Key Android Components

### LiveData
```kotlin
// ViewModel
val connectionStatus = MutableLiveData<Boolean>()

// Activity
viewModel.connectionStatus.observe(this) { isConnected ->
    updateUI(isConnected)
}
```

### ViewModel
```kotlin
class MainViewModel : ViewModel() {
    private val mqttRepository = MqttRepository()
    
    fun sendCommand(direction: String) {
        mqttRepository.publish("command", direction)
    }
}
```

### Repository Pattern
```kotlin
class MqttRepository {
    private val client = MqttClient()
    
    fun publish(topic: String, message: String) {
        client.publish(topic, message)
    }
}
```

## App Architecture Flow

```
User taps joystick
       │
       ▼
   MainActivity (View)
       │
       ▼
   MainViewModel.sendCommand("forward")
       │
       ▼
   MqttRepository.publish(...)
       │
       ▼
   MQTT Broker → ESP32
```

## Learn More

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Documentation](https://developer.android.com/topic/libraries/architecture/viewmodel)
