Feature: KMP Project Scaffolding Engine & kmp create Subcommand
  As an AI agent or Kotlin Multiplatform developer
  I want to scaffold modern KMP projects with sensible defaults, multiple architecture templates, and build system options
  So that I can start developing cross-platform applications immediately without boilerplate configuration errors

  Background:
    Given a clean working environment

  Scenario: Scaffold a Compose Multiplatform project with Gradle
    When the developer executes "kmp create --template compose-multiplatform --name MyComposeApp --package com.example.app"
    Then a project directory "MyComposeApp" should be created
    And the project should contain "settings.gradle.kts" and "gradle/libs.versions.toml"
    And the project should contain "composeApp/build.gradle.kts"
    And "composeApp/src/commonMain/kotlin/com/example/app/App.kt" should contain an adaptive Composable entrypoint
    And the version catalog should declare Compose Multiplatform dependencies

  Scenario: Scaffold a Declarative Kotlin Toolchain project
    When the developer executes "kmp create --template toolchain-app --name MyToolchainApp --package com.example.toolchain"
    Then a project directory "MyToolchainApp" should be created
    And the project should contain "project.yaml"
    And the module "app" should contain "app/module.yaml" declaring platforms and dependencies
    And the project should contain "app/src/commonMain/kotlin/com/example/toolchain/Main.kt"

  Scenario: Scaffold a multiplatform library ready for publishing
    When the developer executes "kmp create --template kmp-library --name MyKmpLib --package com.example.lib"
    Then a project directory "MyKmpLib" should be created
    And the library build configuration should configure Maven publishing
    And "src/commonMain/kotlin/com/example/lib/Platform.kt" should contain expect declarations

  Scenario: Scaffold a Fullstack project with Ktor backend and Compose client
    When the developer executes "kmp create --template fullstack --name MyFullstackApp --package com.example.fullstack"
    Then a project directory "MyFullstackApp" should be created
    And "settings.gradle.kts" should include ":server", ":composeApp", and ":shared"
    And ":server" should configure a Ktor HTTP engine
    And ":shared" should contain shared data models

  Scenario: Scaffold a project with customized target platforms
    When the developer executes "kmp create --template compose-multiplatform --name MyCustomApp --targets android,ios"
    Then the generated build configuration should configure "android" and "ios" targets
    And it should not configure desktop or wasm targets

  Scenario: Scaffold project with JSON output for AI agent automation
    When the developer executes "kmp create --template kmp-library --name MyJsonLib --package com.example.json --json"
    Then the output should be valid JSON
    And the JSON should specify status "SUCCESS", template "kmp-library", and the list of created files
