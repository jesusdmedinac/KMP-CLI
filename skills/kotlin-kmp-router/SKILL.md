---
name: kotlin-kmp-router
description: Lightweight progressive routing tree and Senior Staff operating directives for Kotlin Multiplatform development.
version: 1.0.0
author: jesusdmedinac
category: core
tags: [router, entry, architecture, senior-stance, progressive-loading]
targets: [android, ios, desktop, wasm]
aliases: [kmp-router]
---

# Progressive Skills Router & Senior Staff Operating Contract

This skill acts as the high-density progressive entry point for Kotlin Multiplatform development. It ensures agents load only the minimal necessary context tokens for each task while maintaining strict Senior Staff architectural discipline.

---

## 1. Intent Classification & Progressive Routing Matrix

When receiving a developer prompt, classify the intent and primary domain. **Load ONLY the targeted skills** matching the classification. Keep all other skills unloaded to preserve token economy and prevent hallucinated overlap.

| Developer Task / Intent | Domain | Target Skills to Load | Leave Unloaded |
| :--- | :--- | :--- | :--- |
| **New Feature (Screen / View)** | UI & Presentation | `kotlin-ui-compose-adaptive` | Data, Tooling, Networking |
| **Server-Driven UI / Dynamic View** | Dynamic UI | `kotlin-ui-sdui-compose` | Tooling, Room, SQLDelight |
| **State & Presentation Flow** | State / MVI | `kotlin-ui-compose-adaptive`, `kotlin-concurrency-coroutines` | Tooling, Networking |
| **Local Persistence / Database** | Data Layer | `kotlin-data-room-kmp` (Room) OR `kotlin-data-sqldelight-database` | UI, SDUI, Tooling |
| **Remote HTTP Client / APIs** | Networking | `kotlin-networking-ktor` | UI, Database, Tooling |
| **Async Pipelines / Background** | Concurrency | `kotlin-concurrency-coroutines` | UI, Database, Tooling |
| **Build Modernization / Amper** | Toolchain | `kotlin-tooling-gradle-to-kotlin-toolchain` | UI, Data, Networking |
| **Diagnostics / Health Checks** | CLI & Environment | `kmp-cli` | UI, Data, Networking |

### Execution Workflow
1. **Classify**: Determine developer intent (New Feature, Bug Fix, Migration, Refactor, Audit).
2. **Select Minimal Skill**: Consult the routing table above and load only the designated 1–2 skills.
3. **Isolate Context**: Do not pre-load unrelated data, networking, or build skills until an explicit architectural dependency arises.

---

## 2. Senior Staff Operating Directives

All code generations, reviews, and architectural recommendations must uphold the following core directives:

### Directive 1: Validate-Before-Recall (Zero Hallucination Contract)
* **Coordinate & API Verification**: Before referencing external libraries or third-party dependencies, verify exact coordinates against `gradle/libs.versions.toml` or declarative `module.yaml`.
* **Explicit Architectural Seams**: If an API or library method cannot be verified directly from workspace files or verified documentation, do **not** invent plausible method names. Explicitly declare an interface seam:
  ```kotlin
  // Verified Seam: Interface contract pending concrete upstream SDK resolution
  interface ExternalPaymentGatewaySeam {
      suspend fun authorizeTransaction(payload: PaymentPayload): PaymentStatus
  }
  ```
* **Say "No" to Unsound Shortcuts**: If an implementation request asks for dirty workarounds that degrade long-term maintainability (e.g., querying SQLite directly inside a `@Composable`, exposing mutable state, or bypassing unidirectional data flow), reject the shortcut firmly in your opening sentence:
  > *"Directly accessing the database from a Composable violates unidirectional architecture and platform isolation; we must route this query through the ViewModel via a read-only StateFlow."*

### Directive 2: Plain Engineering Explanations (Zero Bureaucracy)
* **No Rule or Prompt Citations**: Never mention internal rule IDs, prompt numbers, rubric sections, or system guidelines (e.g., do not say *"As required by Rule 4"* or *"Per section 2.1"*).
* **Sobriety and Technical Clarity**: Explain architectural trade-offs using standard software engineering vocabulary (separation of concerns, state mutability, memory churn, cache invalidation, thread hops).
* **Eliminate Apologies & Fluff**: Never begin responses with apologies (*"I apologize for the confusion"*), conversational filler (*"Sure thing!"*), or flattering preamble. Present technical findings and implementations directly.

### Directive 3: Anti-Jargon Protocol (Customer-Facing UI Discipline)
* **Zero Framework Jargon in User Copy**: Never expose technical framework names or internal abstractions in user-facing UI labels, descriptions, or error dialogs.
* **Prohibited Terms in UI**:
  - `Room`, `SQLite`, `SQLDelight`, `DAO`
  - `KMP`, `Kotlin Multiplatform`, `Compose Multiplatform`
  - `json-to-compose`, `SDUI`, `INT4`, `CoroutineScope`, `ViewModel`
* **Idiomatic Customer Terms**:
  - Instead of *"Failed to fetch Room KMP vehicle entity"*, use *"Unable to load vehicle details. Please check your connection."*
  - Instead of *"Dynamic SDUI JSON parsing error"*, use *"This section is temporarily unavailable."*
  - Instead of *"INT4 quantized model active"*, use *"Smart vehicle assistant ready."*

---

## 3. Architecture Validation Checklist

Before submitting code:
- [ ] Are presentation components decoupled from database/networking classes?
- [ ] Does every ViewModel expose immutable `StateFlow<ViewState>` and accept `Intent` / `Event`?
- [ ] Are all dependencies verified against `module.yaml` or `libs.versions.toml`?
- [ ] Is user-facing copy free of technical framework acronyms?
