---
name: kmp-cli
description: Procedural guide for AI agents and human developers on using the KMP CLI for environment diagnostics, project scaffolding, architecture inspection, dependency analysis, and skills management.
version: 1.2.0
author: jesusdmedinac
tags:
  - cli
  - tooling
  - doctor
  - skills
  - diagnostics
  - describe
  - analyze
  - create
  - scaffolding
  - mcp
  - model-context-protocol
  - libraries
  - klibs
  - architecture-lint
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - kmp cli
  - kmp doctor
  - diagnose environment
  - kmp doctor --architecture
  - architecture doctor
  - living foundation check
  - kmp describe
  - inspect kmp project
  - kmp analyze
  - kmp analyze dependencies
  - analyze dependencies
  - kmp analyze libraries
  - search kmp libraries
  - klibs
  - kmp create
  - scaffold kmp project
  - kmp skills
  - kmp mcp
  - mcp server
  - model context protocol
compatibility:
  kotlin: ">=2.0.0"
---

# KMP CLI Developer & Agent Guide

The Kotlin Multiplatform CLI (`kmp`) provides unified environment diagnostics, multiplatform project scaffolding, architecture inspection, dependency analysis, and agent skill management for Kotlin Multiplatform projects.

---

## 1. Environment Diagnostics (`kmp doctor`)

Run diagnostics before compiling, creating, or inspecting projects to ensure host prerequisites are met.

### Interactive Mode:
```bash
kmp doctor
```
Displays ANSI-formatted checkmarks (`✓`), warnings (`!`), and failures (`✗`) along with actionable remediation commands.

### Agent / Machine Mode (`--json`):
```bash
kmp doctor --json
```
Emits a structured JSON report adhering to `DiagnosticReport`:
- `overallStatus`: `"SUCCESS"`, `"WARNING"`, or `"FAILURE"`.
- `isHealthy`: Boolean (`false` if any critical failure exists).
- `checks`: Array of check items (`kdoctor`, `jdk`, `kotlinToolchain`), each containing `id`, `title`, `status`, `message`, `details`, and `remediation`.
- If `remediation` is present, execute `remediation.command` to automatically resolve the environment issue.

### 1.1 Architecture & Stage 1 Living Foundation Check (`--architecture`):
```bash
# Run architecture boundary and entry-point audit
kmp doctor --architecture

# Machine-readable architecture audit for CI or AI agents
kmp doctor --architecture --json
```
Verifies two core health dimensions without building the full project:
1. **Living Foundation Entry Points**:
   - Ensures each declared target platform has a valid entry point (`MainActivity.kt` for Android, `MainViewController.kt` for iOS, `jvmMain/main.kt` for Desktop, `wasmJsMain/main.kt` for Web).
2. **Clean Architecture Boundary Hygiene**:
   - Inspects `:core:domain` or `commonMain` source files.
   - Flags forbidden platform/UI imports (e.g., `android.*`, `platform.UIKit.*`, `androidx.compose.*`) inside pure domain layers.
   - Provides concrete remediation commands if boundaries are violated.

---

## 2. Project Architecture Inspection (`kmp describe`)

Inspect and understand an existing Kotlin Multiplatform project's build system, Kotlin version, declared target platforms, and modules without manual log scraping.

### Interactive Mode:
```bash
# Inspect project in current directory
kmp describe

# Inspect project in specific directory
kmp describe path/to/project
```
Outputs project name, build system (`Gradle` or `Kotlin Toolchain`), Kotlin version, targets (`android`, `iosArm64`, `jvm`, `wasmJs`), and module hierarchies.

### Agent / Machine Mode (`--json`):
```bash
kmp describe --json
kmp describe path/to/project --json
```
Emits a structured JSON payload adhering to `ProjectDescriptor`:
```json
{
  "name": "MyAwesomeApp",
  "buildSystem": "GRADLE",
  "kotlinVersion": "2.2.20",
  "targets": ["android", "iosArm64", "iosX64", "jvm"],
  "modules": [
    {
      "name": "shared",
      "path": "shared",
      "targets": ["android", "iosArm64", "iosX64", "jvm"]
    }
  ]
}
```

---

## 3. Dependency & Ecosystem Analysis (`kmp analyze`)

Audit declared version catalogs or discover multiplatform-compatible libraries from the official ecosystem.

### 3.1 Version Catalog Analysis (`kmp analyze dependencies`)

Analyze declared libraries, plugins, and version catalogs (`libs.versions.toml`) to audit dependency hygiene or inspect project capabilities.

