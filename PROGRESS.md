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
- [x] **[#4](https://github.com/jesusdmedinac/KMP-CLI/issues/4)** Upstream macOS Delegation: `KDoctor` Integration Wrapper
- [x] **[#5](https://github.com/jesusdmedinac/KMP-CLI/issues/5)** Clikt Doctor Command with Dual Output in `kmp-cli` (Human TUI & `--json`)
- [x] **[#6](https://github.com/jesusdmedinac/KMP-CLI/issues/6)** Universal macOS Binary Assembly Task (`assembleReleaseExecutableMacos`)
- [x] Scenarios in `01_kmp_doctor.feature`:
  - [x] Scenario: Run doctor delegating to kdoctor on macOS when available
  - [x] Scenario: Run doctor with JSON output for AI agent consumption (`--json`)
  - [x] Scenario: Run doctor when kdoctor is not installed on macOS (native fallback & brew hint)
  - [x] Scenario: Run doctor when a critical requirement is missing (with actionable remediations)

---

## Phase 2: KMP Skills Hub & Management
### Feature: KMP Skills Repository (`docs/features/02_kmp_skills_management.feature`)
- [ ] **[#7](https://github.com/jesusdmedinac/KMP-CLI/issues/7)** Open `SKILL.md` Schema, Data Models & Registry Catalog in `kmp-core`
- [ ] **[#8](https://github.com/jesusdmedinac/KMP-CLI/issues/8)** Curate Official Seed KMP Skills (Adaptive Compose, Ktor, SQLDelight, Toolchain, Coroutines)
  - [ ] `kmp-compose-adaptive` (Responsive & adaptive Compose Multiplatform patterns)
  - [ ] `kmp-ktor-networking` (Ktor client setup, native engines, serialization)
  - [ ] `kmp-sqldelight-database` (SQLDelight driver setup per target & migrations)
  - [ ] `kmp-toolchain-migration` (Step-by-step migration from `build.gradle.kts` to Kotlin Toolchain `module.yaml`)
  - [ ] `kmp-coroutines-concurrency` (Best practices for multiplatform async/flows)
- [ ] **[#9](https://github.com/jesusdmedinac/KMP-CLI/issues/9)** Skills Repository Engine & Local Installer in `kmp-core`
- [ ] **[#10](https://github.com/jesusdmedinac/KMP-CLI/issues/10)** `kmp skills` CLI Subcommands (`list`, `find`, `describe`, `add`) in `kmp-cli`

---

## Phase 3: Project Analysis & Modern Templates
### Feature: KMP Project Analysis (`docs/features/03_kmp_project_analysis.feature`)
- [ ] **[#11](https://github.com/jesusdmedinac/KMP-CLI/issues/11)** Dual Build System Parser (Gradle & Kotlin Toolchain `module.yaml`) in `kmp-core`
- [ ] **[#12](https://github.com/jesusdmedinac/KMP-CLI/issues/12)** Project Inspection & Dependency Analysis CLI Commands (`kmp describe` & `kmp analyze`)
- [ ] **[#13](https://github.com/jesusdmedinac/KMP-CLI/issues/13)** Multiplatform Project Scaffolding Engine & `kmp create` Subcommand
  - [ ] Template: `compose-multiplatform` (Android, iOS, Desktop, Wasm)
  - [ ] Template: `toolchain-app` (`module.yaml` + Compose Hot Reload)
  - [ ] Template: `kmp-library` (cross-platform library skeleton)
  - [ ] Template: `fullstack` (Ktor backend + Compose client)

---

## Phase 4: MCP Server, Plugin Engine & Distribution
- [ ] **[#14](https://github.com/jesusdmedinac/KMP-CLI/issues/14)** Stdio Model Context Protocol (MCP) Server in `kmp-cli` (`kmp mcp`)
- [ ] **[#15](https://github.com/jesusdmedinac/KMP-CLI/issues/15)** Dynamic Subcommand Plugin Discovery Architecture (`kmp-*`)
- [ ] **[#16](https://github.com/jesusdmedinac/KMP-CLI/issues/16)** Self-Update Mechanism (`kmp update`) & GitHub Release Downloads
- [ ] **[#17](https://github.com/jesusdmedinac/KMP-CLI/issues/17)** Distribution Packaging (Homebrew Tap Formula & Maven Central Publishing)

---

## Phase 5: Ecosystem Expansions & IDE Tooling (Future Outcomes / Nice-to-Haves)
- [ ] **[#18](https://github.com/jesusdmedinac/KMP-CLI/issues/18)** Native Gradle Plugin (`kmp-gradle-plugin`) for CI/CD Quality Gates (`kmpDoctor`, `kmpAnalyze`, `kmpSkillsVerify`)
- [ ] **[#19](https://github.com/jesusdmedinac/KMP-CLI/issues/19)** IDE Plugin for IntelliJ IDEA, Android Studio & Fleet (Diagnostics toolwindow & in-IDE skills browser)
- [ ] **[#20](https://github.com/jesusdmedinac/KMP-CLI/issues/20)** Web Skills Hub Portal (`skills.kmp-cli.org`) & Community Registry Pipeline

