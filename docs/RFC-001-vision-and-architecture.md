# RFC-001: Vision, Architecture, and Roadmap for KMP CLI and KMP Skills Hub

* **Status**: Proposed (Phase 0)
* **Date**: September 2026
* **Authors**: Jesus Daniel Medina Cruz & Antigravity AI Pair

---

## 1. Executive Summary and Motivation

The **Kotlin Multiplatform (KMP)** ecosystem has reached production maturity for sharing business logic, multiplatform UI with Compose Multiplatform, and lightweight backends with Ktor. However, the developer experience (DX) remains fragmented:
- Environment configuration (JDK, Android SDK, Xcode, CocoaPods/SPM, Kotlin Native targets) is a recurring source of friction.
- Creating new projects or modules often depends on heavyweight IDE wizards or outdated template repositories.
- With the rise of modern AI coding agents (such as Antigravity, Claude Code, Cursor, Copilot), there is no open standard for KMP tooling to expose structured project metadata, nor a curated hub of "skills" (procedural knowledge, runbooks, and best practices) tailored specifically for KMP development.

Inspired by the capabilities of **Android CLI** and the **Android Skills** ecosystem, this project aims to build:
1. **KMP CLI (`kmp`)**: A native, fast, and extensible command-line interface designed for both human developers and AI agents.
2. **KMP Skills Hub**: An open catalog of reusable skills based on the open `SKILL.md` standard to guide architecture, implementations, and best practices across Compose Multiplatform, Ktor, SQLDelight, Room KMP, Firebase, Coroutines, and migrations.

---

## 2. Design Principles

### 2.1. Dual Experience: Humans and AI Agents
- **For Humans**: A rich, interactive, and clean terminal UI (semantic ANSI colors, progress spinners, interactive prompts, and readable tables) powered by libraries like Clikt and Mordant.
- **For AI Agents**:
  - Global `--json` flag on all commands to provide typed, structured, and deterministic outputs.
  - Dedicated inspection commands (`kmp describe`, `kmp analyze`) returning dependency graphs, active targets, and artifact locations without requiring fragile Gradle log scraping.
  - Compatibility with modern agent protocols (Model Context Protocol - MCP and standard skill formats).

### 2.2. Dogfooding & Native Performance
- Build the core CLI in **Kotlin Multiplatform Native** to generate standalone executable binaries (universal `Mach-O` on macOS x86_64/arm64, Linux ELF, and Windows PE) without requiring a pre-installed JVM to run basic commands or diagnostics.

### 2.3. Decoupled Extensibility (Plugin Architecture)
- Architecture inspired by `git` and `gh`: any executable in the user's `PATH` or in `~/.kmp/plugins/` adhering to the `kmp-<subcommand>` naming convention is automatically discovered as a subcommand.
- Standard communication over stdin/stdout with argument forwarding and JSON metadata exchange (`kmp <subcommand> --plugin-manifest`).

### 2.4. Open Skills Standard (`SKILL.md`)
- Adoption of the progressive disclosure skill specification featuring YAML frontmatter (`name`, `description`, `compatibility`, `tags`) paired with actionable procedural guidelines.
- Native compatibility with agentic orchestration systems (including Google Antigravity).

---

## 3. System Architecture

```
                      ┌─────────────────────────────────────────┐
                      │                KMP CLI                  │
                      │          (Kotlin Multiplatform)         │
                      └────────────────────┬────────────────────┘
                                           │
         ┌───────────────────┬─────────────┴───────┬───────────────────┐
         ▼                   ▼                     ▼                   ▼
   ┌───────────┐      ┌─────────────┐       ┌─────────────┐     ┌─────────────┐
   │kmp doctor │      │ kmp create  │       │ kmp analyze │     │ kmp skills  │
   │Host env   │      │ Multi-      │       │ Project     │     │ Skill hub   │
   │diagnostics│      │ platform    │       │ metadata for│     │ management  │
   │(JDK, SDK, │      │ project     │       │ AI agents   │     │ for devs    │
   │Xcode...)  │      │ templates   │       │ and IDEs    │     │ and agents  │
   └───────────┘      └─────────────┘       └─────────────┘     └──────┬──────┘
                                                                       │
                                                   ┌───────────────────┴───────────────────┐
                                                   ▼                                       ▼
                                       ┌───────────────────────┐               ┌───────────────────────┐
                                       │    KMP Skills Hub     │               │  KMP Plugins Engine   │
                                       │ (SKILL.md catalog:    │               │ (Subprocesses in PATH:│
                                       │ Compose, Ktor, SQL,   │               │ kmp-<plugin-name>)    │
                                       │ Coroutines, Arch)     │               └───────────────────────┘
                                       └───────────────────────┘
```

