---
name: kmp-cli
description: Procedural guide for AI agents and human developers on using the KMP CLI for environment diagnostics, skills management, and project analysis.
version: 1.0.0
author: jesusdmedinac
tags:
  - cli
  - tooling
  - doctor
  - skills
  - diagnostics
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - kmp cli
  - kmp doctor
  - diagnose environment
  - kmp skills
compatibility:
  kotlin: ">=2.0.0"
---

# KMP CLI Developer & Agent Guide

The Kotlin Multiplatform CLI (`kmp`) provides unified environment diagnostics, agent skill management, and project inspection for Kotlin Multiplatform projects.

## 1. Environment Diagnostics (`kmp doctor`)

Run diagnostics before compiling or creating projects to ensure host prerequisites are met.

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

## 2. Managing Agent Skills (`kmp skills`)

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

## 3. Core Collaboration Principles: Respect the Developer's Architecture

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

## 4. Hierarchy of Canonical Truth

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

### Platform-Specific Canonical Documentation
When implementing target-specific bridges in `iosMain`, `desktopMain`, or `wasmJsMain`:
- **Apple / iOS / macOS**: [Apple Developer Documentation](https://developer.apple.com/) (Swift interop, ARC memory semantics, UIKit/SwiftUI bridging).
- **Web & WebAssembly**: [MDN Web Docs](https://developer.mozilla.org/) and [WebAssembly.org](https://webassembly.org/) (DOM, Canvas, WebGPU APIs).
- **Desktop (macOS, Windows, Linux)**: Platform OS APIs and Java Desktop/Skia guidelines.

## 5. Exit Codes
- `0`: Success or warnings (environment is operable).
- `1`: Critical failure detected in required tools.

