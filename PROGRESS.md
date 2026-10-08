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
  - [x] [`docs/features/06_agency_kmp_architect_agent.feature`](docs/features/06_agency_kmp_architect_agent.feature)
  - [x] [`docs/features/07_cross_platform_modernization_and_migration.feature`](docs/features/07_cross_platform_modernization_and_migration.feature)
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
- [x] **[#11](https://github.com/jesusdmedinac/KMP-CLI/issues/11)** Dual Build System Parser (Gradle & Kotlin Toolchain `module.yaml`) in `kmp-core`
- [x] **[#12](https://github.com/jesusdmedinac/KMP-CLI/issues/12)** Project Inspection & Dependency Analysis CLI Commands (`kmp describe` & `kmp analyze`)
  - [x] Scenario: Describe KMP project modules and targets from standard Gradle configuration
  - [x] Scenario: Describe KMP project modules and targets from Kotlin Toolchain module.yaml
  - [x] Scenario: Analyze dependencies from version catalog
- [x] **[#13](https://github.com/jesusdmedinac/KMP-CLI/issues/13)** Multiplatform Project Scaffolding Engine & JetBrains Wizard (`docs/features/08_kmp_project_scaffolding.feature`)
  - [x] Scenario: Scaffold from Official JetBrains Shared UI Multiplatform App template
  - [x] Scenario: Scaffold from Official JetBrains Native UI Multiplatform App template
  - [x] Scenario: Scaffold from Official JetBrains Multiplatform Library template
  - [x] Scenario: Scaffold from Official JetBrains Toolchain App template
  - [x] Scenario: Scaffold specialized Server-Driven UI starter template for json-to-compose
  - [x] Scenario: Scaffold specialized Fullstack KMP template
  - [x] Scenario: Run interactive wizard configuring React Web and Ktor Server
  - [x] Scenario: Scaffold project with customized target platforms
  - [x] Scenario: Scaffold project with JSON output for AI agent automation
  - [x] Scenario: Preserve and enforce execution permissions on wrappers

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

## Commercial Ecosystem: Zora KMP Kits Hub, Licensing & Copilot
### Feature: Zora KMP Kits Hub & Copilot (`docs/features/09_zora_kits_licensing_and_copilot.feature`)
- [ ] **[RFC-003](docs/RFC-003-zora-kits-licensing-and-copilot.md)** Zora KMP Kits Hub, Licensing Engine, and Copilot Integration
  - [ ] Scenario: Discover and inspect available Zora KMP Starter Kits in the CLI
  - [ ] Scenario: Scaffold a free open-source project using Zora Community Kit
  - [ ] Scenario: Prompt developer with concierge acquisition screen when selecting paid kit without license
  - [ ] Scenario: Activate a valid commercial license key
  - [ ] Scenario: Scaffold a project using Zora Basic or Premium kit with valid license
  - [ ] Scenario: Customize a Zora kit configuration for licensed developers
  - [ ] Scenario: Execute KMP Copilot architectural guidance for licensed developers

---

## Phase 4: MCP Server, Plugin Engine & Distribution
- [ ] **[#14](https://github.com/jesusdmedinac/KMP-CLI/issues/14)** Stdio Model Context Protocol (MCP) Server in `kmp-cli` (`kmp mcp`)
- [ ] **[#15](https://github.com/jesusdmedinac/KMP-CLI/issues/15)** Dynamic Subcommand Plugin Discovery Architecture (`kmp-*`)
- [ ] **[#16](https://github.com/jesusdmedinac/KMP-CLI/issues/16)** Self-Update Mechanism (`kmp update`) & GitHub Release Downloads
- [ ] **[#17](https://github.com/jesusdmedinac/KMP-CLI/issues/17)** Distribution Packaging (Homebrew Tap Formula & Maven Central Publishing)

---

## Agentic Ecosystem: Agency Agents Synergy
### Feature: Kotlin Multiplatform Architect Agent (`docs/features/06_agency_kmp_architect_agent.feature`)
- [ ] **[#39](https://github.com/jesusdmedinac/KMP-CLI/issues/39)** Kotlin Multiplatform Architect Agent Persona for `agency-agents`
  - [ ] Scenario: Validate agent metadata and YAML frontmatter schema
  - [ ] Scenario: Define specialized persona identity, memory, and core mission
  - [ ] Scenario: Enforce critical KMP architecture rules and anti-patterns
  - [ ] Scenario: Provide idiomatic technical deliverables
  - [ ] Scenario: Integrate KMP-CLI dual DX diagnostic and skills workflows
  - [ ] Scenario: Verify multi-tool agency compatibility and conversion

---

## Cross-Platform Modernization: React Native & Flutter Migration Engine
### Feature: Cross-Platform Modernization & Migration Analysis (`docs/features/07_cross_platform_modernization_and_migration.feature`)
- [ ] **[#40](https://github.com/jesusdmedinac/KMP-CLI/issues/40)** Cross-Platform Modernization & Audit Engine
  - [ ] Scenario: Audit React Native codebase and generate dependency replacement matrix
  - [ ] Scenario: Audit Flutter codebase and generate dependency replacement matrix
  - [ ] Scenario: Smart redirect from kmp analyze when detecting legacy cross-platform codebases
  - [ ] Scenario: Recommend architectural modernization pathway
  - [ ] Scenario: Generate phased Strangler Fig migration roadmap
  - [ ] Scenario: Provide canonical migration skills in KMP Skills Hub
  - [ ] Scenario: Scaffold Shared KMP Core bridge template
  - [ ] Scenario: Output migration audit in dual formats for humans and AI agents

---

## Visual Branding & Multiplatform Asset Ecosystem
### Feature: Zora Brand Identity & Multiplatform Assets (`docs/features/10_zora_branding_and_assets.feature`)
- [x] Scenario: Generate standardized multi-platform assets from Zora master icons
- [x] Scenario: Update Android launcher and adaptive icons across all project templates
- [x] Scenario: Update iOS AppIcon asset catalog across all project templates
- [x] Scenario: Integrate Zora vector drawable into Compose Multiplatform template UI
- [x] Scenario: Embed official Zora branding into documentation

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
- [ ] **[#43](https://github.com/jesusdmedinac/KMP-CLI/issues/43)** Template Freshness & Autonomous Sync: Add upstream check in 'kmp doctor', add 'kmp templates' command, and instruct AI agent skill to verify template freshness


