---
name: kmp-cli
description: Procedural guide for AI agents and human developers on using the KMP CLI for environment diagnostics, project scaffolding, architecture inspection, dependency analysis, and skills management.
version: 1.1.0
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
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - kmp cli
  - kmp doctor
  - diagnose environment
  - kmp describe
  - inspect kmp project
  - kmp analyze
  - kmp analyze dependencies
  - analyze dependencies
  - kmp create
  - scaffold kmp project
  - kmp skills
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

## 3. Dependency Catalog Analysis (`kmp analyze dependencies`)

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

---

## 4. Multiplatform Project Scaffolding (`kmp create`)

Generate clean, compiling, production-grade Kotlin Multiplatform project hierarchies using the official JetBrains templates gallery or an interactive step-by-step console wizard.

### Syntax:
```bash
kmp create [project-name] [flags]
```

### Flags:
- `--wizard`: Launch interactive project creation wizard in terminal.
- `--template <id>`: Scaffolding template (auto-resolved based on target options or defaults to `shared-ui`):
  - `shared-ui`: Official JetBrains Compose Multiplatform Shared UI template (Android, Desktop, iOS Compose, Web Wasm).
  - `native-ui`: Official JetBrains Native UI template (Android/Desktop Compose, iOS SwiftUI, Web React with TypeScript, Ktor Server).
  - `multiplatform-library`: Official JetBrains Multiplatform Library configured with Maven publishing.
  - `toolchain-shared-ui`: Official JetBrains Kotlin Toolchain declarative app with Compose Multiplatform.
  - `toolchain-native-ui`: Official JetBrains Kotlin Toolchain declarative app with SwiftUI.
  - `sdui-starter`: Dynamic Server-Driven UI starter powered by `json-to-compose`.
  - `fullstack`: Specialized Ktor backend server + Compose client sharing models in `:shared`.
- `--name <name>`: Project name (or specify as positional argument).
- `--package <package>`: Application package name (default: `com.example.<sanitizedName>`).
- `--build-system <gradle|toolchain>`: Build system alias.
- `--ios-ui <compose|swiftui>`: iOS UI framework (prompted in wizard when build system is Gradle).
- `--web-ui <compose|react>`: Web UI framework (prompted in wizard when build system is Gradle).
- `--targets <list>`: Comma-separated target platforms (e.g. `android,ios,desktop,wasm,server`).
- `--remote`: Fetch latest upstream template from GitHub with local fallback.
- `--output, -o <dir>`: Destination directory (default: project name).
- `--json`: Emit structured machine JSON output for AI agent automation.

### Examples:
```bash
# Launch interactive console wizard
kmp create --wizard

# Scaffold official JetBrains Shared UI application
kmp create MyMobileApp --template shared-ui --targets android,ios,desktop,wasm

# Scaffold official JetBrains Native UI application (SwiftUI + React Vite + Ktor Server)
kmp create MyNativeApp --build-system gradle --ios-ui swiftui --web-ui react --targets android,ios,web,server

# Scaffold declarative Kotlin Toolchain application
kmp create MyToolchainApp --template toolchain-shared-ui

# Scaffold a publishable multiplatform library
kmp create MyKmpLib --template multiplatform-library --package com.myorg.kmplib

# Scaffold a fullstack Ktor + Compose application
kmp create MyFullstackApp --template fullstack

# Headless execution for AI Agent automation
kmp create MyAgentApp --template multiplatform-library --json
```

Agent JSON Output:
```json
{
  "status": "SUCCESS",
  "template": "multiplatform-library",
  "projectPath": "MyAgentApp",
  "createdFiles": [
    "MyAgentApp/.gitignore",
    "MyAgentApp/settings.gradle.kts",
    "MyAgentApp/build.gradle.kts",
    "MyAgentApp/gradle/libs.versions.toml",
    "MyAgentApp/src/commonMain/kotlin/com/example/myagentapp/Platform.kt"
  ],
  "errorMessage": null
}
```

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

## 6. Core Collaboration Principles: Respect the Developer's Architecture

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

## 7. Hierarchy of Canonical Truth

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

## 8. Exit Codes
- `0`: Success (clean execution).
- `1`: Failure detected (critical tool failure, missing arguments, or invalid configuration).
