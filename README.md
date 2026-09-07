# KMP CLI & KMP Skills Hub

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-1.9%2B-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Status](https://img.shields.io/badge/Status-Phase_1:_MVP_In_Progress-blue.svg)](PROGRESS.md)
[![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](LICENSE)
[![Protocol](https://img.shields.io/badge/Protocol-Model_Context_Protocol_(MCP)-orange.svg)](https://modelcontextprotocol.io/)

> **A unified, extensible CLI and curated Agent Skills Hub for Kotlin Multiplatform — built for both human developers and AI coding agents.**

Inspired by the developer experience of **Google Android CLI & Android Skills**, **Flutter CLI**, and **GitHub CLI**, this project unifies environment diagnostics, project scaffolding, and agent-driven development into a single, cohesive open-source ecosystem.

---

## 🌟 Why KMP CLI?

Kotlin Multiplatform is maturing rapidly with Compose Multiplatform, Ktor, and the recent introduction of **The Kotlin Toolchain 0.12+** (`module.yaml`). However, the developer experience (DX) and AI collaboration experience remain fragmented:

1. **Fragmented Diagnostics**: Existing tools like JetBrains `kdoctor` focus exclusively on mobile setups on macOS without structured JSON output (`--json`), and are unaware of Kotlin Toolchain 0.12, Wasm web runtimes, or Desktop dependencies.
2. **Missing Agent Skills Hub**: While AI coding agents (Antigravity, Claude Code, Cursor, Copilot) are increasingly used to build and migrate KMP projects, there is **no open package manager or standard repository for KMP agent skills** (`SKILL.md`).
3. **Dual Build-System Reality**: Modern teams navigate between established Gradle setups (`build.gradle.kts`) and declarative Kotlin Toolchain projects (`module.yaml`). Tooling must understand both seamlessly.

**KMP CLI (`kmp`)** bridges this gap: it serves as a native CLI companion for developers and an **AI-native bridge via Model Context Protocol (MCP)**.

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

- **Dual Experience (Humans + AI Agents)**:
  - Rich interactive terminal UI with semantic ANSI colors, spinners, tables, and prompts via [Clikt](https://ajalt.github.io/clikt/) and [Mordant](https://ajalt.github.io/mordant/).
  - Global `--json` flag producing deterministic, schema-validated JSON outputs for AI agents and CI/CD pipelines.
- **Facade & Orchestration**:
  - Automatically detects and delegates Apple/Xcode checks to JetBrains `kdoctor` on macOS when installed.
  - Expands diagnostics to include **The Kotlin Toolchain (`./kotlin`)**, Wasm/Web toolchains, Desktop targets, and installed agent skills.
- **KMP Skills Hub**:
  - Open, curated repository of procedural knowledge following the open `SKILL.md` specification with YAML frontmatter.
  - Compatible with Google Antigravity, Claude Code, Cursor, and custom agent runtimes.
- **Model Context Protocol (MCP)**:
  - Built-in `kmp mcp` server exposing diagnostics, project inspection, and skill management directly to AI pairs.
- **Standalone Native Performance**:
  - Built in Kotlin/Native producing standalone binaries (universal `Mach-O` for macOS arm64/x64, Linux ELF, and Windows PE) with sub-second startup times and zero JVM overhead.

---

## 🛠️ Planned Command Suite

| Command | Description | Human Output | AI Output (`--json`) |
| :--- | :--- | :---: | :---: |
| `kmp doctor` | Diagnoses host environment (JDK, Android SDK, Xcode via `kdoctor`, Kotlin Toolchain 0.12, Wasm runtimes) | Checkmarks, warnings, tables | Typed diagnostic report + remediation commands |
| `kmp describe` / `kmp analyze` | Inspects project targets, modules, and dependencies from `build.gradle.kts` or `module.yaml` | Project summary tree | Full dependency & target matrix JSON schema |
| `kmp create` | Interactive scaffolding generator for Compose Multiplatform, Kotlin Toolchain, and Library projects | Interactive wizard | Non-interactive flag-driven execution |
| `kmp skills` | Package manager for agent skills (`list`, `find`, `add`, `describe`) | Formatted skill catalog | Machine-readable catalog & installation paths |
| `kmp mcp` | Runs the Model Context Protocol stdio server for AI agent integration | N/A | MCP JSON-RPC protocol |
| `kmp update` | In-place self-update for the CLI binary | Progress animation | Status payload |

---

## 📊 Live Project Roadmap & Progress

Development follows the **5-Step AI Planning & Development Process** (Natural Language Intentions $\rightarrow$ Gherkin BDD Specs $\rightarrow$ PROGRESS.md $\rightarrow$ TDD Iteration $\rightarrow$ Atomic Commits).

For the full living progress tracker, see **[PROGRESS.md](PROGRESS.md)**.

### Phase 0: Foundations & Architecture ✅ (Completed)
- [x] Repository initialization & clean Git version control
- [x] Baseline multiplatform `.gitignore`
- [x] **[RFC-001: Vision, Architecture, and Roadmap](docs/RFC-001-vision-and-architecture.md)** (incorporating Kotlin Toolchain 0.12 & KDoctor)
- [x] Initial BDD Feature Specifications:
  - [`docs/features/01_kmp_doctor.feature`](docs/features/01_kmp_doctor.feature)
  - [`docs/features/02_kmp_skills_management.feature`](docs/features/02_kmp_skills_management.feature)
  - [`docs/features/03_kmp_project_analysis.feature`](docs/features/03_kmp_project_analysis.feature)
- [x] Central Progress Tracker setup ([`PROGRESS.md`](PROGRESS.md))

### Phase 1: MVP Core CLI & `kmp doctor` ⏳ (In Progress)
- [ ] Setup KMP Native Gradle build skeleton with Clikt & Mordant
- [ ] Implement System Diagnostics Engine:
  - [ ] `kdoctor` integration wrapper (macOS delegation & output normalizer)
  - [ ] JDK detector and Gradle version compatibility checker
  - [ ] Android SDK detector (`ANDROID_HOME` / platform tools)
  - [ ] Kotlin Toolchain (0.12+) detector (`./kotlin` / `kotlin` CLI)
  - [ ] Web / Wasm prerequisites checker (Node.js, emsdk, browser runtimes)
  - [ ] Agent Readiness checker (local/global skills directories)
- [ ] Implement `--json` output serializer with actionable remediation commands
- [ ] Universal macOS binary build task (`assembleReleaseExecutableMacos`)

### Phase 2: KMP Skills Hub & Management 📅 (Planned)
- [ ] Open `SKILL.md` schema and registry index format (`catalog.json`)
- [ ] Seed skills:
  - `kmp-compose-adaptive` (Responsive & adaptive Compose Multiplatform patterns)
  - `kmp-ktor-networking` (Ktor client setup, native engines, serialization)
  - `kmp-sqldelight-database` (SQLDelight driver setup per target & migrations)
  - `kmp-toolchain-migration` (Step-by-step migration from `build.gradle.kts` to `module.yaml`)
  - `kmp-coroutines-concurrency` (Best practices for multiplatform async/flows)
- [ ] Subcommands: `kmp skills list`, `find`, `describe`, and `add`

### Phase 3: Project Analysis & Scaffolding 📅 (Planned)
- [ ] `kmp describe --json` parser for Gradle (`build.gradle.kts`) and Kotlin Toolchain (`module.yaml`)
- [ ] `kmp create` template generator

### Phase 4: MCP Server, Plugins & Distribution 📅 (Planned)
- [ ] Model Context Protocol (`kmp mcp`) stdio server
- [ ] Decoupled plugin discovery (`kmp-<subcommand>` in `PATH`)
- [ ] Homebrew Tap and standalone installation scripts

---

## 📂 Repository Structure

```text
.
├── README.md                                   # Project landing page & community guide
├── PROGRESS.md                                 # Live BDD progress tracker
├── .gitignore                                  # Multiplatform & build ignore rules
└── docs/
    ├── RFC-001-vision-and-architecture.md      # Comprehensive architecture & RFC
    ├── architecture/
    │   └── ADR-001-modular-core-and-cli-architecture.md # Core library vs CLI binary decision
    ├── ecosystem/
    │   └── kmp-ecosystem-state.md              # Living KMP ecosystem & developer reference guide
    └── features/                               # Executable BDD Gherkin specifications
        ├── 01_kmp_doctor.feature               # Environment doctor behavior
        ├── 02_kmp_skills_management.feature    # Skills repository management
        └── 03_kmp_project_analysis.feature     # Project inspection & metadata
```

---

## 🤝 Contributing

We welcome contributions from Kotlin Multiplatform developers, tool authors, and AI engineers!

Here is how you can get involved right now:
1. **Review RFCs**: Read and discuss [RFC-001](docs/RFC-001-vision-and-architecture.md) in the issues/discussions.
2. **Propose Feature Scenarios**: Contribute new `.feature` files in `docs/features/` defining desired CLI behavior in Gherkin syntax.
3. **Author KMP Skills**: Contribute reusable recipes, runbooks, and best practices formatted as `SKILL.md` for the upcoming Skills Hub.
4. **Code the CLI**: Join us in Phase 1 as we implement the Kotlin/Native CLI skeleton and diagnostic checks.

### Development Guidelines
- All code, documentation, commit messages, and feature specifications are maintained in **English**.
- Every functional change must be anchored to an approved scenario in `docs/features/` and tracked in `PROGRESS.md`.
- Commits must follow [Conventional Commits](https://www.conventionalcommits.org/).

---

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).
