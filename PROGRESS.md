# Project Progress Roadmap: KMP CLI & KMP Skills Hub

Central tracking roadmap for the project, following the [5-Step AI Planning & Development Process](docs/RFC-001-vision-and-architecture.md).

---

## Phase 0: Foundations & Specifications (Completed)
- [x] Repository initialization & Git setup
- [x] Baseline `.gitignore` configuration
- [x] [RFC-001: Vision, Architecture, and Roadmap](docs/RFC-001-vision-and-architecture.md) (aligned with Kotlin Toolchain 0.12 & KDoctor)
- [x] Initial Gherkin Features:
  - [x] [`docs/features/01_kmp_doctor.feature`](docs/features/01_kmp_doctor.feature)
  - [x] [`docs/features/02_kmp_skills_management.feature`](docs/features/02_kmp_skills_management.feature)
  - [x] [`docs/features/03_kmp_project_analysis.feature`](docs/features/03_kmp_project_analysis.feature)
- [x] Progress Tracker (`PROGRESS.md`) setup
- [x] Community Landing Page & Guide (`README.md`)

---

## Phase 1: MVP Core CLI & `kmp doctor`
### Feature: KMP Environment Doctor (`docs/features/01_kmp_doctor.feature`)
- [ ] Setup KMP Native Gradle build skeleton with Clikt & Mordant
- [ ] Implement System Diagnostics Engine:
  - [ ] `kdoctor` integration wrapper (delegation and output normalizer on macOS)
  - [ ] JDK detector and Gradle version compatibility checker
  - [ ] Android SDK detector (`ANDROID_HOME` / platform tools)
  - [ ] Kotlin Toolchain (0.12+) detector (`./kotlin` / `kotlin` CLI)
  - [ ] Web / Wasm prerequisites checker (Node.js, emsdk, browser runtimes)
  - [ ] Agent Readiness checker (local/global skills directories)
- [ ] Scenario: Run doctor delegating to kdoctor on macOS when available
- [ ] Scenario: Run doctor with JSON output for AI agent consumption (`--json`)
- [ ] Scenario: Run doctor when kdoctor is not installed on macOS (native fallback & brew hint)
- [ ] Scenario: Run doctor when a critical requirement is missing (with actionable remediations)
- [ ] Universal macOS binary build task (`assembleReleaseExecutableMacos`)

---

## Phase 2: KMP Skills Hub & Management
### Feature: KMP Skills Repository (`docs/features/02_kmp_skills_management.feature`)
- [ ] Define open `SKILL.md` schema and catalog registry index (`catalog.json`)
- [ ] Seed skills:
  - [ ] `kmp-compose-adaptive` (Responsive & adaptive Compose Multiplatform patterns)
  - [ ] `kmp-ktor-networking` (Ktor client setup, native engines, serialization)
  - [ ] `kmp-sqldelight-database` (SQLDelight driver setup per target & migrations)
  - [ ] `kmp-toolchain-migration` (Step-by-step migration from `build.gradle.kts` to Kotlin Toolchain `module.yaml`)
  - [ ] `kmp-coroutines-concurrency` (Best practices for multiplatform async/flows)
- [ ] Subcommand `kmp skills list`
- [ ] Subcommand `kmp skills find <keyword>`
- [ ] Subcommand `kmp skills describe <skill-id>`
- [ ] Subcommand `kmp skills add <skill-id>` (destination resolver: global or project `.agents/skills/`)

---

## Phase 3: Project Analysis & Modern Templates
### Feature: KMP Project Analysis (`docs/features/03_kmp_project_analysis.feature`)
- [ ] Subcommand `kmp describe --json`:
  - [ ] Parser for Gradle projects (`build.gradle.kts` + `libs.versions.toml`)
  - [ ] Parser for Kotlin Toolchain projects (`module.yaml`)
- [ ] Subcommand `kmp analyze dependencies --json`
- [ ] Subcommand `kmp create` with templates (`compose-multiplatform`, `toolchain-app`, `kmp-library`, `fullstack`)

---

## Phase 4: MCP Server, Plugin Engine & Distribution
- [ ] `kmp mcp` server implementation (Model Context Protocol bridge for AI agents)
- [ ] External plugin discovery (`PATH` resolution for `kmp-<plugin>`)
- [ ] JSON IPC communication standard for plugins
- [ ] Self-update mechanism (`kmp update`)
- [ ] Packaging for distribution (Homebrew Tap, universal binary)
