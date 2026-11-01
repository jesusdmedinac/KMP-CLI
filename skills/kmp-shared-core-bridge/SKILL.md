---
name: kmp-shared-core-bridge
description: Architecture, packaging, and integration of a Shared KMP Core module for existing native (SwiftUI/Compose) and hybrid mobile shells.
version: 1.0.0
author: jesusdmedinac
category: migration
tags:
  - migration
  - shared-core
  - bridge
  - packaging
  - spm
  - cocoapods
  - aar
targets:
  - android
  - ios
triggers:
  - shared core bridge
  - kmp bridge template
  - spm cocoapods kmp export
  - package kmp for native
  - hybrid kmp architecture
  - shared core migration
compatibility:
  kotlin: ">=2.0.0"
---

# Shared KMP Core Bridge Architecture & Packaging

The **Shared KMP Core Bridge** pattern enables incremental modernization of mobile codebases. Instead of replacing the entire application at once, developers build a standalone, reusable Kotlin Multiplatform module (`:shared`) packaged as:
1. **iOS**: Swift Package Manager (SPM) or CocoaPods dynamic framework with SKIE Swift concurrency support.
2. **Android**: Android Archive (`.aar`) or Maven Local dependency.

---

## 1. Multiplatform Module Configuration (`build.gradle.kts`)

Configure cross-compilation for CocoaPods/SPM and Android AAR:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.skie) // Generates native Swift async/await & enum wrappers
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(libs.kotlinx.coroutines.core)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
        }
    }
}
```

---

## 2. Shared Core Architecture & Boundary Hygiene

Structure `:shared` with strict separation of domain, data, and platform abstractions:

```
shared/
├── src/
│   ├── commonMain/
│   │   └── kotlin/
│   │       └── com/example/shared/
│   │           ├── domain/       # UseCases, entities, business invariants
│   │           ├── data/         # Repositories, Ktor client, SQLDelight/Room
│   │           └── bridge/       # Swift-friendly interfaces & coroutine adapters
│   ├── androidMain/              # Android-specific engines (OkHttp, EncryptedSharedPreferences)
│   └── iosMain/                  # Apple-specific engines (Darwin, NSUserDefaults, Keychain)
```

### Swift-Friendly Interface in `commonMain`:
```kotlin
package com.example.shared.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VehicleCoreBridge {
    val vehicleListFlow: StateFlow<List<VehicleItem>>
    suspend fun refreshVehicles()
    fun selectVehicle(vin: String)
}
```

---

## 3. Ingestion by Existing Shells

### In Swift / iOS (`Podfile` or `Package.swift`):
```swift
import SwiftUI
import SharedCore

@main
struct LegacyApp: App {
    let bridge: VehicleCoreBridge = SharedCoreProvider.shared.vehicleBridge

    var body: some Scene {
        WindowGroup {
            VehicleListView(bridge: bridge)
        }
    }
}
```

### In Android (`app/build.gradle.kts`):
```kotlin
dependencies {
    implementation(project(":shared"))
}
```

---

## 4. Scaffolding with KMP CLI

Scaffold this exact architectural baseline directly using KMP CLI:

```bash
kmp create MySharedBridge --template kmp-shared-core-bridge
```

### Key Capabilities Provided:
- Pre-configured Kotlin Multiplatform Gradle build with SPM/CocoaPods export.
- SKIE compiler plugin enabled for ergonomic Swift `async/await` bridging.
- Zero UI dependencies in `:shared` for maximal cross-platform portability.
