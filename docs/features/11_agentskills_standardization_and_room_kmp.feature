Feature: AgentSkills Standardization and Room KMP Integration
  As a developer or AI coding agent using KMP CLI
  I want skills to follow the official JetBrains `agentskills.io` naming standard and include Room KMP
  So that skills are natively compatible with Claude Code, Codex, Cursor, and modern Android/KMP standards

  Scenario: Align local skill names and tags with JetBrains agentskills.io standard
    Given the KMP Skills catalog
    When inspected via `kmp skills list`
    Then skills reflect canonical identifiers:
      | canonical_id                                  | legacy_alias                | category   |
      | kotlin-tooling-gradle-to-kotlin-toolchain     | kmp-toolchain-migration     | tooling    |
      | kotlin-ui-compose-adaptive                    | kmp-compose-adaptive        | ui         |
      | kotlin-ui-sdui-compose                        | kmp-sdui-compose            | ui         |
      | kotlin-networking-ktor                        | kmp-ktor-networking         | networking |
      | kotlin-concurrency-coroutines                 | kmp-coroutines-concurrency  | concurrency|
    And each skill contains YAML frontmatter compliant with agentskills.io specification

  Scenario: Curate official kotlin-data-room-kmp skill for SQLite multiplatform
    Given the KMP Skills Hub
    When a developer queries `kmp skills describe kotlin-data-room-kmp`
    Then the skill presents setup guidelines for `androidx.room` 2.7.0+ on SQLite
    And includes multiplatform DAO Flow definitions, database builder expect/actual, and schema export rules

  Scenario: Maintain backward compatibility with legacy skill IDs in catalog search
    Given a developer executes `kmp skills add kmp-toolchain-migration`
    When the CLI resolves the identifier
    Then the CLI informs the user of the canonical name "kotlin-tooling-gradle-to-kotlin-toolchain"
    And installs the skill successfully without failing

  Scenario: Validate skills manifest against agentskills.io schema
    Given any skill directory inside `skills/`
    When validated by the skills verification harness
    Then the `SKILL.md` file contains valid `name`, `description`, and `metadata` frontmatter
    And passes validation without errors
