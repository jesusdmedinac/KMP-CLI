# RFC-002: Server-Driven UI Synergy between KMP-CLI and json-to-compose

* **Status**: Proposed
* **Date**: September 2026
* **Authors**: Jesus Daniel Medina Cruz & Antigravity AI Pair

---

## 1. Executive Summary & Vision

Server-Driven UI (SDUI) is rapidly emerging as a foundational architecture in modern multiplatform development, enabling product teams to update UI layouts, business logic triggers, and design tokens instantly from remote backends without waiting for app store review cycles.

Within our multiplatform ecosystem, three specialized components exist:
1. **`json-to-compose` (Runtime Engine & Library)**: The core multiplatform renderer and action engine that converts JSON documents into native Jetpack Compose UIs across Android, iOS, Desktop, and Wasm, complete with an offline-first caching system, HTTP 304/ETag revalidation, and Stale-While-Revalidate (SWR).
2. **`Composy` (Visual Studio & Canvas)**: The visual drag-and-drop editor and preview tool allowing designers and developers to construct SDUI screens and export `ComposeDocument` schemas.
3. **`KMP-CLI` (`kmp`)**: The developer toolchain, project scaffolding engine, AI agent bridge, and Skills Hub for the Kotlin Multiplatform ecosystem.

This RFC defines the **architectural synergy** between `KMP-CLI` and `json-to-compose`, positioning `kmp` as the primary CLI bridge for scaffolding, validating, transpiling, and managing SDUI applications and agent workflows.

---

## 2. Four Strategic Pillars of Synergy

```
+-----------------------------------------------------------------------------------------+
|                                    COMPOSY (Studio)                                     |
|  - Visual Drag-and-Drop Canvas                                                          |
|  - In-browser / Desktop preview with simulated latency, offline mode, and 304 ETag       |
|  - AI-assisted chat prompt layout generation                                            |
|  - Exports ComposeDocument JSON                                                         |
+--------------------------------------------+--------------------------------------------+
                                             |
                                    JSON Schemas & Specs
                                             |
                                             v
+-----------------------------------------------------------------------------------------+
|                                   KMP-CLI (Developer Bridge)                            |
|                                                                                         |
|  1. Scaffolding: `kmp create --template sdui-starter`                                   |
|     Bootstraps a complete multiplatform app pre-wired with json-to-compose & cache.      |
|                                                                                         |
|  2. Agent Skill: `kmp-sdui-compose`                                                     |
|     Teaches developers & AI pairs how to design, evolve, cache, and test SDUI screens.  |
|                                                                                         |
|  3. CLI Toolset: `kmp sdui`                                                             |
|     - `kmp sdui schema`: Exports JSON schema for validation and IDE autocompletion.     |
|     - `kmp sdui validate <file>`: Validates SDUI JSON syntax and component integrity.    |
|     - `kmp sdui preview <file>`: Instant desktop preview of remote/local JSON layouts.  |
|     - `kmp sdui convert <file.json> [--output Screen.kt]`: Converts SDUI JSON schemas   |
|       into clean, static, compile-time Jetpack Compose Kotlin code!                     |
+--------------------------------------------+--------------------------------------------+
                                             |
                                     Kotlin Multiplatform
                                             |
                                             v
+-----------------------------------------------------------------------------------------+
|                              json-to-compose (Runtime Engine)                           |
|  - Core Library (`library/`): Multiplatform parser, reactive state & actions wiring.   |
|  - Remote Engine & Cache (Phase 9): Ktor client, L1/L2 cache, SWR, HTTP 304,           |
|    and declarative `RemoteComposeDocument(url)` Composable.                             |
+-----------------------------------------------------------------------------------------+
```

### Pillar 1: Scaffolding Template `sdui-starter` (`kmp create`)
Developers can scaffold a production-ready KMP Server-Driven UI project with a single command:
```bash
kmp create --template sdui-starter --name MySduiApp
```
The generated project contains:
- Multiplatform targets: Android, iOS, Desktop (JVM), and Web (Wasm).
- Dependency injection / service registry pre-configured with `json-to-compose` and Ktor Client engines.
- Pre-wired `SduiEngine` with dual-tier caching (L1 in-memory + L2 persistent storage).
- Sample remote screen consumer (`RemoteComposeDocument("https://api.example.com/screens/home.json")`).
- Local embedded fallback bundle in `composeResources/` for first-run offline support.
- Pre-configured `LocalCustomActionHandlers` for native integrations (navigation, logging, analytics).

