# Project Progress Roadmap: KMP CLI & KMP Skills Hub

Central tracking roadmap for the project, following the [5-Step AI Planning & Development Process](docs/RFC-001-vision-and-architecture.md).

---

## Phase 0: Foundations & Specifications (In Progress)
- [x] Repository initialization & Git setup
- [x] Baseline `.gitignore` configuration
- [x] [RFC-001: Vision, Architecture, and Roadmap](docs/RFC-001-vision-and-architecture.md)
- [x] Initial Gherkin Features:
  - [x] [`docs/features/01_kmp_doctor.feature`](docs/features/01_kmp_doctor.feature)
  - [x] [`docs/features/02_kmp_skills_management.feature`](docs/features/02_kmp_skills_management.feature)
  - [x] [`docs/features/03_kmp_project_analysis.feature`](docs/features/03_kmp_project_analysis.feature)
- [x] Progress Tracker (`PROGRESS.md`) setup

---

## Phase 1: MVP Core CLI & `kmp doctor`
### Feature: KMP Environment Doctor (`docs/features/01_kmp_doctor.feature`)
- [ ] Setup KMP Native Gradle build skeleton with Clikt & Mordant
- [ ] Implement System Checker (JDK detection and version check)
- [ ] Implement Android SDK Checker (`ANDROID_HOME` / platform tools)
- [ ] Implement macOS Toolchain Checker (Xcode, `xcrun`, CocoaPods)
- [ ] Scenario: Run doctor with human-friendly output when all tools are installed
- [ ] Scenario: Run doctor with JSON output for AI agent consumption (`--json`)
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
  - [ ] `kmp-coroutines-concurrency` (Best practices for multiplatform async/flows)
- [ ] Subcommand `kmp skills list`
- [ ] Subcommand `kmp skills find <keyword>`
- [ ] Subcommand `kmp skills describe <skill-id>`
- [ ] Subcommand `kmp skills add <skill-id>` (destination resolver: global or project `.agents/skills/`)

---

## Phase 3: Project Analysis & Modern Templates
### Feature: KMP Project Analysis (`docs/features/03_kmp_project_analysis.feature`)
- [ ] Subcommand `kmp describe --json` (project structure, targets, modules)
- [ ] Subcommand `kmp analyze dependencies --json` (TOML catalog parser)
- [ ] Subcommand `kmp create` with templates (`compose-multiplatform`, `library`, `fullstack`)

---

## Phase 4: Plugin Architecture & Distribution
- [ ] External plugin discovery (`PATH` resolution for `kmp-<plugin>`)
- [ ] JSON IPC communication standard for plugins
- [ ] Self-update mechanism (`kmp update`)
- [ ] Packaging for distribution (Homebrew Tap, shell installer)
