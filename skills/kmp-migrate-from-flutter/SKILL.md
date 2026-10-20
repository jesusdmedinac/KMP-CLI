---
name: kmp-migrate-from-flutter
description: Flutter Widget & BLoC migration to Compose Multiplatform & Coroutines, platform channel elimination, and phased Flutter modernization.
version: 1.0.0
author: jesusdmedinac
category: migration
tags:
  - migration
  - flutter
  - bloc
  - compose-multiplatform
  - platform-channels
  - coroutines
targets:
  - android
  - ios
triggers:
  - migrate flutter
  - flutter to kmp
  - flutter to compose
  - bloc to orbit mvi
  - method channel to kmp
  - flutter migration
compatibility:
  kotlin: ">=2.0.0"
  flutter: ">=3.0.0"
---

# Migrating Flutter Applications to Kotlin Multiplatform

This operational runbook guides engineering teams and AI agents through migrating Flutter codebases to Kotlin Multiplatform and Compose Multiplatform.

---

## 1. Conceptual Mapping: Flutter vs Compose Multiplatform

Flutter's reactive widget tree maps 1:1 to Compose Multiplatform:

| Flutter Concept | Compose Multiplatform Equivalent | Notes |
| :--- | :--- | :--- |
| `StatelessWidget` | `@Composable fun Component()` | Pure function taking immutable parameters |
| `StatefulWidget` / `State<T>` | `@Composable` with `remember { mutableStateOf() }` | Managed state with Compose snapshot system |
| `BLoC` (Events & States) | Orbit MVI `ContainerHost` | Direct mapping: `Event` -> `intent`, `State` -> `reduce` |
| `Riverpod` / `Provider` | `ViewModel` + `StateFlow` | Native lifecycle-aware state holders in `commonMain` |
| `MethodChannel` / `Pigeon` | Kotlin `expect/actual` or Interface DI | Compile-time safe platform abstractions without IPC serialization |
| `sqflite` | SQLDelight 2.x or Room KMP | Type-safe SQL schema compilation and reactive Flow queries |
| `dio` / `http` | Ktor Client 3.x | High-performance multiplatform HTTP client with Darwin and CIO |
| `cached_network_image` | Coil 3 `AsyncImage` | Declarative image loading and multiplatform disk caching |

---

## 2. Platform Channel Elimination

Flutter requires `MethodChannel` with binary Messenger serialization to talk to native Android and iOS code. In Kotlin Multiplatform, native capabilities are invoked directly via **zero-overhead language interfaces**:

### Step 1: Define Interface in `commonMain`
```kotlin
package com.example.shared.device

interface BatteryHealthMonitor {
    fun getBatteryLevel(): Int
    val isCharging: Boolean
}
```

### Step 2: Implement in `androidMain` (Direct Android SDK)
```kotlin
package com.example.shared.device

import android.content.Context
import android.os.BatteryManager

class AndroidBatteryHealthMonitor(private val context: Context) : BatteryHealthMonitor {
    override fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    override val isCharging: Boolean get() = false
}
```

### Step 3: Implement in `iosMain` (Direct Apple UIKit via Kotlin/Native)
```kotlin
package com.example.shared.device

import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryState

class IosBatteryHealthMonitor : BatteryHealthMonitor {
    override fun getBatteryLevel(): Int {
        UIDevice.currentDevice.batteryMonitoringEnabled = true
        return (UIDevice.currentDevice.batteryLevel * 100).toInt()
    }

    override val isCharging: Boolean
        get() = UIDevice.currentDevice.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateCharging
}
```

---

## 3. BLoC to Orbit MVI Migration

Transforming Flutter BLoCs to Kotlin MVI requires minimal cognitive overhead:

```kotlin
// In commonMain
package com.example.shared.counter

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce

data class CounterState(val count: Int = 0)

sealed interface CounterEvent {
    data object Increment : CounterEvent
    data object Decrement : CounterEvent
}

class CounterViewModel : ContainerHost<CounterState, Unit>, ViewModel() {
    override val container = container<CounterState, Unit>(CounterState())

    fun onEvent(event: CounterEvent) = intent {
        when (event) {
            is CounterEvent.Increment -> reduce { state.copy(count = state.count + 1) }
            is CounterEvent.Decrement -> reduce { state.copy(count = state.count - 1) }
        }
    }
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Leverage Kotlin Foundation Authority**: Use official KMP libraries (`kotlin-networking-ktor`, `kotlin-data-room-kmp`, `kotlin-media-coil`) rather than custom wrappers.
2. **Eliminate Dart VM Overhead**: Moving logic to KMP eliminates the Dart JIT/AOT memory footprint on Android and iOS.
3. **Automate Feasibility with `kmp migrate audit`**: Audit `pubspec.yaml` to identify packages with complex platform channels early.

### ❌ Anti-Patterns to Avoid:
1. **Never simulate MethodChannels in KMP**: Avoid string-based method routing; use typed interfaces.
2. **Never maintain dual build systems**: Consolidate build logic into standard Gradle or Kotlin Toolchain `module.yaml`.