### Interactive Mode:
```bash
# Analyze version catalog in current project (default: gradle/libs.versions.toml)
kmp analyze dependencies

# Analyze version catalog in specific project root
kmp analyze dependencies path/to/project

# Analyze a direct .toml file
kmp analyze dependencies gradle/libs.versions.toml
kmp analyze dependencies custom-libs.toml
```

### Agent / Machine Mode (`--json`):
```bash
kmp analyze dependencies --json
kmp analyze dependencies custom-libs.toml --json
```
Emits a structured JSON payload adhering to `ProjectDependencies`:
```json
{
  "versions": {
    "kotlin": "2.2.0",
    "compose-multiplatform": "1.8.0",
    "ktor": "3.1.1"
  },
  "libraries": {
    "ktor-client-core": {
      "module": "io.ktor:ktor-client-core",
      "version": "3.1.1"
    }
  },
  "plugins": {
    "kotlinMultiplatform": {
      "id": "org.jetbrains.kotlin.multiplatform",
      "version": "2.2.0"
    }
  }
}
```

### 3.2 Multiplatform Library Ecosystem Analysis (`kmp analyze libraries`)

Search and verify Kotlin Multiplatform libraries against the official `klibs.io` catalog to confirm platform compatibility, coordinates, and licensing before adding them to projects.

#### Interactive Mode:
```bash
# Search for libraries matching a query
kmp analyze libraries ktor

# Filter search by target platforms
kmp analyze libraries "sql" --targets android,ios
```
Displays an ANSI table with library coordinates, latest release, target platform support badges (`Android`, `iOS`, `Desktop`, `Wasm`), and licenses.

#### Agent / Machine Mode (`--json`):
```bash
kmp analyze libraries ktor --json
kmp analyze libraries "sql" --targets android,ios --json
```
Emits a structured JSON payload adhering to `LibrarySearchResult`:
```json
{
  "query": "ktor",
  "totalResults": 1,
  "libraries": [
    {
      "group": "io.ktor",
      "name": "ktor-client-core",
      "version": "3.1.1",
      "description": "Ktor Multiplatform HTTP client",
      "targets": ["android", "iosArm64", "iosX64", "jvm", "wasmJs"],
      "license": "Apache-2.0"
    }
  ]
}
```

---

## 4. Multiplatform Project Scaffolding (`kmp create`)

Generate clean, compiling, production-grade Kotlin Multiplatform project hierarchies using the official JetBrains templates gallery, composable custom scaffolding, or an interactive step-by-step console wizard.

### Scaffolding Paradigms:

1. **Gallery Mode / Immutable Templates (`--template <id>`)**:
   - Deploys an exact, battle-tested starter from the templates gallery.
   - Ideal when the user asks for a specific known template as-is (e.g. "Use the official Shared UI template" or "Scaffold the SDUI starter").
   - **Important**: Multi-module gallery templates (like `native-ui` or `fullstack`) are pre-packaged reference architectures. Do not use gallery mode when the user requests a custom subset of platforms.

2. **Customization Mode / Composable Scaffolding (Flags without `--template`)**:
   - Tailors the project strictly to the declared target platforms and UI layers.
   - Enforces the **Zero-Bloat Principle**: Never generate modules, targets, or package managers (such as Web Vite or Desktop JVM) that were not requested.
   - Automatically prunes inactive target modules from `settings.gradle.kts` and build scripts.

3. **Interactive Console Wizard (`--wizard`)**:
   - Step-by-step interactive terminal walkthrough for human developers.
   - Prompts individually for each target platform, build system, and UI framework.
   - **Note for AI Agents**: The wizard is interactive via stdin (`readlnOrNull`). For automated AI execution, use **Customization Mode** with `--targets`, `--ios-ui`, and `--json`.

### Syntax:
```bash
kmp create [project-name] [flags]
```

### Flags:
- `--wizard`: Launch interactive project creation wizard in terminal (interactive humans only).
- `--template <id>`: Immutable gallery template:
  - `shared-ui`: Official JetBrains Compose Multiplatform Shared UI template (Android, Desktop, iOS Compose, Web Wasm).
  - `native-ui`: Official JetBrains Native UI template (Android/Desktop Compose, iOS SwiftUI, Web React with TypeScript, Ktor Server).
  - `multiplatform-library`: Official JetBrains Multiplatform Library configured with Maven publishing.
  - `toolchain-shared-ui`: Official JetBrains Kotlin Toolchain declarative app with Compose Multiplatform.
  - `toolchain-native-ui`: Official JetBrains Kotlin Toolchain declarative app with SwiftUI.
  - `sdui-starter`: Dynamic Server-Driven UI starter powered by `json-to-compose`.
  - `fullstack`: Specialized Ktor backend server + Compose client sharing models in `:shared`.
  - `kmp-shared-core-bridge`: Lightweight KMP library configured for ingestion by existing native/hybrid shells (SPM/CocoaPods & Maven/AAR).
