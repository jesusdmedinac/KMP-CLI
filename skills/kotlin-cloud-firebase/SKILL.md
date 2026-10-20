---
name: kotlin-cloud-firebase
description: Multiplatform Firebase integration (Auth, Firestore, Storage) with GitLive SDK, Coroutines Flows, and Kotlinx Serialization.
version: 1.0.0
author: jesusdmedinac
tags:
  - firebase
  - firestore
  - auth
  - storage
  - cloud
  - serialization
  - grant-winner
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - firebase kmp
  - gitlive firebase
  - firestore multiplatform
  - firebase auth kmp
  - firebase compose
  - cloud backend kmp
compatibility:
  kotlin: ">=2.0.0"
  gitlive-firebase: ">=2.1.0"
---

# Multiplatform Firebase Integration with GitLive SDK

The GitLive Firebase Kotlin SDK is the canonical multiplatform Firebase library, sponsored by the **Kotlin Foundation Grants Program (2025)**. It wraps native Firebase SDKs (Google Play Services on Android, CocoaPods/SPM on Apple, and JS SDK on Web) behind an idiomatic, coroutine-friendly Kotlin API in `commonMain`.

---

## 1. Multiplatform Dependencies

### `module.yaml` (Kotlin Toolchain)
```yaml
dependencies:
  - dev.gitlive:firebase-firestore:2.1.0
  - dev.gitlive:firebase-auth:2.1.0
  - dev.gitlive:firebase-common:2.1.0
```

### `libs.versions.toml` (Gradle)
```toml
[versions]
gitlive-firebase = "2.1.0"

[libraries]
firebase-common = { module = "dev.gitlive:firebase-common", version.ref = "gitlive-firebase" }
firebase-firestore = { module = "dev.gitlive:firebase-firestore", version.ref = "gitlive-firebase" }
firebase-auth = { module = "dev.gitlive:firebase-auth", version.ref = "gitlive-firebase" }
```

In `shared/build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation(libs.firebase.common)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.serialization.json)
}
```

---

## 2. Cloud Firestore with Kotlinx Serialization in `commonMain`

Model Firestore documents using `@Serializable` data classes and observe live document streams:

```kotlin
package com.example.app.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class VehicleDocument(
    val vin: String,
    val make: String,
    val model: String,
    val year: Int,
    val status: String = "ACTIVE",
)

class FirebaseVehicleDataSource {

    private val firestore get() = Firebase.firestore
    private val vehiclesCollection get() = firestore.collection("vehicles")

    fun observeVehicle(vin: String): Flow<VehicleDocument?> {
        return vehiclesCollection.document(vin).snapshots.map { snapshot ->
            if (snapshot.exists) {
                snapshot.data<VehicleDocument>()
            } else {
                null
            }
        }
    }

    suspend fun saveVehicle(vehicle: VehicleDocument) {
        vehiclesCollection.document(vehicle.vin).set(vehicle)
    }

    suspend fun deleteVehicle(vin: String) {
        vehiclesCollection.document(vin).delete()
    }
}
```

---

## 3. Multiplatform Authentication

Authenticate users seamlessly in `commonMain`:

```kotlin
package com.example.app.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthRepository {

    private val auth get() = Firebase.auth

    val currentUserIdFlow: Flow<String?> = auth.authStateChanged.map { it?.uid }

    suspend fun signIn(email: String, password: String): String? {
        val result = auth.signInWithEmailAndPassword(email, password)
        return result.user?.uid
    }

    suspend fun signOut() {
        auth.signOut()
    }
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Always use `@Serializable`**: Let GitLive decode documents directly into typed data classes (`snapshot.data<T>()`) to eliminate fragile dictionary parsing.
2. **Observe with Coroutine Flows**: Use `.snapshots` to automatically receive real-time updates across all supported platforms.
3. **Decouple Firebase from UI**: Keep `Firebase.firestore` inside repository classes in the data layer; never access Firebase singletons inside Composables.

### ❌ Anti-Patterns to Avoid:
1. **Never import Google Play Services `Task`**: Do not import `com.google.android.gms.tasks.*` into `commonMain`. GitLive APIs return standard Kotlin `suspend` functions and `Flow`.
2. **Never import CocoaPods `FIRFirestore`**: Rely exclusively on GitLive multiplatform wrappers in `commonMain`.
3. **Never store Firebase credentials in git**: Keep Google Services configuration files (`google-services.json`, `GoogleService-Info.plist`) gitignored or provisioned via CI secrets.
