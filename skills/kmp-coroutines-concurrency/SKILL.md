---
name: kmp-coroutines-concurrency
description: Best practices for structured concurrency, background dispatchers, and StateFlow management across KMP targets.
version: 1.0.0
author: jesusdmedinac
tags:
  - coroutines
  - concurrency
  - flows
  - async
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - kotlin coroutines
  - multiplatform concurrency
  - dispatchers
compatibility:
  kotlin: ">=2.0.0"
---

# Structured Concurrency & Coroutines in Kotlin Multiplatform

Kotlin/Native operates with the modern memory manager (relaxed memory model, free sharing of frozen/unfrozen objects between threads). However, multiplatform concurrency still requires adherence to strict rules.

## 1. Dispatcher Conventions

- **Main Thread**: Always use `Dispatchers.Main` for UI state updates. On Android, it routes to `Looper.getMainLooper()`; on iOS, it routes to Grand Central Dispatch `dispatch_get_main_queue()`.
- **Background Work**: Use `Dispatchers.Default` for CPU-bound computations across all platforms.
- **I/O Work**:
  - JVM and Android support `Dispatchers.IO`.
  - In `commonMain`, `Dispatchers.IO` is available via `kotlinx-coroutines-core` 1.7+. If targeting JavaScript or single-threaded runtimes without IO thread pools, use `Dispatchers.Default` as fallback.

## 2. CoroutineScope Lifecycle Ownership

Never use `GlobalScope`. Always bind coroutines to a controlled lifecycle:
- In ViewModel / Presenter: Bind to `CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)`.
- Cancel the scope when the UI screen or component is detached/disposed.
- Propagate `CancellationException`: Never catch `Throwable` or `Exception` without rethrowing `CancellationException`, as doing so breaks structured cancellation.

```kotlin
try {
    performNetworkSync()
} catch (e: CancellationException) {
    throw e // Always rethrow!
} catch (e: Exception) {
    handleError(e)
}
```

## 3. iOS Swift Interop with Flow

- Kotlin `StateFlow` and `SharedFlow` can be observed in Swift via `SKIE` or `KMP-NativeCoroutines` to generate Swift `AsyncSequence` and `@Published` properties.
- Expose read-only `asStateFlow()` or `StateFlow<T>` from view models rather than mutable instances.