- `--name <name>`: Project name (or specify as positional argument).
- `--package <package>`: Application package name (default: `com.example.<sanitizedName>`).
- `--build-system <gradle|toolchain>`: Build system alias.
- `--ios-ui <compose|swiftui>`: iOS UI framework (Compose Multiplatform or native SwiftUI).
- `--web-ui <compose|react>`: Web UI framework (Compose Multiplatform Wasm or React Vite).
- `--targets <list>`: Comma-separated target platforms (`android`, `ios`, `desktop`, `wasm`, `server`). In Customization Mode, only declared targets are generated.
- `--remote`: Fetch latest upstream template from GitHub with local fallback.
- `--output, -o <dir>`: Destination directory (default: project name).
- `--json`: Emit structured machine JSON output for AI agent automation.

### Examples:
```bash
# Human Developer: Interactive console wizard
kmp create --wizard

# Gallery Template: Scaffold official JetBrains Shared UI application
kmp create MyMobileApp --template shared-ui

# Composable Customization: Mobile-only app with native SwiftUI on iOS (Zero-Bloat: no desktop, no web)
kmp create MyMobileApp --targets android,ios --ios-ui swiftui --json

# Composable Fullstack: Mobile + Ktor Server (Zero-Bloat: no desktop, no web)
kmp create MyFullstackApp --targets android,ios,server --ios-ui swiftui --json

# Scaffold a publishable multiplatform library
kmp create MyKmpLib --template multiplatform-library --package com.myorg.kmplib

# Headless execution for AI Agent automation
kmp create MyAgentApp --template multiplatform-library --json
```

Agent JSON Output:
```json
{
  "status": "SUCCESS",
  "template": "native-ui",
  "projectPath": "MyAgentApp",
  "createdFiles": [
    "MyAgentApp/.gitignore",
    "MyAgentApp/settings.gradle.kts",
    "MyAgentApp/build.gradle.kts",
    "MyAgentApp/gradle/libs.versions.toml"
  ],
  "errorMessage": null
}
```

---

## 4.1. Agent Scaffolding Protocol & Decision Tree

When an AI coding agent is tasked with creating or scaffolding a Kotlin Multiplatform project, it must follow this protocol:

```
                      User requests new KMP project
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
Did user request an explicit                      Did user specify custom
gallery template by name?                         requirements (targets, UI, server)?
(e.g., "Usa el template native-ui")               (e.g., "Android y iOS con SwiftUI")
           │                                                 │
           ▼                                                 ▼
   [Gallery Mode]                                   [Customization Mode]
Pass `--template <id>`                            Omit `--template`.
Deploy template as-is.                            Pass `--targets`, `--ios-ui`.
Do not mutate or prune.                           Enforce Zero-Bloat: do NOT emit
                                                  unrequested modules.
                                                             │
                                                             ▼
                                                  Need extra capabilities?
                                                  (Networking, DB, Navigation)
                                                             │
                                                             ▼
                                                  Consult KMP Skills Hub!
                                                  Apply recipes from:
                                                  - `kmp-ktor-networking`
                                                  - `kmp-sqldelight-database`
                                                  - `kmp-compose-adaptive`
```

### Critical Rules for AI Agents:
1. **Never Assume Surplus Platforms**: If the user asks for Android and iOS, never generate Desktop or Web modules.
2. **Server Separation**: If the user requests fullstack with a Ktor server, include the `:server` module, but keep the mobile clients clean.
3. **Skills are Construction Blueprints**: When the user asks for networking, local databases, or dependency injection, do not guess configurations—use the certified recipes documented in the respective `skills/<skill-id>/SKILL.md`.

---

## 5. Managing Agent Skills (`kmp skills`)

Discover and install certified procedural skills formatted as `SKILL.md`:

