Feature: Kotlin Foundation Grant Winners Skills Catalog
  As a Kotlin Multiplatform developer or AI coding agent using kmp-cli
  I want kmp-cli to provide curated, certified Agent Skills for Kotlin Foundation Grant Winner libraries
  So that I have canonical multiplatform recipes, dependency coordinates, and strict boundary hygiene for essential ecosystem libraries

  Scenario: Catalog registration and discoverability of Grant Winner skills
    Given the KMP Skills catalog
    When queried via `kmp skills list` or `kmp skills search grant-winner`
    Then all official Kotlin Foundation Grant Winner skills are discovered with canonical IDs and aliases:
      | canonical_id                       | legacy_alias         | category     | grant_year |
      | kotlin-media-coil                  | kmp-coil             | media        | 2023       |
      | kotlin-data-multiplatform-settings | kmp-settings         | persistence  | 2024       |
      | kotlin-data-store5                 | kmp-store5           | persistence  | 2024       |
      | kotlin-architecture-orbit-mvi      | kmp-orbit-mvi        | architecture | 2024       |
      | kotlin-ui-compose-rich-editor      | kmp-rich-editor      | ui           | 2024       |
      | kotlin-interop-swift-coroutines    | kmp-swift-coroutines | interop      | 2023       |
      | kotlin-web-kobweb                  | kmp-kobweb           | web          | 2025       |
      | kotlin-cloud-firebase              | kmp-firebase         | cloud        | 2025       |
    And each entry is tagged with `grant-winner` and its verified platform targets

  Scenario: Describe Persistence and Media Grant Winner skills (Phase 1)
    Given the KMP Skills Hub
    When an agent or developer queries `kmp skills describe` for Phase 1 skills:
      | skill_id                           | primary_feature                                                |
      | kotlin-data-multiplatform-settings | key-value preferences with ObservableSettings flow             |
      | kotlin-media-coil                  | AsyncImage, memory/disk cache, and Ktor3 engine configuration  |
      | kotlin-data-store5                 | typed Fetcher, SourceOfTruth, and offline-first cache streaming|
    Then the skill outputs procedural instructions adhering to agentskills.io format
    And enforces pure `commonMain` boundary hygiene without leaking Android or platform SDKs

  Scenario: Describe Architecture and Rich UI Grant Winner skills (Phase 2)
    Given the KMP Skills Hub
    When an agent or developer queries `kmp skills describe` for Phase 2 skills:
      | skill_id                           | primary_feature                                                  |
      | kotlin-architecture-orbit-mvi      | ContainerHost, intent { reduce { } }, and side-effect testing     |
      | kotlin-ui-compose-rich-editor      | rememberRichTextState, styling toolbar, and HTML/Markdown export |
    Then the skill provides idiomatic Compose Multiplatform code snippets
    And includes architectural guardrails preventing anti-patterns

  Scenario: Describe Platforms, Interop, and Cloud Grant Winner skills (Phase 3)
    Given the KMP Skills Hub
    When an agent or developer queries `kmp skills describe` for Phase 3 skills:
      | skill_id                           | primary_feature                                                   |
      | kotlin-interop-swift-coroutines    | Swift modern concurrency, AsyncSequence, and SKIE interop         |
      | kotlin-web-kobweb                  | full-stack Compose HTML, file-based routing, Silk UI tokens       |
      | kotlin-cloud-firebase              | GitLive multiplatform SDK, coroutines Flow, kotlinx serialization |
    Then the procedural instructions guide multiplatform integration across native targets

  Scenario: Validate Grant Winner skills manifest and catalog integrity
    Given the `skills/` directory and `DefaultSkillsCatalog`
    When validated by the seed skills verification test suite
    Then all 8 Grant Winner skills pass YAML frontmatter parsing, tag indexing, and alias resolution
    And report zero structural errors
