# Project Progress Roadmap: KMP CLI & KMP Skills Hub

Central tracking roadmap for the project, following the [5-Step AI Planning & Development Process](docs/RFC-001-vision-and-architecture.md).

---

## Phase 0: Foundations & Specifications (Completed)
- [x] Repository initialization & Git setup
- [x] Baseline `.gitignore` configuration
- [x] [RFC-001: Vision, Architecture, and Roadmap](docs/RFC-001-vision-and-architecture.md) (aligned with Kotlin Toolchain 0.12 & KDoctor)
- [x] [RFC-002: Server-Driven UI Synergy with json-to-compose](docs/RFC-002-sdui-synergy-json-to-compose.md)
- [x] Initial Gherkin Features:
  - [x] [`docs/features/01_kmp_doctor.feature`](docs/features/01_kmp_doctor.feature)
  - [x] [`docs/features/02_kmp_skills_management.feature`](docs/features/02_kmp_skills_management.feature)
  - [x] [`docs/features/03_kmp_project_analysis.feature`](docs/features/03_kmp_project_analysis.feature)
  - [x] [`docs/features/04_kmp_skills_curation.feature`](docs/features/04_kmp_skills_curation.feature)
  - [x] [`docs/features/05_kmp_sdui_integration.feature`](docs/features/05_kmp_sdui_integration.feature)
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
- [x] **[#7](https://github.com/jesusdmedinac/KMP-CLI/issues/7)** Open `SKILL.md` Schema, Data Models & Registry Catalog in `kmp-core`
- [x] **[#8](https://github.com/jesusdmedinac/KMP-CLI/issues/8)** Curate Official Seed KMP Skills (Adaptive Compose, Ktor, SQLDelight, Toolchain, Coroutines)
  - [x] `kmp-compose-adaptive` (Responsive & adaptive Compose Multiplatform patterns)
  - [x] `kmp-ktor-networking` (Ktor client setup, native engines, serialization)
  - [x] `kmp-sqldelight-database` (SQLDelight driver setup per target & migrations)
  - [x] `kmp-toolchain-migration` (Step-by-step migration from `build.gradle.kts` to Kotlin Toolchain `module.yaml`)
  - [x] `kmp-coroutines-concurrency` (Best practices for multiplatform async/flows)
  - [x] `kmp-cli` (Procedural guide for AI agents and developers using KMP CLI)
- [x] **[#9](https://github.com/jesusdmedinac/KMP-CLI/issues/9)** Skills Repository Engine & Local Installer in `kmp-core`
- [x] **[#10](https://github.com/jesusdmedinac/KMP-CLI/issues/10)** `kmp skills` CLI Subcommands (`list`, `find`, `describe`, `add`) in `kmp-cli`

---

## Skills Hub: Canonical Expansion (Planned Milestone)
### Feature: Canonical KMP Skills Curation & Ethical Attribution (`docs/features/04_kmp_skills_curation.feature`)
- [ ] **[#25](https://github.com/jesusdmedinac/KMP-CLI/issues/25)** Curate `kmp-koin-di` skill (Multiplatform dependency injection with Koin)
- [ ] **[#26](https://github.com/jesusdmedinac/KMP-CLI/issues/26)** Curate `kmp-swift-interop` skill (Swift export & Touchlab SKIE bridge)
- [ ] **[#27](https://github.com/jesusdmedinac/KMP-CLI/issues/27)** Curate `kmp-testing-architecture` skill (Turbine flows & multiplatform fakes)
- [ ] **[#28](https://github.com/jesusdmedinac/KMP-CLI/issues/28)** Curate `kmp-decompose-navigation` skill (Multiplatform component stack & lifecycle)
- [ ] **[#29](https://github.com/jesusdmedinac/KMP-CLI/issues/29)** Curate `kmp-multiplatform-settings` skill (Cross-platform key-value persistence)
- [ ] **[#30](https://github.com/jesusdmedinac/KMP-CLI/issues/30)** Curate `kmp-coil-media` skill (Asynchronous image loading & caching in Compose MP)

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
  - [ ] Template: `sdui-starter` (Server-Driven UI app powered by json-to-compose & caching)

---

## Server-Driven UI Synergy: json-to-compose & Composy
### Feature: Server-Driven UI Integration (`docs/features/05_kmp_sdui_integration.feature`)
- [ ] **[#32](https://github.com/jesusdmedinac/KMP-CLI/issues/32)** Curate `kmp-sdui-compose` canonical skill for Server-Driven UI *(Pre-condition: refactor `SkillsRepositoryTest` `compose` assertion predicate to accommodate multiple compose skills)*
- [ ] **[#33](https://github.com/jesusdmedinac/KMP-CLI/issues/33)** Add `sdui-starter` template powered by json-to-compose
- [ ] **[#34](https://github.com/jesusdmedinac/KMP-CLI/issues/34)** Add `kmp sdui` command with schema export and validation
- [ ] **[#35](https://github.com/jesusdmedinac/KMP-CLI/issues/35)** Implement static Compose Kotlin code transpiler in `kmp sdui convert`
- Scenarios in `05_kmp_sdui_integration.feature`:
  - [ ] Scenario: Scaffold a new multiplatform project using the sdui-starter template (#33)
  - [ ] Scenario: Discover, inspect, and install the canonical kmp-sdui-compose skill (#32)
  - [ ] Scenario: Export the json-to-compose JSON schema for IDE and AI validation (#34)
  - [ ] Scenario: Validate a local or remote SDUI JSON document against the schema (#34)
  - [ ] Scenario: Transpile dynamic SDUI JSON into static Jetpack Compose Kotlin code (#35)

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

---

## Technical Debt & Enhancements Backlog
- [ ] **[#23](https://github.com/jesusdmedinac/KMP-CLI/issues/23)** Atomic File Writes: Use temporary files and atomic POSIX rename in `SystemEnvironment.writeFileText`
- [ ] **[#24](https://github.com/jesusdmedinac/KMP-CLI/issues/24)** Remote Skills Registry HTTP Resolver via Ktor Client
- [x] **[#31](https://github.com/jesusdmedinac/KMP-CLI/issues/31)** Local Installation Automation: Add `installLocal` Gradle tasks and refine local PATH documentation


