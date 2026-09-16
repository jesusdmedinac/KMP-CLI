Feature: Server-Driven UI Integration & Synergy with json-to-compose
  As a Kotlin Multiplatform developer or AI coding assistant
  I want first-class Server-Driven UI tooling, templates, and canonical skills in KMP-CLI
  So that I can easily scaffold, validate, cache, preview, and transpile dynamic Compose UIs driven by json-to-compose

  Background:
    Given a working KMP-CLI installation with access to the KMP Skills Hub

  Scenario: Scaffold a new multiplatform project using the sdui-starter template
    Given the developer invokes "kmp create --template sdui-starter --name MySduiApp"
    When the scaffolding generator completes execution
    Then a new project directory "MySduiApp" is created
    And the project includes Android, iOS, Desktop, and Wasm targets
    And "gradle/libs.versions.toml" contains "json-to-compose" and Ktor Client dependencies
    And "commonMain" includes a pre-configured SduiEngine with L1/L2 cache storage
    And a sample screen using RemoteComposeDocument is provided with offline fallback assets

  Scenario: Discover, inspect, and install the canonical kmp-sdui-compose skill
    Given the KMP Skills Hub catalog
    When the user runs "kmp skills find sdui"
    Then "kmp-sdui-compose" is listed in the search results
    And running "kmp skills describe kmp-sdui-compose" displays instructions for ComposeDocument, Remote Engine, and Caching
    And running "kmp skills add kmp-sdui-compose" installs the skill into ".agents/skills/kmp-sdui-compose/SKILL.md"

  Scenario: Export the json-to-compose JSON schema for IDE and AI validation
    Given the command "kmp sdui schema"
    When executed with "--output schema.json" or "--json"
    Then a valid JSON Schema is generated
    And the schema enumerates all supported ComposeNode types, Modifiers, and ComposeAction definitions
    And it enables IDE autocompletion and agent schema verification

  Scenario: Validate a local or remote SDUI JSON document against the schema
    Given a local JSON file "screen.json" representing a ComposeDocument
    When the user runs "kmp sdui validate screen.json"
    Then the CLI verifies node types, property constraints, and action configurations
    And reports a zero exit code for valid schemas or detailed error diagnostics for invalid fields

  Scenario: Transpile dynamic SDUI JSON into static Jetpack Compose Kotlin code
    Given a valid "login_screen.json" containing a Column with TextField and Button nodes
    When the user executes "kmp sdui convert login_screen.json --output LoginScreen.kt"
    Then the tool transpiles the declarative JSON into an idiomatic @Composable fun LoginScreen()
    And "initialState" entries are translated to Compose state holders
    And actions are mapped into Kotlin callbacks
    And the generated Kotlin file compiles successfully without syntax errors
