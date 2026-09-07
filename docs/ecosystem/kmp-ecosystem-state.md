# The Kotlin Multiplatform (KMP) Ecosystem: Current State & Developer Reference

* **Status**: Living Reference Document
* **Last Updated**: September 2026
* **Target Audience**: Developers, Contributors, and AI Agents

---

## 1. Executive Overview & Maturity

Kotlin Multiplatform (KMP) has transitioned from an experimental technology into an enterprise-grade standard for cross-platform software development across **Mobile (Android, iOS)**, **Desktop (macOS, Windows, Linux)**, **Web (WebAssembly/Wasm)**, and **Server (JVM/Ktor)**.

### Key Milestones
- **Nomenclature Shift (KMM $\to$ KMP)**: JetBrains deprecated "Kotlin Multiplatform Mobile" (KMM) in favor of "Kotlin Multiplatform" (KMP) to reflect its expansion across Desktop, Server, and Wasm targets.
- **Production-Ready Stability**: Production stability was achieved with Kotlin 1.9.20 and solidified with Kotlin 2.0+ and subsequent minor releases.
- **Unified K2 Compiler**: The K2 compiler frontend standardizes type inference, diagnostic checks, and smart casting across all compilation targets. Compilation times are significantly reduced, and frontend discrepancies between JVM and Native backends are resolved.
- **Modern Memory Manager**: Kotlin/Native eliminated legacy object "freezing". A concurrent, thread-safe Garbage Collector allows seamless background state sharing with `kotlinx.coroutines`.

---

## 2. The Google AndroidX & JetBrains Strategic Alliance

The inflection point for KMP adoption was Google's official endorsement and active contribution of AndroidX libraries to the multiplatform ecosystem:

| Library | Multiplatform Status | Capabilities |
| :--- | :--- | :--- |
| **`androidx.lifecycle`** | Stable KMP | Multiplatform `ViewModel`, `LifecycleOwner`, and coroutine scope management. |
| **`androidx.room`** | Stable KMP | Type-safe SQLite persistence for Android, iOS, and JVM. |
| **`androidx.navigation`** | Stable KMP | Declarative navigation graphs in Compose Multiplatform. |
| **`androidx.datastore`** | Stable KMP | Key-value and typed preferences persistence. |
| **`androidx.paging`** | Stable KMP | Reactive data pagination across platforms. |

---

## 3. Compose Multiplatform (Shared UI)

Developed by JetBrains on top of Google's Jetpack Compose:

- **Android**: Direct native execution using Android's UI toolkit.
- **iOS**: Rendered via Skia/Metal directly to native framebuffers at up to 120 FPS. Features two-way interop: embedding UIKit views via `UIKitView` and embedding Compose screens into SwiftUI via `UIViewControllerRepresentable`.
- **Desktop (macOS, Windows, Linux)**: High-performance rendering via Skia and JVM, powering applications like JetBrains Toolbox and Fleet.
- **Web (Wasm / WebAssembly)**: Kotlin/Wasm + Canvas/WebGPU represents the modern web strategy, bypassing traditional JavaScript transpilations to provide near-native UI performance in modern browsers.

---

## 4. The Modern Production Stack ("The Golden Path")

