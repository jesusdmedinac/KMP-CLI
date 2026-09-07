# RFC-001: Vision, Architecture, and Roadmap for KMP CLI and KMP Skills Hub

* **Status**: Proposed (Phase 0)
* **Date**: September 2026
* **Authors**: Jesus Daniel Medina Cruz & Antigravity AI Pair

---

## 1. Executive Summary and Motivation

The **Kotlin Multiplatform (KMP)** ecosystem has reached production maturity for sharing business logic, multiplatform UI with Compose Multiplatform, and lightweight backends with Ktor. Recent ecosystem advancements have accelerated this transition:
- **JetBrains Kotlin Toolchain (v0.12.0)**: JetBrains has evolved Amper into the official unified **Kotlin Toolchain** (`./kotlin`), introducing declarative project configuration (`module.yaml`), Compose Hot Reload, multiplatform library publishing, and early Model Context Protocol (MCP) support for AI pairs.
- **JetBrains KDoctor**: JetBrains maintains `kdoctor` as a dedicated mobile environment checker (Java, Android Studio, Xcode, CocoaPods).

Despite these tools, the developer experience (DX) and AI collaboration experience remain fragmented:
1. **No Unified CLI with AI-Native Diagnostics**: `kdoctor` is a single-purpose terminal tool without JSON output (`--json`), limited to mobile macOS setups, and unaware of the new Kotlin Toolchain (`module.yaml`), Wasm web targets, Desktop prerequisites, or agent skill configurations.
2. **Missing Agent Skills Hub**: While JetBrains acknowledges that AI agents are instrumental for tasks like project migration and modernization, there is no open package manager or curated repository for reusable **KMP Skills** (`SKILL.md`) covering Compose Multiplatform, Ktor, SQLDelight, Room KMP, Firebase, and migration playbooks.
3. **Dual Build-System Reality**: Real-world projects navigate between established Gradle setups (`build.gradle.kts` + version catalogs) and declarative Kotlin Toolchain setups (`module.yaml`). Tooling must understand both seamlessly.

Inspired by **Android CLI** and the **Android Skills** ecosystem, this project establishes:
1. **KMP CLI (`kmp`)**: A high-performance, native command-line interface and MCP bridge that unifies environment diagnostics (orchestrating and expanding `kdoctor`), project analysis, and scaffolding for both humans and AI agents.
2. **KMP Skills Hub**: An open, curated catalog of procedural skills adhering to the open `SKILL.md` standard for developer and AI pair consumption.

---

## 2. Design Principles

### 2.1. Dual Experience: Humans and AI Agents
- **For Humans**: A rich, interactive terminal UI (ANSI color coding, progress spinners, interactive prompts, and readable tables) powered by Clikt and Mordant.
- **For AI Agents**:
  - Global `--json` flag on all commands providing deterministic, schema-validated payloads.
  - Dedicated inspection commands (`kmp describe`, `kmp analyze`) parsing project structure without fragile log scraping.
  - Native **Model Context Protocol (MCP)** server integration, allowing AI agents (Antigravity, Claude Code, Cursor, Copilot) to query diagnostics, inspect projects, and install skills natively as tools.

### 2.2. Facade & Orchestration Architecture
- **Embrace and Augment Upstream Tools**:
  - `kmp doctor` does not reinvent diagnostics from scratch. If `kdoctor` is installed on macOS, `kmp doctor` invokes it, normalizes its output into structured data, and augments it with checks that `kdoctor` lacks (Kotlin Toolchain 0.12, Wasm tooling, Desktop targets, Gradle compatibility, and active agent skills).
  - If `kdoctor` is absent, `kmp doctor` executes built-in diagnostic checks and optionally assists with installation (`brew install kdoctor`).
- **Support Both Build Worlds**:
  - Full inspection and scaffolding support for both standard Gradle (`build.gradle.kts` with `libs.versions.toml`) and modern Kotlin Toolchain (`module.yaml`).