---

## 4. Core Command Suite

### 4.1. `kmp doctor`
Evaluates the health and readiness of the host environment for KMP development:
- Installed Java/JDK version and Gradle compatibility.
- Android SDK (`ANDROID_HOME`), platform-tools, and Build Tools.
- On macOS: Xcode installation, `xcrun` version, command-line tools, available simulators, and CocoaPods/SPM.
- Connectivity to primary Maven repositories (Maven Central, Google, Gradle Plugin Portal).
- Dual output: interactive visual report for humans, or structured JSON with remediation actions for AI agents.

### 4.2. `kmp describe` / `kmp analyze`
Inspects the project located in the current working directory:
- Identifies KMP modules and configured targets (`jvm`, `iosArm64`, `wasmJs`, `desktop`, etc.).
- Inspects dependencies defined in `gradle/libs.versions.toml`.
- Reports applied plugins and target compatibility.
- Emits structured JSON schemas so AI agents understand project context without parsing Gradle files blindly.

### 4.3. `kmp create`
Interactive scaffolding generator for modern KMP projects:
- Official templates:
  - `compose-multiplatform`: Multiplatform application with shared UI (Android, iOS, Desktop, Web Wasm).
  - `library`: KMP library pre-configured for Maven Central publishing with Dokka and GitHub Actions.
  - `fullstack`: Ktor Server + Compose Web/Desktop/Mobile sharing models and domain logic.
- Non-interactive flag support (`--name`, `--package`, `--targets=android,ios,desktop`, `--output`).

### 4.4. `kmp skills`
Package manager for Kotlin Multiplatform skills:
- `kmp skills list`: Lists installed or locally available skills.
- `kmp skills find <keyword>`: Searches the community catalog or remote registries.
- `kmp skills add <id>`: Downloads and installs a skill into the agent configuration directory (e.g. `~/.gemini/config/skills/` or local `.agents/skills/`).
- `kmp skills remove <id>`: Uninstalls a skill.
- `kmp skills describe <id>`: Displays the `SKILL.md` contents and references.

### 4.5. `kmp update`
- Checks for CLI updates and allows in-place self-updates of the binary.

---

## 5. Implementation Roadmap

### Phase 0: Foundations & Specifications (Current)
- [x] Repository initialization & Git version control.
- [x] RFC-001: Vision, architecture, and standards.
- [x] BDD functional specifications (Gherkin `.feature` files).
- [x] Progress Tracker (`PROGRESS.md`).

### Phase 1: Core CLI & `kmp doctor` (MVP)
- Kotlin Multiplatform Native Gradle build configuration.
- Clikt and Mordant integration for CLI options, subcommands, and terminal UI.
- `kmp doctor` implementation (JDK, Android SDK, and Xcode checks) with human & `--json` output.
- Automated diagnostic test suite.

### Phase 2: KMP Skills Hub & `kmp skills` Command
- Open `SKILL.md` schema and registry index format (`catalog.json`).
- Initial seed skills:
  - `kmp-compose-adaptive`: Responsive and adaptive Compose Multiplatform patterns.
  - `kmp-ktor-networking`: Ktor client multiplatform setup with native engines and serialization.
  - `kmp-sqldelight-database`: Multiplatform database persistence with SQLDelight / Room KMP.
  - `kmp-coroutines-concurrency`: Best practices for multiplatform async and flows.
- Subcommand `kmp skills (list | find | add | describe)`.

### Phase 3: Project Scaffolding & Analysis (`kmp create` / `kmp analyze`)
- Project structure inspector for `kmp analyze`.
- Template scaffolding engine for `kmp create`.

### Phase 4: Plugin Architecture & Distribution
- External plugin discovery (`kmp-<subcommand>` resolution in `PATH`).
- IPC JSON communication standard for plugins.
- Packaging for distribution (Homebrew Tap, standalone shell installer).