```bash
# List all certified skills in the catalog
kmp skills list

# Search for skills matching keywords
kmp skills find compose

# Inspect detailed instructions and metadata
kmp skills describe kmp-compose-adaptive

# Install skill into active project (.agents/skills/<id>/)
kmp skills add kmp-compose-adaptive

# Install skill globally (~/.kmp/skills/<id>/)
kmp skills add kmp-compose-adaptive --global
```

---

## 6. Native Model Context Protocol (MCP) Server (`kmp mcp`)

The `kmp` CLI provides a native, standalone Stdio Model Context Protocol (MCP) server (JSON-RPC 2.0) conforming to protocol version `2024-11-05`. This allows LLMs, IDE agents (Claude Desktop, Cursor, Google Antigravity, Windsurf), and autonomous coding pipelines to call `kmp` capabilities as native tools with structured JSON parameters.

### 6.1 Starting the MCP Server
```bash
# Launch stdio MCP server directly
kmp mcp
```

### 6.2 Host Client Configurations

#### Claude Desktop (`claude_desktop_config.json`):
```json
{
  "mcpServers": {
    "kmp-cli": {
      "command": "kmp",
      "args": ["mcp"]
    }
  }
}
```

#### Antigravity / Agent Runtimes:
```json
{
  "command": "/path/to/kmp",
  "args": ["mcp"]
}
```

### 6.3 Exposed MCP Tools (9)

| Tool Name | Purpose | Parameters | Returns |
| :--- | :--- | :--- | :--- |
| `kmp_doctor` | Environment diagnostics (JDK, Xcode via `kdoctor`, Toolchain 0.12) | `includeKdoctor` (boolean), `projectPath` (string) | `DiagnosticReport` JSON |
| `kmp_doctor_architecture` | Clean Architecture guard and Stage 1 Living Foundation checks | `projectPath` (string) | Architecture audit `DiagnosticReport` JSON |
| `kmp_describe` | Inspects project architecture, targets, build system, and modules | `projectPath` (string) | `ProjectDescriptor` JSON |
| `kmp_analyze_dependencies` | Audits version catalog (`libs.versions.toml`) dependencies and plugins | `catalogPath` (string), `projectPath` (string) | `ProjectDependencies` JSON |
| `kmp_analyze_libraries` | Queries klibs.io for target compatibility, coordinates, and license | `query` (string, required), `targets` (array) | `LibrarySearchResult` JSON |
| `kmp_list_skills` | Discovers available procedural skills in the KMP Skills Hub | None | Skills catalog list JSON |
| `kmp_add_skill` | Installs an agent skill locally into `.agents/skills/` or globally | `skillId` (string, required), `global` (boolean) | Installation result JSON |
| `kmp_migrate_audit` | Audit React Native or Flutter codebases for native bridge migration | `path` (string) | `MigrationAuditReport` JSON |
| `kmp_migrate_plan` | Generate phased Strangler Fig migration roadmap to KMP | `path` (string), `pathway` (string) | `MigrationPlan` JSON |

---

## 7. Cross-Platform Modernization & Migration (`kmp migrate`)

The `migrate` command family audits legacy mobile applications (React Native, Flutter) and generates incremental Strangler Fig migration pathways to Kotlin Multiplatform.

### 7.1 Modernization Audit (`kmp migrate audit`)
```bash
# Human readable TUI with replacement matrix and risk indicators
kmp migrate audit

# Machine-readable JSON output for AI agent orchestration
kmp migrate audit --json
```

Analyzes:
- Framework versions and architectures (Old Bridge vs TurboModules, Flutter Platform Channels).
- Third-party packages requiring native bridges (AsyncStorage, Axios, Redux, React Navigation, SQLite, Firebase, etc.).
- Replacement matrix mapping each legacy dependency to its certified KMP equivalent and skill.
- Architectural pathway recommendation: `DUAL_NATIVE`, `NATIVE_UI_SHARED_CORE`, or `COMPOSE_MULTIPLATFORM`.

### 7.2 Phased Strangler Fig Migration Roadmap (`kmp migrate plan`)
```bash
# Generate 4-phase Strangler Fig roadmap
kmp migrate plan

# Override pathway recommendation
kmp migrate plan --pathway native-ui-shared-core --json
```

Produces a structured 4-phase progression:
1. **Phase 1: Foundation & Core Bridge**: Shared KMP module skeleton, CocoaPods/SPM & Gradle AAR export, unified network/auth.
2. **Phase 2: Domain & Data Strangler**: Offline persistence (Room KMP / SQLDelight), Store5 caching, state flow observation.
3. **Phase 3: UI & Screen Incremental**: Replace high-friction screens with Compose Multiplatform or native UI.
4. **Phase 4: Legacy Shell Deletion**: Remove JS/Dart runtime engines, delete bridge pods, finalize standalone multiplatform binaries.