### 2.3. Dogfooding & Standalone Native Performance
- Built in **Kotlin Multiplatform Native** to generate standalone binaries (universal `Mach-O` on macOS x86_64/arm64, Linux ELF, and Windows PE) with sub-second startup times and zero JVM boot overhead.

### 2.4. Decoupled Extensibility (Plugin Engine)
- Inspired by `git` and `gh`: any executable matching `kmp-<subcommand>` found in the user's `PATH` or in `~/.kmp/plugins/` is automatically registered as a subcommand with argument forwarding and standard JSON IPC.

### 2.5. Open Skills Standard (`SKILL.md`)
- Adopts the progressive disclosure standard featuring YAML frontmatter (`name`, `description`, `compatibility`, `tags`) and procedural documentation. Fully compatible with Google Antigravity and open agent runtimes.

---

## 3. System Architecture

```
                      ┌─────────────────────────────────────────┐
                      │          KMP CLI & MCP Bridge           │
                      │         (Kotlin/Native Engine)          │
                      └────────────────────┬────────────────────┘
                                           │
         ┌───────────────────┬─────────────┴───────┬───────────────────┐
         ▼                   ▼                     ▼                   ▼
   ┌───────────┐      ┌─────────────┐       ┌─────────────┐     ┌─────────────┐
   │kmp doctor │      │ kmp create  │       │ kmp analyze │     │ kmp skills  │
   │Environment│      │ Scaffolding │       │ Inspect     │     │ Package     │
   │diagnostics│      │ (Gradle &   │       │ Gradle &    │     │ manager for │
   │& repair   │      │ Toolchain)  │       │ module.yaml │     │ SKILL.md hub│
   └─────┬─────┘      └─────────────┘       └─────────────┘     └──────┬──────┘
         │                                                             │
         ├──────────────────────────────┐                              │
         ▼                              ▼                              │
┌──────────────────┐          ┌──────────────────┐                     │
│ JetBrains        │          │ Native Checks    │                     │
│ kdoctor (macOS)  │          │ (Toolchain 0.12, │                     │
│ [Delegated Check]│          │ Wasm, Desktop,   │                     │
│                  │          │ Skills state)    │                     │
└──────────────────┘          └──────────────────┘                     │
                                                   ┌───────────────────┴───────────────────┐
                                                   ▼                                       ▼
                                       ┌───────────────────────┐               ┌───────────────────────┐
                                       │    KMP Skills Hub     │               │  KMP Plugins Engine   │
                                       │ (SKILL.md catalog:    │               │ (Subprocesses in PATH:│
                                       │ Compose, Ktor, SQL,   │               │ kmp-<plugin-name>)    │
                                       │ Toolchain migration)  │               └───────────────────────┘
                                       └───────────────────────┘
```

---

## 4. Core Command Suite

### 4.1. `kmp doctor`
Evaluates the host environment for complete Kotlin Multiplatform development:
- **Upstream Delegation**: If `kdoctor` is installed, delegates macOS/iOS/CocoaPods verification and aggregates the findings.
- **Modern Extended Checks**:
  - **Kotlin Toolchain**: Detects whether `./kotlin` / `kotlin` CLI (v0.12+) is available.
  - **Web / Wasm Target**: Checks for Node.js, Emscripten/emsdk, and compatible browser runtimes.
  - **Desktop Target**: Validates JDK compatibility and host graphics/native libraries.
  - **AI Agent Readiness**: Verifies local agent skills directories (`.agents/skills/` or `~/.gemini/config/skills/`).
- **Dual Output**:
  - Human: ANSI colored checkmarks, warning badges, and formatted tables.
  - AI Agent (`--json`): Typed JSON schema with status codes, error classifications, and recommended remediation commands.

