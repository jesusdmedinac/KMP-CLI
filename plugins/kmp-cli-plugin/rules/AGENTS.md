# Guidelines for Kotlin Multiplatform Development (KMP CLI Plugin)

These rules are active whenever the `kmp-cli-plugin` is enabled. All agents and subagents must adhere to these directives when working on Kotlin Multiplatform codebases.

---

## 1. Inspect Before Prescribing
- Always verify environment health using `kmp doctor --json` when diagnosing build or environment issues.
- Inspect the project's build system, Kotlin compiler version, and declared target platforms using `kmp describe --json` before modifying build scripts.
- Audit declared dependencies and version catalogs using `kmp analyze dependencies --json`.
- Respect existing project patterns (e.g., if the project uses Room KMP, do not force SQLDelight; if it uses manual DI or kotlin-inject, do not force Koin).

---

## 2. Zero-Bloat Principle
- When scaffolding new projects or modules using `kmp create`, generate only the platforms explicitly requested by the user.
- Never add surplus desktop, web, or server modules that were not requested.
- For AI agent workflows, pass explicit flags (`--targets`, `--ios-ui`, `--json`) rather than launching the interactive console wizard.

---

## 3. Canonical 4-Stage Development Direction
When developing a Kotlin Multiplatform application, agents must follow the natural lifecycle stages in sequential order:
1. **Stage 1: Living Foundation (Cimientos Vivos)**:
   - Identify active targets from build configuration (`build.gradle.kts` / `module.yaml`).
   - Wire every declared target 1:1 to an executable entry point (`jvmMain/kotlin/.../main.kt`, `wasmJsMain/kotlin/.../main.kt` + `index.html`, `iosMain/kotlin/.../MainViewController.kt`, `androidMain/kotlin/.../MainActivity.kt`) connected to the root composable.
   - Verify that the project compiles and is runnable on active targets before implementing deep application features.
2. **Stage 2: Shell & Routing (Concha y Navegación)**:
   - Establish the shared theme/design tokens and the root navigation tree (e.g., Navigation 3 / Decompose).
   - Wire placeholder screens to confirm backstack and drawer/tab navigation flows across all active runners.
3. **Stage 3: Verified Capabilities & Data (Dominio y Datos)**:
   - Consult `klibs.io` (via official MCP `https://api.klibs.io/mcp` or `kmp-libraries-expert` skill) as the source of truth for library coordinates and target platform compatibility.
   - Integrate persistence (SQLDelight/Room KMP), networking (Ktor), and dependency injection cleanly in `commonMain`.
4. **Stage 4: Incremental Feature Delivery (Entrega de Features)**:
   - Implement features and user stories scenario-by-scenario with continuous verification on the live runners.

---

## 4. Strict Multiplatform Boundary Hygiene
- Core business logic, data models, repository abstractions, and view models belong in `commonMain`.
- **Never** import platform-specific SDKs into `commonMain` (e.g., `android.*`, `platform.UIKit.*`, `java.io.*`, `java.net.*`).
- Encapsulate platform-specific capabilities behind clean interface abstractions in `commonMain` and provide concrete implementations in target source sets (`androidMain`, `iosMain`, `desktopMain`, `wasmJsMain`).
- Prioritize dependency inversion and interfaces over excessive `expect/actual` declarations.

---

## 5. Structured Concurrency & Coroutines
- Never use `GlobalScope` or blocking dispatchers (`Dispatchers.IO` is only available on supported JVM/Native targets; use `Dispatchers.Default` for CPU-bound computations and `Dispatchers.Main` for UI state updates).
- Maintain immutable UI state exposure via `StateFlow` and handle lifecycle-aware collection in UI layers.

---

## 6. Three-Tier Hierarchy of Canonical Truth
1. **Tier 1: Contextual Truth**: The developer's real project files (`build.gradle.kts` / `module.yaml`, active dependencies, declared targets).
2. **Tier 2: Canonical Authority**: The Kotlin Foundation ecosystem (JetBrains Kotlin/Native, Compose Multiplatform docs, klibs.io catalog, Google AndroidX KMP, Touchlab, CashApp).
3. **Tier 3: Procedural Operational Runbooks**: Certified recipes from the KMP Skills Hub (`kmp-cli`, `kotlin-concurrency-coroutines`, `kotlin-data-room-kmp`, `kotlin-data-sqldelight-database`, `kotlin-kmp-router`, `kotlin-networking-ktor`, `kotlin-tooling-gradle-to-kotlin-toolchain`, `kotlin-ui-compose-adaptive`, `kotlin-ui-sdui-compose`).
