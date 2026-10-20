---
name: kotlin-data-store5
description: Offline-first single-source-of-truth data caching and synchronization with Store5, Ktor, and local persistence.
version: 1.0.0
author: jesusdmedinac
tags:
  - store
  - store5
  - cache
  - offline-first
  - repository
  - flow
  - grant-winner
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - store5
  - store kmp
  - offline first repository
  - cache sync kmp
  - single source of truth kmp
  - source of truth fetcher
compatibility:
  kotlin: ">=2.0.0"
  store5: ">=5.1.0"
---

# Offline-First Data Architecture with Store5

Store5 (Mobile Native Foundation) is the canonical reactive data-loading and caching library for Kotlin Multiplatform, sponsored by the **Kotlin Foundation Grants Program (2024)**. It solves the hardest part of mobile and desktop architecture: orchestrating network fetching, local disk caching, memory caching, and deduplication into a unified reactive `Flow<StoreReadResponse<T>>`.

---

## 1. Multiplatform Dependencies

### `module.yaml` (Kotlin Toolchain)
```yaml
dependencies:
  - org.mobilenativefoundation.store:store5:5.1.0
```

### `libs.versions.toml` (Gradle)
```toml
[versions]
store5 = "5.1.0"

[libraries]
store5 = { module = "org.mobilenativefoundation.store:store5", version.ref = "store5" }
```

In `shared/build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation(libs.store5)
}
```

---

## 2. Store Orchestration in `commonMain`

Construct a typed `Store` by pairing a network `Fetcher` with a database `SourceOfTruth`:

```kotlin
package com.example.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

data class Vehicle(
    val vin: String,
    val make: String,
    val model: String,
    val year: Int,
)

interface VehicleNetworkApi {
    suspend fun fetchVehicle(vin: String): Vehicle
}

interface VehicleLocalDatabase {
    fun getVehicleFlow(vin: String): Flow<Vehicle?>
    suspend fun saveVehicle(vehicle: Vehicle)
    suspend fun deleteVehicle(vin: String)
}

class VehicleRepository(
    private val api: VehicleNetworkApi,
    private val db: VehicleLocalDatabase,
) {
    private val store: Store<String, Vehicle> = StoreBuilder.from(
        fetcher = Fetcher.of { vin: String ->
            api.fetchVehicle(vin)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { vin: String ->
                db.getVehicleFlow(vin)
            },
            writer = { _: String, vehicle: Vehicle ->
                db.saveVehicle(vehicle)
            },
            delete = { vin: String ->
                db.deleteVehicle(vin)
            },
            deleteAll = {
                // optional bulk purge
            }
        )
    ).build()

    fun streamVehicle(vin: String, refresh: Boolean = false): Flow<StoreReadResponse<Vehicle>> =
        store.stream(StoreReadRequest.cached(key = vin, refresh = refresh))
}
```

---

## 3. UI Layer Integration & State Handling

Consume `StoreReadResponse` in your ViewModel or MVI container:

```kotlin
package com.example.app.ui.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.Vehicle
import com.example.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.mobilenativefoundation.store.store5.StoreReadResponse

sealed interface VehicleUiState {
    data object Loading : VehicleUiState
    data class Success(val vehicle: Vehicle, val isRefreshing: Boolean) : VehicleUiState
    data class Error(val message: String) : VehicleUiState
}

class VehicleDetailViewModel(
    vin: String,
    repository: VehicleRepository,
) : ViewModel() {

    val uiState: StateFlow<VehicleUiState> = repository.streamVehicle(vin, refresh = true)
        .map { response ->
            when (response) {
                is StoreReadResponse.Loading -> VehicleUiState.Loading
                is StoreReadResponse.Data -> VehicleUiState.Success(
                    vehicle = response.value,
                    isRefreshing = false,
                )
                is StoreReadResponse.Error.Exception -> VehicleUiState.Error(
                    response.error.message ?: "Failed to load vehicle"
                )
                is StoreReadResponse.Error.Message -> VehicleUiState.Error(response.message)
                is StoreReadResponse.NoNewData -> VehicleUiState.Loading
                is StoreReadResponse.Initial -> VehicleUiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VehicleUiState.Loading,
        )
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Source of Truth is Authoritative**: Never update UI state directly from raw network responses. Network fetches write to disk; UI observes disk.
2. **Handle Loading States Gracefully**: `StoreReadResponse.Loading` informs the UI when a background revalidation is in progress while existing cached data is still on screen (Stale-While-Revalidate).
3. **Use Memory Cache Cautiously**: Configure custom memory eviction policies if keys represent large entities or images.

### ❌ Anti-Patterns to Avoid:
1. **Never bypass the cache**: Calling `api.fetchVehicle()` directly inside UI ViewModels re-introduces cache divergence and flaky offline behavior.
2. **Never expose `Store` directly to Compose**: Expose high-level repository interfaces and map responses to domain `UiState` models.
3. **Never block on `store.get()`**: Prefer `store.stream()` to maintain reactive push updates when background syncing occurs.
