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
- [x] Living KMP Ecosystem & Reference Guide ([`docs/ecosystem/kmp-ecosystem-state.md`](docs/ecosystem/kmp-ecosystem-state.md))
- [x] [ADR-001: Modular Engine (kmp-core) and CLI (kmp-cli) Architecture](docs/architecture/ADR-001-modular-core-and-cli-architecture.md)

---

## Phase 1: MVP Core CLI & `kmp doctor`
### Feature: KMP Environment Doctor (`docs/features/01_kmp_doctor.feature`)
- [x] Setup multi-module KMP Gradle skeleton: `kmp-core` (engine library) and `kmp-cli` (Native application with Clikt & Mordant)
- [x] **[#2](https://github.com/jesusdmedinac/KMP-CLI/issues/2)** Core Diagnostic Data Models & Serialization in `kmp-core`
- [x] **[#3](https://github.com/jesusdmedinac/KMP-CLI/issues/3)** System Diagnostics Engine (`JdkChecker` & `KotlinToolchainChecker`)
- [ ] **[#4](https://github.com/jesusdmedinac/KMP-CLI/issues/4)** Upstream macOS Delegation: `KDoctor` Integration Wrapper
- [ ] **[#5](https://github.com/jesusdmedinac/KMP-CLI/issues/5)** Clikt Doctor Command with Dual Output in `kmp-cli` (Human TUI & `--json`)
- [ ] **[#6](https://github.com/jesusdmedinac/KMP-CLI/issues/6)** Universal macOS Binary Assembly Task (`assembleReleaseExecutableMacos`)
- [ ] Scenarios in `01_kmp_doctor.feature`:
  - [ ] Scenario: Run doctor delegating to kdoctor on macOS when available
  - [ ] Scenario: Run doctor with JSON output for AI agent consumption (`--json`)
  - [ ] Scenario: Run doctor when kdoctor is not installed on macOS (native fallback & brew hint)
  - [ ] Scenario: Run doctor when a critical requirement is missing (with actionable remediations)

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
- [ ] Publish `kmp-core` to Maven Central & verify listing on `klibs.io`

---

## Phase 5: Ecosystem Expansions & IDE Tooling (Future Outcomes / Nice-to-Haves)
- [ ] **Gradle Plugin (`kmp-gradle-plugin`)**:
  - [ ] Task `kmpDoctor` for CI/CD environment validation
  - [ ] Task `kmpAnalyze` for automated build health reports
  - [ ] Task `kmpSkillsVerify` to ensure repository follows certified KMP skills
- [ ] **IDE Integration (IntelliJ IDEA, Android Studio, Fleet)**:
  - [ ] Diagnostic status panel and one-click environment fixes
  - [ ] In-IDE KMP Skills catalog browser
- [ ] **Web Skills Hub (`skills.kmp-cli.org`)**: Interactive web portal for community skills
- [ ] **Community Plugin Registry**: Verified registry for third-party `kmp-*` extensions