### 7.3 Shared Core Bridge Scaffolding
```bash
kmp create MyBridge --template kmp-shared-core-bridge
```
Scaffolds a lightweight KMP library configured for immediate consumption by existing native/hybrid shells (SPM/CocoaPods export, Android AAR publishing, SKIE coroutine bridge, and coroutine dispatchers).

---

## 8. Core Collaboration Principles: Respect the Developer's Architecture

When an AI coding agent collaborates with a human developer on a Kotlin Multiplatform codebase, it must act as a contextually aware pair programmer rather than an opinionated dictator:

1. **Inspect Before Prescribing**:
   - Always inspect the project's actual build configuration (`build.gradle.kts` / `module.yaml`) and active dependencies before suggesting changes.
   - Use `kmp doctor` to verify environment prerequisites and `kmp describe` / `kmp analyze` to understand active targets and existing modules.
2. **Never Impose Dogmatic Stacks**:
   - **Persistence**: If the developer chose Room KMP, do not force SQLDelight (and vice versa).
   - **Dependency Injection**: If the project uses manual constructor injection, kotlin-inject, or Kodein, respect that pattern instead of forcing Koin.
   - **Networking & Server**: If the multiplatform system integrates with a Spring Boot, Micronaut, or gRPC backend, adapt the shared code accordingly instead of forcing Ktor Server.
3. **Strict Multiplatform Boundary Hygiene**:
   - Core domain models and business logic belong in `commonMain`.
   - Never import platform-specific SDKs (e.g. `android.content.Context`, `java.io.File`, `platform.UIKit.*`) into `commonMain`.
   - Encapsulate platform differences behind interfaces, `expect/actual` declarations, or specialized target source sets (`androidMain`, `iosMain`, `desktopMain`, `wasmJsMain`).

---

## 9. Hierarchy of Canonical Truth

To avoid hallucinations, outdated conventions, and fragile code, AI agents must adhere to the following **Three-Tier Hierarchy of Truth**:

```
┌─────────────────────────────────────────────────────────────┐
│  Tier 1: Contextual Truth (The Developer's Real Project)     │
│  build.gradle.kts / module.yaml / kmp doctor / Active Targets│
├─────────────────────────────────────────────────────────────┤
│  Tier 2: Canonical Authority (Kotlin Foundation & Platforms)│
│  Kotlin Foundation Ecosystem + Official Target Docs         │
├─────────────────────────────────────────────────────────────┤
│  Tier 3: Procedural Operational Runbooks (KMP Skills Hub)   │
│  skills/<id>/SKILL.md (Boundaries, recipes, common pitfalls) │
└─────────────────────────────────────────────────────────────┘
```

### The Kotlin Foundation Ecosystem (Canonical Authority)
The [Kotlin Foundation](https://kotlinfoundation.org/) coordinates the evolution, quality, and standards of the Kotlin ecosystem. Agents should consult and defer to documentation authored by its member organizations:
- **Founding Members**:
  - **JetBrains** ([kotlinlang.org/multiplatform](https://kotlinlang.org/docs/multiplatform.html)): Core language specification, K2 compiler, Kotlin/Native memory model, Compose Multiplatform.
  - **Google** ([developer.android.com](https://developer.android.com/)): AndroidX KMP libraries (`androidx.lifecycle`, `androidx.room`, `androidx.navigation`, `androidx.datastore`).
- **Gold Members**:
  - **Meta**: Enterprise Kotlin adoption and tooling optimizations.
- **Silver Members**:
  - **Gradle** ([gradle.org](https://docs.gradle.org/)): Multiplatform build tool conventions, JVM execution, and configuration cache.
  - **Touchlab** ([touchlab.co](https://touchlab.co/)): Touchlab SKIE, Swift export tooling, and Kotlin/Native interoperability.
  - **Uber**: Large-scale multiplatform mobile architecture.
  - **Kotzilla** ([insert-koin.io](https://insert-koin.io/)): Koin dependency injection and multiplatform state management.
  - **Block** ([developer.squareup.com](https://developer.squareup.com/)): CashApp open-source libraries (SQLDelight, Turbine, Molecule).

---

## 10. Exit Codes
- `0`: Success (clean execution).
- `1`: Failure detected (critical tool failure, missing arguments, or invalid configuration).
