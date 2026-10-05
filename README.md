# KMP CLI & KMP Skills Hub

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-2.0%2B-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Status](https://img.shields.io/badge/Status-Phase_3:_Completed-brightgreen.svg)](PROGRESS.md)
[![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](LICENSE)
[![Protocol](https://img.shields.io/badge/Protocol-Model_Context_Protocol_(MCP)-orange.svg)](https://modelcontextprotocol.io/)

> **A unified, extensible CLI and curated Agent Skills Hub for Kotlin Multiplatform — built for both human developers and AI coding agents.**

Inspired by the developer experience of **Google Android CLI & Android Skills**, **Flutter CLI**, and **GitHub CLI**, this project unifies environment diagnostics, project inspection, dependency auditing, project scaffolding, and agent-driven development into a single, cohesive open-source ecosystem.

---

## 🌟 Why KMP CLI?

Kotlin Multiplatform is maturing rapidly with Compose Multiplatform, Ktor, and the recent introduction of **The Kotlin Toolchain 0.12+** (`module.yaml`). However, the developer experience (DX) and AI collaboration experience remain fragmented:

1. **Fragmented Diagnostics**: Existing tools like JetBrains `kdoctor` focus exclusively on mobile setups on macOS without structured JSON output (`--json`), and are unaware of Kotlin Toolchain 0.12, Wasm web runtimes, or Desktop dependencies.
2. **Missing Agent Skills Hub**: While AI coding agents (Antigravity, Claude Code, Cursor, Copilot) are increasingly used to build and migrate KMP projects, there was **no open package manager or standard repository for KMP agent skills** (`SKILL.md`).
3. **Dual Build-System Reality**: Modern teams navigate between established Gradle setups (`build.gradle.kts`) and declarative Kotlin Toolchain projects (`module.yaml`). Tooling must understand both seamlessly.

**KMP CLI (`kmp`)** bridges this gap: it serves as a high-performance native CLI companion for developers and an **AI-native bridge via Model Context Protocol (MCP)**.

---

## 🚀 Key Architectural Pillars

```
                      ┌─────────────────────────────────────────┐
                      │          KMP CLI & MCP Bridge           │
                      │         (Kotlin/Native Engine)          │
                      └────────────────────┬────────────────────┘
                                           │
         ┌───────────────────┬─────────────┴───────┬───────────────────┐
         ▼                   ▼                     ▼                   ▼
   ┌───────────┐      ┌─────────────┐       ┌─────────────┐     ┌─────────────┐
   │kmp doctor │      │ kmp create  │       │kmp describe │     │ kmp skills  │
   │Environment│      │ Scaffolding │       │& kmp analyze│     │ Package     │
   │diagnostics│      │ (Immutable  │       │ Inspect     │     │ manager for │
   │& repair   │      │ Templates)  │       │ Gradle & YAML│    │ SKILL.md hub│
   └─────┬─────┘      └──────┬──────┘       └─────────────┘     └──────┬──────┘
         │                   │                                         │
         ├───────────────────┼──────────────┐                          │
         ▼                   ▼              ▼                          │
┌──────────────────┐ ┌──────────────┐ ┌──────────────────┐             │
│ JetBrains        │ │ templates/   │ │ Native Checks    │             │
│ kdoctor (macOS)  │ │ Directory    │ │ (Toolchain 0.12, │             │
│ [Delegated Check]│ │ (5 Stacks)   │ │ Wasm, Desktop)   │             │
└──────────────────┘ └──────────────┘ └──────────────────┘             │
                                                   ┌───────────────────┴───────────────────┐
                                                   ▼                                       ▼
                                       ┌───────────────────────┐               ┌───────────────────────┐
                                       │    KMP Skills Hub     │               │  KMP Plugins Engine   │
                                       │ (SKILL.md catalog:    │               │ (Subprocesses in PATH:│
                                       │ Compose, Ktor, SQL,   │               │ kmp-<plugin-name>)    │
                                       │ Toolchain migration)  │               └───────────────────────┘
                                       └───────────────────────┘
```

- **Dual Experience (Humans + AI Agents)**:
  - Rich interactive terminal UI with semantic ANSI colors, spinners, tables, and prompts via [Clikt](https://ajalt.github.io/clikt/) and [Mordant](https://ajalt.github.io/mordant/).
  - Global `--json` flag producing deterministic, schema-validated JSON outputs for AI agents and CI/CD pipelines.
- **Facade & Orchestration**:
  - Automatically detects and delegates Apple/Xcode checks to JetBrains `kdoctor` on macOS when installed.
  - Expands diagnostics to include **The Kotlin Toolchain (`./kotlin`)**, Wasm/Web toolchains, Desktop targets, and installed agent skills.
- **Dual Build-System Support**:
  - Full inspection and scaffolding support for both standard Gradle (`build.gradle.kts` with `libs.versions.toml`) and modern Kotlin Toolchain (`module.yaml`).
- **KMP Skills Hub**:
  - Open, curated repository of procedural knowledge following the open `SKILL.md` specification with YAML frontmatter.
  - Compatible with Google Antigravity, Claude Code, Cursor, and custom agent runtimes.
- **Standalone Native Performance**:
  - Built in Kotlin/Native producing standalone binaries (universal `Mach-O` for macOS arm64/x64, Linux ELF, and Windows PE) with sub-second startup times and zero JVM overhead.

---

## 🛠️ Command Suite & Quick Start

| Command | Status | Description | Human Output | AI Output (`--json`) |
| :--- | :---: | :--- | :---: | :---: |
| `kmp doctor` | ✅ Available | Diagnoses host environment (JDK, Android SDK, Xcode via `kdoctor`, Toolchain 0.12, Wasm) | Checkmarks, warnings, remediation | Typed diagnostic report + commands |
| `kmp describe` | ✅ Available | Inspects project architecture, targets, and modules from Gradle or `module.yaml` | Hierarchy tree & summary | `ProjectDescriptor` JSON schema |
| `kmp analyze dependencies` | ✅ Available | Analyzes version catalog (`libs.versions.toml`) dependencies, plugins, and versions | Formatted dependency table | `ProjectDependencies` JSON schema |
| `kmp create` | ✅ Available | Scaffolds new projects using customizable, immutable architectural templates | Success card & next steps | `ScaffoldingResult` JSON schema |
| `kmp skills` | ✅ Available | Package manager for agent skills (`list`, `find`, `describe`, `add`) | Formatted skills table | Machine-readable catalog & paths |
| `kmp mcp` | 📅 Phase 4 | Model Context Protocol stdio server for native AI pair integration | N/A | MCP JSON-RPC protocol |
| `kmp update` | 📅 Phase 4 | In-place self-updater for the CLI binary | Progress animation | Status payload |

---

### Command Highlights & Usage

#### 1. Environment Diagnostics (`kmp doctor`)
```bash
# Human-readable checkmarks and remediations
kmp doctor

# Machine-readable report for CI/CD or AI agents
kmp doctor --json
```

#### 2. Project Architecture Inspection (`kmp describe`)
```bash
# Inspect active working directory
kmp describe

# Inspect a specific project path
kmp describe path/to/project

# Machine JSON format (lists targets, build system, Kotlin version, modules)
kmp describe --json
```

#### 3. Dependency Catalog Analysis (`kmp analyze dependencies`)
```bash
# Analyze default gradle/libs.versions.toml
kmp analyze dependencies

# Analyze a direct .toml file
kmp analyze dependencies custom.toml
kmp analyze dependencies gradle/libs.versions.toml --json
```

#### 4. Project Scaffolding (`kmp create`)
Scaffolds projects using physical, immutable templates from `templates/` with parameter customization:
```bash
# Scaffold an adaptive Compose Multiplatform application
kmp create MyApp --template compose-multiplatform --targets android,ios,desktop,wasm

# Scaffold a declarative Kotlin Toolchain application
kmp create MyToolchainApp --template toolchain-app

# Scaffold a publishable multiplatform library
kmp create MyLib --template kmp-library --package com.example.mylib

# Scaffold a Fullstack application (Ktor Server + Compose Client sharing models)
kmp create MyFullstackApp --template fullstack

# Scaffold a Server-Driven UI starter powered by json-to-compose
kmp create MySduiApp --template sdui-starter

# Scripted execution for AI agents
kmp create MyAgentApp --template kmp-library --json
```

#### 5. KMP Skills Hub (`kmp skills`)
```bash
# List all curated skills
kmp skills list

# Search skills by keyword
kmp skills find compose

# Inspect skill instructions and metadata
kmp skills describe kmp-compose-adaptive

# Install into active project (.agents/skills/<id>/SKILL.md)
kmp skills add kmp-compose-adaptive

# Install globally (~/.kmp/skills/<id>/SKILL.md)
kmp skills add kmp-compose-adaptive --global
```

---

## 📊 Live Project Roadmap & Progress

Development follows the **5-Step AI Planning & Development Process** (Natural Language Intentions $\rightarrow$ Gherkin BDD Specs $\rightarrow$ PROGRESS.md $\rightarrow$ TDD Iteration $\rightarrow$ Atomic Commits).

For the full living progress tracker, see **[PROGRESS.md](PROGRESS.md)**.

### Phase 0: Foundations & Architecture ✅ (Completed)
- [x] Repository initialization & clean Git version control
- [x] Baseline multiplatform `.gitignore`
- [x] **[RFC-001: Vision, Architecture, and Roadmap](docs/RFC-001-vision-and-architecture.md)**
- [x] **[RFC-002: Server-Driven UI Synergy with json-to-compose](docs/RFC-002-sdui-synergy-json-to-compose.md)**
- [x] Initial BDD Feature Specifications (`01_kmp_doctor.feature` through `05_kmp_sdui_integration.feature`)
- [x] Central Progress Tracker setup ([`PROGRESS.md`](PROGRESS.md))

### Phase 1: MVP Core CLI & `kmp doctor` ✅ (Completed)
- [x] Multi-module KMP skeleton: `kmp-core` and native `kmp-cli` with Clikt & Mordant
- [x] Diagnostics Engine (`JdkChecker`, `KotlinToolchainChecker`)
- [x] `KDoctor` macOS wrapper with automatic normalization
- [x] Clikt `doctor` command with dual output (Human TUI & `--json`)
- [x] Universal macOS binary build task (`assembleReleaseExecutableMacos`)

### Phase 2: KMP Skills Hub & Management ✅ (Completed)
- [x] Open `SKILL.md` schema, data models & catalog serialization
- [x] Curated seed KMP skills:
  - `kmp-compose-adaptive` (Adaptive Compose Multiplatform UI)
  - `kmp-ktor-networking` (Ktor 3.x client setup & engines)
  - `kmp-sqldelight-database` (SQLDelight 2.x persistence & migrations)
  - `kmp-toolchain-migration` (Migration to Kotlin Toolchain `module.yaml`)
  - `kmp-coroutines-concurrency` (Structured concurrency & StateFlow)
  - `kmp-cli` (Operational runbook for AI agents)
- [x] Local & Global Skills installer (`kmp skills add`, `describe`, `find`, `list`)

### Phase 3: Project Analysis & Modern Templates ✅ (Completed)
- [x] Dual Build System Parser (`UnifiedProjectParser`, `GradleProjectParser`, `KotlinToolchainParser`)
- [x] `kmp describe` command (Project targets, build system, Kotlin version, modules)
- [x] `kmp analyze dependencies` command (Version catalog & `.toml` dependency parsing)
- [x] Multiplatform Scaffolding Engine (`kmp create`):
  - `compose-multiplatform` (Android, iOS, Desktop, Wasm)
  - `toolchain-app` (`project.yaml` + `module.yaml`)
  - `kmp-library` (Publishing-ready library skeleton)
  - `fullstack` (Ktor Server + Compose Client)
  - `sdui-starter` (Server-Driven UI app)
- [x] Immutable Physical Templates Engine (`templates/` + `TemplateProcessor` + `TemplateSource`)

### Commercial Ecosystem: Zora KMP Kits Hub, Licensing & Copilot 📅 (Next)
- [ ] **[RFC-003: Zora KMP Kits Hub, Licensing Engine, and Copilot Integration](docs/RFC-003-zora-kits-licensing-and-copilot.md)**
- [ ] `kmp kit list` (Catalog discovery for Community, Basic, and Premium tiers)
- [ ] `kmp kit create` with Ed25519 cryptographic license verification
- [ ] In-terminal concierge acquisition screen
- [ ] `kmp kit customize` & `kmp copilot` for licensed developers

### Phase 4: MCP Server, Plugins & Distribution 📅 (Planned)
- [ ] Model Context Protocol (`kmp mcp`) stdio server
- [ ] Decoupled dynamic subcommand discovery (`kmp-*` in `PATH`)
- [ ] In-place self-updater (`kmp update`)
- [ ] Homebrew Tap formula and Maven Central publishing

---

## 💻 Developer Guide: Building & Using `kmp-core` & `kmp-cli`

### 1. Prerequisites
- **Java Development Kit (JDK)**: OpenJDK 17 or 21 (required by Gradle 8.14+ and Kotlin 2.1+).
- **Host OS**: macOS (Apple Silicon `macosArm64` or Intel `macosX64`) or Linux (`linuxX64`).
- **Optional**: JetBrains `kdoctor` (`brew install kdoctor`) for delegated mobile/Xcode diagnostics.

### 2. Running Automated Tests
Run the full verification suite across all multiplatform targets (JVM, macOS Arm64, macOS x64, Linux x64):
```bash
# Run all tests across both modules and native targets
./gradlew allTests

# Run full project checks
./gradlew check
```

### 3. Building & Running `kmp-cli` (Native Binary)

```bash
# Build release executable for macOS Apple Silicon
./gradlew :kmp-cli:linkReleaseExecutableMacosArm64

# Universal macOS Binary (Apple Silicon + Intel via lipo)
./gradlew :kmp-cli:assembleReleaseExecutableMacos

# Install locally to ~/.local/bin/kmp
./gradlew installLocal

# Hot-reload development symlink
./gradlew installDebugLocal
```

---

## 📂 Repository Structure

```text
.
├── README.md                                   # Project landing page & community guide
├── PROGRESS.md                                 # Live BDD progress tracker
├── .gitignore                                  # Multiplatform & build ignore rules
├── templates/                                  # Physical immutable project templates
│   ├── compose-multiplatform/                  # Adaptive Compose Multiplatform app
│   ├── toolchain-app/                          # Kotlin Toolchain 0.12+ declarative app
│   ├── kmp-library/                            # Multiplatform library ready for publishing
│   ├── fullstack/                              # Ktor backend + Compose client
│   └── sdui-starter/                           # Server-Driven UI starter (json-to-compose)
├── skills/                                     # Certified KMP Skills Catalog (SKILL.md)
│   ├── catalog.json                            # Skills registry index
│   ├── kmp-cli/                                # Operational guide for AI agents
│   ├── kmp-compose-adaptive/                   # Responsive Compose patterns
│   ├── kmp-ktor-networking/                    # Ktor client architecture
│   ├── kmp-sqldelight-database/                # Multiplatform SQLite persistence
│   ├── kmp-toolchain-migration/                # Toolchain migration runbook
│   └── kmp-coroutines-concurrency/             # Concurrency best practices
└── docs/
    ├── RFC-001-vision-and-architecture.md      # Core vision, architecture & roadmap
    ├── RFC-002-sdui-synergy-json-to-compose.md # Server-Driven UI synergy
    ├── RFC-003-zora-kits-licensing-and-copilot.md # Commercial Zora Kits & Copilot RFC
    └── features/                               # Executable BDD Gherkin specifications
        ├── 01_kmp_doctor.feature               # Environment doctor behavior
        ├── 02_kmp_skills_management.feature    # Skills repository management
        ├── 03_kmp_project_analysis.feature     # Project inspection & metadata
        ├── 08_kmp_project_scaffolding.feature  # Project scaffolding behavior
        └── 09_zora_kits_licensing_and_copilot.feature # Zora kits & licensing
```

---

## 🤝 Contributing

We welcome contributions from Kotlin Multiplatform developers, tool authors, and AI engineers!

1. **Review RFCs**: Read and discuss [RFC-001](docs/RFC-001-vision-and-architecture.md), [RFC-002](docs/RFC-002-sdui-synergy-json-to-compose.md), and [RFC-003](docs/RFC-003-zora-kits-licensing-and-copilot.md).
2. **Propose Feature Scenarios**: Contribute new `.feature` files in `docs/features/` defining desired CLI behavior in Gherkin syntax.
3. **Author KMP Skills**: Contribute reusable recipes and best practices formatted as `SKILL.md` for the Skills Hub.
4. **Follow BDD & TDD**: Every functional change must be anchored to an approved scenario in `docs/features/` and tracked in `PROGRESS.md`.

---

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).
