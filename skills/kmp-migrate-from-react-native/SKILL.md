---
name: kmp-migrate-from-react-native
description: Strangler migration patterns, bridge removal, JS state to Kotlin StateFlow, and incremental React Native modernization to Kotlin Multiplatform.
version: 1.0.0
author: jesusdmedinac
category: migration
tags:
  - migration
  - react-native
  - strangler-fig
  - bridge-removal
  - stateflow
  - turbomodules
targets:
  - android
  - ios
triggers:
  - migrate react native
  - react native to kmp
  - strangler react native
  - bridge removal kmp
  - js state to stateflow
  - react native migration
compatibility:
  kotlin: ">=2.0.0"
  react-native: ">=0.70.0"
---

# Migrating React Native Applications to Kotlin Multiplatform

This operational runbook guides engineering teams and AI agents through migrating legacy React Native applications to Kotlin Multiplatform using the incremental **Strangler Fig pattern**.

---

## 1. Migration Strategy: The Strangler Fig Pattern

Never attempt a "big bang" rewrite of a production mobile application. Replace modules from the inside out:

```
┌────────────────────────────────────────────────────────┐
│ Phase 1: Core Networking & Auth Bridge (:shared KMP)   │
│  • Export SPM / CocoaPods framework for iOS            │
│  • Export Maven Local / AAR for Android                │
│  • Bridge Kotlin suspend functions to JS promises      │
├────────────────────────────────────────────────────────┤
│ Phase 2: Domain, Repositories & Local Persistence      │
│  • Replace AsyncStorage with Multiplatform-Settings    │
│  • Replace SQLite bridges with SQLDelight / Room KMP   │
│  • Strangle Redux/Zustand: push state via StateFlow    │
├────────────────────────────────────────────────────────┤
│ Phase 3: Incremental Screen Modernization              │
│  • Embed ComposeView in ReactRootView (Android)        │
│  • Embed ComposeUIViewController in RCTBridge (iOS)    │
│  • Modernize high-traffic screens first                │
├────────────────────────────────────────────────────────┤
│ Phase 4: Shell Deletion & Binary Finalization          │
│  • Remove ReactActivity and AppDelegate RCTBridge      │
│  • Remove JavaScript engine (Hermes / JSC)             │
│  • Finalize native binary compilation                  │
└────────────────────────────────────────────────────────┘
```

---

## 2. Dependency Replacement Mapping

| React Native Library | KMP / Native Equivalent | Recipe |
| :--- | :--- | :--- |
| `@react-native-async-storage/async-storage` | `kotlin-data-multiplatform-settings` | Wrap in `ObservableSettings` with `SharedPreferencesSettings` and `NSUserDefaultsSettings` |
| `axios` / `fetch` | `kotlin-networking-ktor` | Multiplatform Ktor Client with CIO/Darwin engines and content negotiation |
| `redux` / `zustand` / `mobx` | `kotlin-architecture-orbit-mvi` | Orbit MVI `ContainerHost` with type-safe state reduction |
| `@react-navigation/*` | `kotlin-ui-compose-adaptive` | Navigation 3 backstack with multi-pane adaptive scenes |
| `react-native-fast-image` | `kotlin-media-coil` | Coil 3 `AsyncImage` with cross-platform memory and Okio disk caching |
| `@react-native-firebase/*` | `kotlin-cloud-firebase` | GitLive Firebase Kotlin SDK with coroutine flows in `commonMain` |

---

## 3. Bridging KMP Core into React Native Shells

### Android (`android/app/src/main/java/.../KmpBridgeModule.kt`):
```kotlin
package com.example.app.bridge

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.example.shared.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KmpBridgeModule(
    reactContext: ReactApplicationContext,
    private val userRepository: UserRepository,
) : ReactContextBaseJavaModule(reactContext) {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun getName() = "KmpBridge"

    @ReactMethod
    fun fetchUserProfile(userId: String, promise: Promise) {
        scope.launch {
            try {
                val profile = userRepository.getUserProfile(userId)
                promise.resolve(profile.toJsonString())
            } catch (e: Exception) {
                promise.reject("FETCH_ERROR", e.message, e)
            }
        }
    }
}
```

### iOS (`ios/KmpBridge.swift`):
```swift
import Foundation
import React
import Shared

@objc(KmpBridge)
class KmpBridge: NSObject {
    private let userRepository = UserRepository()

    @objc
    fun fetchUserProfile(_ userId: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        Task {
            do {
                let profile = try await userRepository.getUserProfile(userId: userId)
                resolver(profile.toJsonString())
            } catch {
                rejecter("FETCH_ERROR", error.localizedDescription, error)
            }
        }
    }
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Unify Business Logic First**: Shift data validation, caching, and network models to KMP before touching UI.
2. **Expose Typed Schemas**: Use `kotlinx.serialization` to serialize data passed across the legacy bridge.
3. **Audit with `kmp migrate audit`**: Re-run the audit after each migration sprint to track remaining legacy bridge modules.

### ❌ Anti-Patterns to Avoid:
1. **Never build duplicate data models**: Do not maintain parallel TypeScript and Kotlin data contracts.
2. **Never swallow bridge cancellation**: Ensure React Native component unmounting cancels corresponding Kotlin Coroutine jobs.
3. **Avoid monolithic UI switches**: Migrate screens one route at a time using native view controller embedding.
