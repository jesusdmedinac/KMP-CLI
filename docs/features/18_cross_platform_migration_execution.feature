Feature: Cross-Platform Migration Execution (kmp migrate execute)
  As a mobile engineer, tech lead, or AI agent migrating legacy cross-platform codebases
  I want KMP CLI to orchestrate end-to-end migration execution into idiomatic Kotlin & Compose Multiplatform
  So that I can autonomously or in a guided manner scaffold, translate, and verify working multiplatform applications

  Background:
    Given the official upstream cross-platform baseline guides:
      | Framework    | Canonical Guide URL                         | Standard Bootstrap Command  |
      | React Native | https://reactnative.dev/docs/environment-setup | npx create-expo-app@latest  |
      | Flutter      | https://docs.flutter.dev/install/quick      | flutter create / VS Code    |

  Scenario: Scaffold destination KMP project and synthesize dependency catalog from legacy audit
    Given an existing React Native or Flutter project at "<sourcePath>"
    When the developer or AI agent runs "kmp migrate execute <sourcePath> --output <targetPath>"
    Then the engine must audit the legacy source project
    And it must scaffold a clean Compose Multiplatform project at "<targetPath>" targeting active platforms
    And it must synthesize "libs.versions.toml" with the KMP equivalents discovered during audit (e.g. Coil 3, Ktor, Settings, Navigation)
    And it must configure "build.gradle.kts" with the synthesized dependencies

  Scenario: Migrate and normalize visual assets into Compose Multiplatform resources
    Given a legacy project containing static assets (images, icons, fonts)
    When "kmp migrate execute" runs
    Then it must extract images and copy them to "<targetPath>/src/commonMain/composeResources/drawable/"
    And it must extract custom fonts and place them in "<targetPath>/src/commonMain/composeResources/font/"

  Scenario: Generate layered migration manifest and progress tracking for AI agents
    Given a scaffolded destination project
    When "kmp migrate execute" runs
    Then it must emit "migration-manifest.json" categorizing source files into 4 structured layers: Domain Models, Data Sources & Repositories, State Management, and UI Screens
    And it must emit "MIGRATION_PROGRESS.md" with an actionable checklist for autonomous or guided execution

  Scenario: Execute autonomous translation of Domain Models and Data Layer
    Given a migration manifest with Domain and Data layers
    When the migration assistant translates models and services (TypeScript interfaces or Dart classes)
    Then it generates serializable Kotlin data classes with "@Serializable"
    And it generates Ktor Client services replacing axios or http calls
    And the generated Kotlin code compiles successfully with zero errors

  Scenario: Execute autonomous translation of UI Screens to Compose Multiplatform
    Given a migration manifest with UI components
    When the migration assistant translates React Native JSX or Flutter Widgets to Compose Multiplatform
    Then it maps container components to "@Composable" layouts (Column, Row, Box, Scaffold)
    And it maps UI state to StateFlow and collectAsState
    And each translated screen includes a "@Preview" composable
    And the destination project successfully compiles and passes verification

  Scenario: Support guided migration strategy with interactive review checkpoints
    Given a migration execution with "--strategy guided"
    When the CLI processes each layer
    Then it prompts the developer for confirmation after each translated module before proceeding to the next layer

  Scenario: Expose migration execution tools via MCP for AI agent co-pilots
    Given an active "kmp mcp" server session
    When an AI agent lists available tools
    Then it must discover "kmp_migrate_execute" with options for source path, output directory, strategy, and pathway
    And invoking "kmp_migrate_execute" returns the execution manifest and status
