---
name: kmp-ktor-networking
description: Multiplatform HTTP client architecture with Ktor, native engine selection, and JSON serialization.
version: 1.0.0
author: jesusdmedinac
tags:
  - ktor
  - networking
  - http
  - serialization
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - ktor client
  - network request
  - http client multiplatform
compatibility:
  kotlin: ">=2.0.0"
  gradle: ">=8.5"
---

# Ktor Multiplatform Networking

This skill guides the design and implementation of a robust, cross-platform HTTP client using Ktor 3+.

## 1. Engine Selection per Target

Never hardcode an engine inside `commonMain`. Configure the expected engine using target-specific source sets or platform factories:

- **Android**: `OkHttp` or `Android` engine (supports HTTP/2 and system proxy).
- **iOS / macOS**: `Darwin` engine (uses `NSURLSession`, supports iOS background downloads and SSL pinning).
- **Desktop (JVM)**: `OkHttp` or `CIO` (Coroutine-based I/O).
- **Wasm / JS**: `Js` engine (leverages browser `fetch()` API).

```kotlin
// commonMain
expect fun createHttpClientEngine(): HttpClientEngine

fun createHttpClient(): HttpClient = HttpClient(createHttpClientEngine()) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.INFO
    }
}
```

## 2. Target Implementations

```kotlin
// iosMain
actual fun createHttpClientEngine(): HttpClientEngine = Darwin.create {
    configureRequest {
        setAllowsCellularAccess(true)
    }
}

// androidMain
actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()

// jvmMain
actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()

// wasmJsMain
actual fun createHttpClientEngine(): HttpClientEngine = Js.create()
```

## 3. Error Handling and Resilience

- Catch `HttpRequestTimeoutException` and `ResponseException` distinctly from general IO errors.
- Do not let raw network exceptions bubble unhandled into UI components. Map them into typed domain `Result<T>` or `NetworkResult<T>` models in the repository layer.