### Pillar 2: Canonical Skill `kmp-sdui-compose`
A first-class, ethically-attributed procedural skill authored according to the `SKILL.md` schema, hosted in the KMP Skills Hub:
- Teaches autonomous AI coding agents (Antigravity, Claude Code, Cursor, Copilot) how to design, manipulate, and review `json-to-compose` schemas.
- Details the declarative structure of `ComposeDocument` (`initialState`, `actions`, `root` hierarchy).
- Provides exact instructions for leveraging the Remote Engine, caching policies, and Stale-While-Revalidate semantics.
- Documents best practices for versioning remote schemas without breaking older mobile app versions.

### Pillar 3: CLI Subcommand Suite `kmp sdui`
A dedicated command namespace within `kmp-cli`:
1. **`kmp sdui schema`**:
   - Emits the complete JSON Schema representing all available `ComposeNode` primitives, modifiers, and `ComposeAction` types.
   - Used for IDE autocompletion and agent schema validation.
2. **`kmp sdui validate <path-or-url>`**:
   - Validates that a target JSON file or remote endpoint response conforms strictly to the `json-to-compose` specification.
   - Verifies node hierarchies, required properties, and action mappings.
3. **`kmp sdui preview <path-or-url>`**:
   - Renders a standalone lightweight desktop preview window or connects with `Composy` to visualize the target layout in real time.
4. **`kmp sdui convert <screen.json> [--output Screen.kt]`**:
   - **Static Transpilation Tool**: Translates dynamic SDUI JSON documents into idiomatic, compile-time Jetpack Compose Kotlin code.
   - When a screen graduates from rapid dynamic iteration to a fixed core feature, developers can statically compile it with zero manual rewrite overhead.

### Pillar 4: Bridge to Composy Studio
- `kmp sdui serve` spins up a local mock server streaming SDUI JSON files with live-reload support.
- Allows Composy to connect directly to the local workspace files, creating a tight feedback loop between the visual canvas and local version control.

---

## 3. Design Decisions & Trade-Offs

1. **Decoupled Architecture**: `KMP-CLI` interacts with `json-to-compose` via well-defined contracts (JSON Schema and CLI commands). It does not require hardcoding runtime UI rendering into the CLI binary itself.
2. **Progressive Adoption**: Developers can use `kmp create --template sdui-starter` for new apps, or install `kmp-sdui-compose` skill into existing codebases to add SDUI capabilities incrementally.
3. **Transpiler Output Quality**: `kmp sdui convert` generates idiomatic Compose code formatted per Kotlin conventions (nested layouts, named arguments, trailing lambdas, `Modifier` chains) rather than raw machine-generated syntax.
4. **Transpiler Architecture (Issue #35)**: Phase 1 of `kmp sdui convert` focuses on pure declarative `@Composable` function generation with local state (`remember { mutableStateOf(...) }`). Future iterations can introduce an optional `--architecture mvi|viewmodel` flag for enterprise state holders.
5. **Skills Catalog Test Pre-condition (Issue #32)**: Prior to registering `kmp-sdui-compose` in the compiled `DefaultSkillsCatalog`, `SkillsRepositoryTest.kt` must be refactored from `assertEquals(1, composeSkills.size)` to a flexible predicate (`assertTrue(composeSkills.any { ... })`) to cleanly support multiple skills tagged with `compose`.

---

## 4. Implementation Phasing

1. **Specification & Feature Modeling** (Completed):
   - RFC-002 and BDD Feature Specification ([`05_kmp_sdui_integration.feature`](features/05_kmp_sdui_integration.feature)).
   - GitHub Issues creation and progress tracking sync.
2. **Skill Hub Seed Curation**:
   - **[Issue #32](https://github.com/jesusdmedinac/KMP-CLI/issues/32)**: Curate `kmp-sdui-compose` canonical skill for Server-Driven UI and register in `DefaultSkillsCatalog`.
3. **Template Scaffolding**:
   - **[Issue #33](https://github.com/jesusdmedinac/KMP-CLI/issues/33)**: Add `sdui-starter` template powered by `json-to-compose` in `kmp-core` template engine.
4. **CLI Tooling & Transpiler**:
   - **[Issue #34](https://github.com/jesusdmedinac/KMP-CLI/issues/34)**: Add `kmp sdui` command with `schema` export and `validate` subcommands.
   - **[Issue #35](https://github.com/jesusdmedinac/KMP-CLI/issues/35)**: Implement static Compose Kotlin code transpiler in `kmp sdui convert`.
