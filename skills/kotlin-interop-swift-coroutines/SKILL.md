---
name: kotlin-interop-swift-coroutines
description: Swift modern concurrency bridging with SKIE and KMP-NativeCoroutines, translating StateFlow to AsyncSequence and sealed classes to Swift enums.
version: 1.0.0
author: jesusdmedinac
tags:
  - swift
  - ios
  - coroutines
  - async-await
  - skie
  - interop
  - grant-winner
targets:
  - ios
triggers:
  - skie
  - swift coroutines kmp
  - async sequence kotlin
  - stateflow swiftui
  - swift interop kmp
  - native coroutines
compatibility:
  kotlin: ">=2.0.0"
  skie: ">=0.9.0"
---

# Swift Modern Concurrency & Interop with SKIE

SKIE (Touchlab) and KMP-NativeCoroutines (Rick Clephas) are the canonical Swift-Kotlin interoperability solutions, sponsored by the **Kotlin Foundation Grants Program (2023 & 2024)**. They bridge Kotlin Coroutines, `StateFlow`, and sealed class hierarchies into idiomatic Swift modern concurrency (`async/await`, `AsyncSequence`, and native `enum` pattern matching).

---

## 1. Gradle Setup & Plugin Application

SKIE operates as a Gradle compiler plugin during the Xcode framework linkage step:

### `libs.versions.toml`
```toml
[versions]
skie = "0.10.1"

[plugins]
skie = { id = "co.touchlab.skie", version.ref = "skie" }
```

In `shared/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.skie)
}
```

---

## 2. Kotlin Side: Clean Coroutines in `commonMain`

Write standard, unpolluted Kotlin Coroutines in `commonMain` — zero Swift-specific annotations required when using SKIE:

```kotlin
package com.example.app.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthStatus {
    data object Unauthenticated : AuthStatus
    data class Authenticated(val userId: String, val email: String) : AuthStatus
    data class Error(val reason: String) : AuthStatus
}

class SessionManager {
    private val _status = MutableStateFlow<AuthStatus>(AuthStatus.Unauthenticated)
    val status = _status.asStateFlow()

    suspend fun login(email: String, password: String): Boolean {
        // simulate async network call
        _status.value = AuthStatus.Authenticated("usr_123", email)
        return true
    }

    fun logout() {
        _status.value = AuthStatus.Unauthenticated
    }
}
```

---

## 3. Swift Consumption in iOS / SwiftUI

SKIE automatically generates Swift-native wrappers for `StateFlow` and sealed interfaces:

### Swift `async/await` and Exhaustive `switch`:
```swift
import SwiftUI
import Shared // Kotlin shared framework

struct SessionView: View {
    let sessionManager = SessionManager()
    @State private var currentStatus: AuthStatus = AuthStatusUnauthenticated()

    var body: some View {
        VStack {
            // SKIE translates Kotlin sealed class to native Swift enum
            switch onEnum(of: currentStatus) {
            case .unauthenticated:
                Text("Please log in")
                Button("Login") {
                    Task {
                        // Directly await Kotlin suspend function
                        _ = try? await sessionManager.login(email: "user@example.com", password: "pwd")
                    }
                }
            case .authenticated(let auth):
                Text("Welcome, \(auth.email)!")
                Button("Logout") {
                    sessionManager.logout()
                }
            case .error(let err):
                Text("Error: \(err.reason)").foregroundColor(.red)
            }
        }
        .task {
            // SKIE translates StateFlow to Swift AsyncSequence
            for await status in sessionManager.status {
                self.currentStatus = status
            }
        }
    }
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Rely on `AsyncSequence`**: Consume `StateFlow` and `SharedFlow` with `for await item in flow` inside SwiftUI `.task {}` blocks.
2. **Exhaustive Matching with `onEnum(of:)`**: Use SKIE's generated enum adapters to retain compile-time safety when new sealed subclasses are added.
3. **Structured Cancellation**: When a SwiftUI view disappears, `.task` cancels automatically, cleanly cancelling the underlying Kotlin `Job`.

### ❌ Anti-Patterns to Avoid:
1. **Never use completion handlers**: Do not export objective-C style callbacks (`completionHandler: (Result?, Error?) -> Void`).
2. **Never leak global Swift Tasks**: Avoid unbound `Task { }` calls that continue collecting Kotlin Flows after the UI view controller is dismissed.
3. **Do not pollute `commonMain`**: Do not add iOS-specific decorators or wrappers in shared code; let compiler plugins handle the translation.
