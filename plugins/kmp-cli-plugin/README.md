# KMP CLI Plugin

Unified developer tooling, diagnostics, architecture inspection, project scaffolding, and certified agent skills for Kotlin Multiplatform (KMP).

---

## Overview

The **KMP CLI Plugin** equips AI agents and human developers with high-performance tools and canonical knowledge to design, build, and troubleshoot Kotlin Multiplatform projects across Android, iOS, Desktop, Web (Wasm), and Server.

---

## Features & CLI Capabilities

### 1. Environment Diagnostics (`kmp doctor`)
Diagnose your local development setup before building or creating projects.
```bash
# Interactive checkmark report
kmp doctor

# Machine-readable JSON output for automated remediation
kmp doctor --json
```

### 2. Project Architecture Inspection (`kmp describe`)
Inspect build systems, Kotlin compiler versions, declared target platforms, and module hierarchies without manual log scraping.
```bash
# Inspect current project
kmp describe

# Output structured JSON descriptor
kmp describe path/to/project --json
```

### 3. Dependency Catalog Analysis (`kmp analyze dependencies`)
Audit version catalogs (`libs.versions.toml`), libraries, and plugins.
```bash
kmp analyze dependencies
kmp analyze dependencies --json
```

### 4. Multiplatform Scaffolding (`kmp create`)
Generate clean, compiling, production-ready Kotlin Multiplatform project hierarchies:
- **Gallery Mode (`--template <id>`)**: Deploy battle-tested starters (`shared-ui`, `native-ui`, `multiplatform-library`, `toolchain-shared-ui`, `sdui-starter`, `fullstack`).
- **Customization Mode (Zero-Bloat)**: Scaffold projects strictly tailored to requested target platforms (`--targets android,ios --ios-ui swiftui --json`). Never generates unrequested modules or build scripts.
- **Console Wizard (`--wizard`)**: Interactive terminal walkthrough for developers.

### 5. Certified Skills Hub (`kmp skills`)
Discover, inspect, and install canonical runbooks:
```bash
kmp skills list
kmp skills find compose
kmp skills add kmp-compose-adaptive
```

---

## Bundled Subagents

### `kmp-architect` (Kotlin Multiplatform Architect)
Specialist persona for multiplatform systems design, build troubleshooting, and cross-platform architecture across Android, iOS, Desktop, Web, and Server.
- Enforces hierarchical source sets (`commonMain`, `iosMain`, `androidMain`).
- Prioritizes dependency inversion and clean interface abstractions over excessive `expect/actual`.
- Safeguards Kotlin/Native memory management and structured coroutines concurrency.
- Enforces the Zero-Bloat Principle during scaffolding.

---

## Bundled Certified Skills

This plugin packages 9 canonical procedural skills:
1. `kmp-cli`: Dual human/machine operational guide for the `kmp` CLI.
2. `kotlin-concurrency-coroutines`: Structured concurrency, dispatchers, and StateFlow across KMP targets.
3. `kotlin-data-room-kmp`: Multiplatform SQLite persistence with Room KMP (androidx.room 2.7.0+).
4. `kotlin-data-sqldelight-database`: Reactive Flow queries and driver setup with SQLDelight 2+.
5. `kotlin-kmp-router`: Lightweight progressive routing tree for multiplatform navigation.
6. `kotlin-networking-ktor`: Multiplatform HTTP client architecture with Ktor and JSON serialization.
7. `kotlin-tooling-gradle-to-kotlin-toolchain`: Migration guide to declarative Kotlin Toolchain `module.yaml`.
8. `kotlin-ui-compose-adaptive`: Responsive and adaptive UI patterns for Compose Multiplatform.
9. `kotlin-ui-sdui-compose`: Server-Driven UI architecture with `json-to-compose`.

---

## Architectural Rules (Active While Enabled)

While this plugin is enabled, the agent automatically adheres to:
1. **Inspect Before Prescribing**: Inspect the developer's actual build configuration and run diagnostics before proposing code or dependency modifications.
2. **Strict Multiplatform Boundary Hygiene**: Keep business logic in `commonMain` and never import platform SDKs into common code.
3. **Zero-Bloat Scaffolding**: Never generate surplus targets or unused build modules.
4. **Three-Tier Hierarchy of Truth**: Developer's project context > Kotlin Foundation standards > KMP Skills Hub runbooks.
