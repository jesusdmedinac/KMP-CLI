# ADR-001: Modular Engine (`kmp-core`) and CLI Application (`kmp-cli`) Architecture

* **Status**: Accepted
* **Date**: September 2026
* **Deciders**: Jesus Daniel Medina Cruz & Antigravity AI Pair
* **Consulted**: KMP Community Standards, Detekt & Ktlint precedents, JetBrains KDoctor analysis

---

## 1. Context and Problem Statement

When building command-line developer tools, a common architectural temptation is to build a monolithic CLI application where command-line parsing, terminal formatting, and domain business logic are tightly coupled in a single module.

In the Kotlin Multiplatform (KMP) ecosystem, JetBrains' official diagnostic tool, **KDoctor**, adopted this monolithic pattern. Consequently, KDoctor's diagnostic logic cannot be consumed programmatically by IDE plugins (IntelliJ IDEA, Android Studio, Fleet), Gradle build tasks, or AI agents without spawning OS sub-processes and scraping ANSI terminal strings.

Furthermore, **klibs.io** (the official KMP library registry) only indexes libraries published to Maven Central, not standalone CLI binaries.

We must decide whether to structure the project as a single monolithic CLI executable or decouple the core logic into a reusable KMP library (`kmp-core`) and a thin CLI runner (`kmp-cli`).

---

## 2. Decision

We will adopt a **two-module architecture**:

```text
kmp/
├── kmp-core/      # Pure KMP Engine (Library published to Maven Central & indexed on klibs.io)
│                  # - Data models & DTOs (Diagnostics, Targets, Skills, Metadata)
│                  # - Diagnostic checks (JDK, Android SDK, Xcode, Toolchain 0.12, Wasm)
│                  # - Project inspectors (Gradle build.gradle.kts & Toolchain module.yaml)
│                  # - Skills manager (SKILL.md parsing, validation, resolution)
│                  # - Testable business logic with zero CLI/terminal dependencies
│
└── kmp-cli/       # Executable Terminal Application (Native binary distributed via Homebrew)
                   # - Clikt command-line parsing & option delegates
                   # - Mordant terminal UI (ANSI styling, progress animations, tables)
                   # - Deterministic --json serialization for AI agents
                   # - Standalone native compilation (macOS arm64/x64, Linux, Windows)
```

---

## 3. Rationale and Benefits

### 3.1. Industry Precedents (Detekt & Ktlint)
The most successful developer tooling in the Kotlin ecosystem follows this decoupled pattern:
- **Detekt**: Maintained as `detekt-core` / `detekt-api` (the engine) and `detekt-cli` (the terminal runner), allowing the Gradle plugin and IntelliJ plugins to consume rules natively.
- **Ktlint**: Structured as `ktlint-rule-engine` and `ktlint-cli`.

### 3.2. Multi-Channel Consumption
Decoupling the engine enables consumption beyond the terminal:
1. **CI/CD via Gradle Plugin**: Teams can execute `./gradlew kmpDoctor` or `./gradlew kmpAnalyze` without requiring the standalone CLI installed on their runners.
2. **IDE Integration**: IntelliJ IDEA / Android Studio / Fleet plugins can import `kmp-core` directly to display diagnostic badges and action buttons.
3. **Model Context Protocol (MCP)**: AI coding agents (Antigravity, Claude Code, Cursor) can query `kmp-core` services directly.
4. **Third-Party Plugins**: Plugin authors compiling extensions (e.g. `kmp-firebase`) can compile against `kmp-core` data models.

### 3.3. Pure Test-Driven Development (TDD)
Isolating `kmp-core` removes all CLI harness boilerplate. Diagnostic checks and parsers can be tested with standard Kotlin unit tests against memory fakes without mocking `System.out`, terminal streams, or process exit codes.

### 3.4. Community Discoverability via klibs.io
By publishing `kmp-core` to Maven Central with standard Kotlin Multiplatform metadata (`kotlin-tooling-metadata.json`) and GitHub SCM links, the project will be automatically indexed on **klibs.io**.

---

## 4. Consequences and Mitigations

### 4.1. Positive Consequences
- Clean separation of concerns (UI/terminal presentation vs. business logic).
- Rapid unit testing without native compilation overhead.
- Clear extensibility surface for community contributors.

### 4.2. Negative Consequences & Mitigations
- **Slightly higher initial Gradle setup**: Mitigated by configuring a clean multi-module Gradle project from Day 1 using modern Gradle convention plugins.
- **Risk of premature over-modularization**: Mitigated by strictly constraining the architecture to **only two modules** (`kmp-core` and `kmp-cli`) during Phases 1–4. Additional modules (such as `kmp-gradle-plugin`, IDE integrations, or web portals) are explicitly scheduled under **Phase 5 (Ecosystem Expansions & Future Outcomes)**.