Modern KMP development standardizes around a core set of pure multiplatform libraries, eliminating the need to write custom `expect`/`actual` bridges for everyday functionality:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│    Compose Multiplatform + Decompose / Circuit + Coil 3     │
├─────────────────────────────────────────────────────────────┤
│                     Domain & State                          │
│     kotlinx.coroutines + androidx.lifecycle (ViewModel)     │
├──────────────────────────────┬──────────────────────────────┤
│          Networking          │         Persistence          │
│       Ktor Client 3.x        │    Room KMP / SQLDelight     │
│   + kotlinx.serialization    │       + DataStore KMP        │
├──────────────────────────────┴──────────────────────────────┤
│                    Dependency Injection                     │
│                 Koin / kotlin-inject (KSP)                  │
└─────────────────────────────────────────────────────────────┘
```

- **Networking**: [Ktor Client 3.x](https://ktor.io/) with platform-native engines (Darwin for iOS, OkHttp for Android, Curl/CIO for Desktop).
- **Serialization**: [`kotlinx.serialization`](https://github.com/Kotlin/kotlinx.serialization) for JSON, Protobuf, and CBOR.
- **Dependency Injection**: [Koin](https://insert-koin.io/) (reflection-free KMP) and [kotlin-inject](https://github.com/evant/kotlin-inject) (compile-time KSP dependency injection).
- **Architecture & Navigation**: [Decompose](https://github.com/arkivanov/Decompose) for UI-independent lifecycle navigation, [Circuit](https://github.com/slackhq/circuit) (Slack), and [Voyager](https://voyager.adriel.cafe/).
- **Image Loading**: [Coil 3](https://coil-kt.github.io/coil/), re-architected from the ground up as a pure 100% KMP library.
- **Swift Interoperability**: [SKIE](https://skie.touchlab.co/) by Touchlab (generates idiomatic Swift `async/await`, Combine publishers, and exhaustive sealed enums) alongside JetBrains' native **Swift Export** initiative.

---

## 5. Build Systems: Gradle vs The Kotlin Toolchain

The KMP build landscape currently features two parallel paradigms:

1. **Gradle (`build.gradle.kts` + Version Catalogs)**:
   - The established enterprise engine.
   - Powers complex Android build configurations, multi-module setups, and custom Gradle tasks.
   - High learning curve and maintenance overhead due to deep configuration syntax.
2. **The Kotlin Toolchain 0.12+ (`module.yaml`)**:
   - The official evolution of Amper into a unified CLI (`./kotlin`).
   - Declarative, minimal YAML syntax for defining dependencies, targets, and settings.
   - Features CLI-driven Compose Hot Reload and an integrated Model Context Protocol (MCP) server for hot-reload orchestration.

---

## 6. Library Indexing and Visibility on klibs.io

### What is klibs.io?
[klibs.io](https://klibs.io/) is the primary directory and discovery engine for Kotlin Multiplatform libraries.

### How to Get Listed on klibs.io:
klibs.io does not require manual registration forms; it operates an **automated indexing pipeline** based on Maven Central and GitHub:

1. **Open Source on GitHub**: The project must be public and hosted on GitHub.
2. **Published to Maven Central**: The library artifacts must be published to Maven Central (`mavenCentral()`).
3. **KMP Metadata**: The artifacts must be built with the Kotlin Multiplatform plugin, containing the standard `kotlin-tooling-metadata.json`.
4. **POM SCM Link**: The published POM metadata must include a valid GitHub link under `url` or `scm.url`.
5. **Automatic Sync**: Once these conditions are met, klibs.io automatically detects and indexes the library within its synchronization cycles. Expedited indexing can be requested via the [klibs-io/klibs-io](https://github.com/klibs-io/klibs-io) repository.

### Strategy for KMP CLI & Skills Hub:
By structuring the project with a clean core library module (`kmp-cli-core` or `kmp-engine`) that is published to Maven Central, we enable:
- Developers to embed KMP diagnostics, inspection, and skills management in their own tools and Gradle plugins.
- Automatic indexing and community discoverability on **klibs.io**.

---

## 7. The Unsolved Gap & Our Mission

Despite the maturity of the compiler and libraries, two critical friction points persist:

1. **CLI Fragmentation**: Developers must juggle separate tools (`kdoctor` for macOS mobile diagnostics, `./kotlin` for toolchain builds, web wizards for scaffolding) with no cohesive, cross-platform CLI umbrella.
2. **Absence of an Agent Skills Standard**: AI agents (Antigravity, Claude Code, Cursor) lack structured project inspection APIs and a standard catalog of vetted procedural runbooks (`SKILL.md`) for KMP architectures.

**KMP CLI & KMP Skills Hub** solves this by providing the missing unified command-line interface, MCP bridge, and open skills catalog.