### 4.2. `kmp describe` / `kmp analyze`
Analyzes projects in the working directory:
- **Gradle Mode**: Parses `settings.gradle.kts`, `build.gradle.kts`, and `gradle/libs.versions.toml`.
- **Kotlin Toolchain Mode**: Parses `module.yaml` and multi-module configurations.
- Reports target matrix (`jvm`, `android`, `iosArm64`, `wasmJs`, `desktop`), applied plugins, and dependencies.
- Emits structured JSON schemas for AI context injection.

### 4.3. `kmp create`
Scaffolding generator supporting modern architectures:
- Templates:
  - `compose-multiplatform`: Modern Compose UI targeting Android, iOS, Desktop, and Wasm.
  - `toolchain-app`: Declarative Kotlin Toolchain project (`module.yaml`) with Compose Hot Reload support.
  - `kmp-library`: Library configured for multiplatform publishing (Gradle or Toolchain 0.12).
  - `fullstack`: Ktor Server + Compose client sharing domain logic.
- Headless / non-interactive flags (`--name`, `--package`, `--targets`, `--format=gradle|toolchain`).

### 4.4. `kmp skills`
Package manager for Kotlin Multiplatform agent skills:
- `kmp skills list`: Enumerates installed and available skills.
- `kmp skills find <query>`: Searches local and remote registries.
- `kmp skills add <id>`: Installs a skill into the agent directory (workspace or global).
- `kmp skills remove <id>`: Uninstalls a skill.
- `kmp skills describe <id>`: Displays frontmatter, instructions, and references.

### 4.5. `kmp mcp`
- Launches a stdio **Model Context Protocol (MCP)** server exposing `kmp` commands (`doctor`, `analyze`, `skills`) directly to MCP-compatible AI agents.

### 4.6. `kmp update`
- Checks for new CLI releases and performs in-place binary upgrades.

---

## 5. Implementation Roadmap

### Phase 0: Foundations & Specifications (Current)
- [x] Repository initialization & Git version control.
- [x] RFC-001: Vision, architecture, and standards (incorporating Kotlin Toolchain 0.12 & KDoctor).
- [x] BDD functional specifications (`docs/features/`).
- [x] Central Progress Tracker (`PROGRESS.md`).

### Phase 1: Core CLI & `kmp doctor` (MVP)
- Kotlin Multiplatform Native Gradle setup targeting `macosArm64`, `macosX64`, and `linuxX64`.
- CLI framework with Clikt and Mordant (terminal formatting & `--json` serializer).
- `kmp doctor` implementation:
  - `kdoctor` integration wrapper on macOS.
  - Modern diagnostic checks: JDK, Android SDK, Kotlin Toolchain (0.12+), Wasm prerequisites.
  - Structured `--json` output with actionable remediation commands.
- Automated tests for diagnostic parsing and reporting.

### Phase 2: KMP Skills Hub & `kmp skills` Command
- Open `SKILL.md` schema and registry catalog (`catalog.json`).
- Seed skills:
  - `kmp-compose-adaptive`: Responsive and adaptive Compose Multiplatform patterns.
  - `kmp-ktor-networking`: Ktor client multiplatform setup with native engines and serialization.
  - `kmp-sqldelight-database`: Multiplatform database persistence with SQLDelight / Room KMP.
  - `kmp-toolchain-migration`: Step-by-step migration from `build.gradle.kts` to Kotlin Toolchain `module.yaml`.
  - `kmp-coroutines-concurrency`: Concurrency and structured async practices across targets.
- Subcommand `kmp skills (list | find | add | describe)`.

### Phase 3: Project Analysis & Scaffolding (`kmp analyze` / `kmp create`)
- Project inspector supporting both `build.gradle.kts` and `module.yaml`.
- Template engine for modern KMP project scaffolding.

### Phase 4: MCP Server, Plugin Engine & Distribution
- `kmp mcp` server implementation for AI agent pairing.
- External plugin discovery (`kmp-<subcommand>` in `PATH`).
- Release packaging (Homebrew Tap, universal binary distribution).
