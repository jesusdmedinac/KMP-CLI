---
name: kotlin-architecture-orbit-mvi
description: Unidirectional Data Flow (MVI/UDF) state management with Orbit MVI, ContainerHost, and Compose Multiplatform.
version: 1.0.0
author: jesusdmedinac
tags:
  - orbit
  - orbit-mvi
  - mvi
  - udf
  - state-machine
  - architecture
  - grant-winner
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - orbit mvi
  - mvi compose multiplatform
  - container host orbit
  - udf state management kmp
  - side effects orbit
  - intent reduce orbit
compatibility:
  kotlin: ">=2.0.0"
  compose-multiplatform: ">=1.7.0"
  orbit: ">=9.0.0"
---

# Unidirectional Data Flow with Orbit MVI

Orbit MVI is the canonical Model-View-Intent (MVI) and Unidirectional Data Flow (UDF) library for Kotlin Multiplatform, sponsored by the **Kotlin Foundation Grants Program (2024)**. It brings type-safe state reduction, side-effect streaming, and deterministic coroutine testing to `commonMain`.

---

## 1. Multiplatform Dependencies

### `module.yaml` (Kotlin Toolchain)
```yaml
dependencies:
  - org.orbit-mvi:orbit-core:9.0.0
  - org.orbit-mvi:orbit-compose:9.0.0
```

### `libs.versions.toml` (Gradle)
```toml
[versions]
orbit = "9.0.0"

[libraries]
orbit-core = { module = "org.orbit-mvi:orbit-core", version.ref = "orbit" }
orbit-compose = { module = "org.orbit-mvi:orbit-compose", version.ref = "orbit" }
orbit-test = { module = "org.orbit-mvi:orbit-test", version.ref = "orbit" }
```

In `shared/build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
}

commonTest.dependencies {
    implementation(libs.orbit.test)
}
```

---

## 2. ContainerHost ViewModel in `commonMain`

Implement `ContainerHost<STATE, SIDE_EFFECT>` on an `androidx.lifecycle.ViewModel`:

```kotlin
package com.example.app.ui.counter

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.postSideEffect

data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false,
)

sealed interface CounterSideEffect {
    data class ShowToast(val message: String) : CounterSideEffect
    data object NavigateToSummary : CounterSideEffect
}

class CounterViewModel : ContainerHost<CounterState, CounterSideEffect>, ViewModel() {

    override val container = container<CounterState, CounterSideEffect>(CounterState())

    fun increment() = intent {
        reduce {
            state.copy(count = state.count + 1)
        }
        if (state.count >= 10) {
            postSideEffect(CounterSideEffect.ShowToast("Goal reached: 10!"))
        }
    }

    fun decrement() = intent {
        if (state.count > 0) {
            reduce {
                state.copy(count = state.count - 1)
            }
        } else {
            postSideEffect(CounterSideEffect.ShowToast("Count cannot be negative"))
        }
    }

    fun onFinishClicked() = intent {
        postSideEffect(CounterSideEffect.NavigateToSummary)
    }
}
```

---

## 3. UI Layer Integration with Compose Multiplatform

Collect state and side-effects in Compose using `collectAsState()` and `collectSideEffect`:

```kotlin
package com.example.app.ui.counter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CounterScreen(
    viewModel: CounterViewModel,
    onShowSnackbar: (String) -> Unit,
    onNavigateSummary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CounterSideEffect.ShowToast -> onShowSnackbar(sideEffect.message)
            is CounterSideEffect.NavigateToSummary -> onNavigateSummary()
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Count: ${state.count}")

        Row {
            Button(onClick = viewModel::increment) {
                Text("Increment")
            }
            Button(onClick = viewModel::decrement) {
                Text("Decrement")
            }
        }

        Button(onClick = viewModel::onFinishClicked) {
            Text("Finish")
        }
    }
}
```

---

## 4. Deterministic Unit Testing

Test MVI state transitions and side effects without running on a device:

```kotlin
package com.example.app.ui.counter

import kotlin.test.Test
import org.orbitmvi.orbit.test.test

class CounterViewModelTest {

    @Test
    fun `increment updates state and fires side effect at 10`() = runTest {
        val viewModel = CounterViewModel()

        viewModel.test(CounterState(count = 9)) {
            runIntent { increment() }
            expectState { copy(count = 10) }
            expectSideEffect(CounterSideEffect.ShowToast("Goal reached: 10!"))
        }
    }
}
```

---

## 5. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Pure Reductions**: Keep `reduce { ... }` blocks synchronous and side-effect free.
2. **One-Off Events as Side Effects**: Model transient notifications, dialogs, and navigation triggers as `postSideEffect(...)` so they don't re-fire on screen rotation or configuration changes.
3. **Decouple Navigation**: Pass navigation callbacks up from Compose instead of coupling the ViewModel directly to router classes.

### ❌ Anti-Patterns to Avoid:
1. **Never fire side effects inside `reduce`**: Doing `reduce { postSideEffect(...); state }` breaks determinism and test replay.
2. **Never expose `MutableStateFlow`**: Orbit encapsulates mutation inside `intent`. Always expose immutable state via `container.stateFlow`.
3. **Never block the UI**: Wrap asynchronous IO tasks inside `intent { val result = repository.fetch(); reduce { ... } }`.
