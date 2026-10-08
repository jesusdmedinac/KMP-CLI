Feature: KMP Project Scaffolding Engine & JetBrains Wizard

  As a Kotlin Multiplatform developer or AI agent
  I want to scaffold modern KMP projects using official JetBrains templates or an interactive wizard
  So that I can start developing cross-platform applications immediately with zero sync errors

  Background:
    Given a clean working environment

  # ─── Gallery Scenarios: Official JetBrains Templates ───

  Scenario: Scaffold from Official JetBrains Shared UI Multiplatform App template
    When the developer executes "kmp create --template shared-ui --name MySharedApp --package com.example.shared"
    Then a project directory "MySharedApp" should be created
    And the project should contain "gradlew", "gradlew.bat", and "gradle.properties"
    And "gradle.properties" should enable "android.useAndroidX=true"
    And "settings.gradle.kts" should include ":shared", ":androidApp", ":desktopApp", and ":webApp"

  Scenario: Scaffold from Official JetBrains Native UI Multiplatform App template
    When the developer executes "kmp create --template native-ui --name MyNativeApp --package com.example.native"
    Then a project directory "MyNativeApp" should be created
    And "iosApp" should contain a native Xcode project with SwiftUI "ContentView.swift"
    And "shared" should configure a native framework for iOS

  Scenario: Scaffold from Official JetBrains Multiplatform Library template
    When the developer executes "kmp create --template multiplatform-library --name MyLib --package com.example.lib"
    Then a project directory "MyLib" should be created
    And the build script should configure Maven publishing via vanniktech
    And the build script should apply "com.android.kotlin.multiplatform.library"

  Scenario: Scaffold from Official JetBrains Toolchain App template
    When the developer executes "kmp create --template toolchain-shared-ui --name MyToolchainApp"
    Then a project directory "MyToolchainApp" should be created
    And the project should contain the executable launcher "kotlin" and "kotlin.bat"
    And the project should contain "project.yaml" declaring modules

  # ─── Gallery Scenarios: Specialized Templates ───

  Scenario: Scaffold specialized Server-Driven UI starter template for json-to-compose
    When the developer executes "kmp create --template sdui-starter --name MySduiApp --package com.example.sdui"
    Then a project directory "MySduiApp" should be created
    And "composeApp/build.gradle.kts" should declare dependency on "json-to-compose"
    And "composeApp/build.gradle.kts" should use "@OptIn(ExperimentalWasmDsl::class)"
    And the project should contain "gradle.properties" with "android.useAndroidX=true"

  Scenario: Scaffold specialized Fullstack KMP template
    When the developer executes "kmp create --template fullstack --name MyFullstackApp --package com.example.fullstack"
    Then a project directory "MyFullstackApp" should be created
    And "settings.gradle.kts" should include ":server", ":app:androidApp", and ":app:desktopApp"
    And the project should contain "gradle.properties" and Gradle wrapper files

  # ─── Dynamic Wizard Scenarios ───

  Scenario: Run interactive wizard configuring React Web and Ktor Server
    When the developer runs the wizard with build system "gradle", targets "android,ios-swiftui,web-react,server"
    Then the project should contain a "webApp" directory with "package.json" and "vite.config.ts"
    And "shared/build.gradle.kts" should configure TypeScript definitions export
    And the project should contain a "server" module configuring Ktor

  # ─── Cross-Cutting Capabilities: Automation, Permissions, and Formats ───

  Scenario: Scaffold project with customized target platforms
    When the developer executes "kmp create --template shared-ui --name MyCustomApp --targets android,ios"
    Then the generated build configuration should configure "android" and "ios" targets
    And it should not configure desktop or wasm targets

  Scenario: Scaffold project with JSON output for AI agent automation
    When the developer executes "kmp create --template multiplatform-library --name MyJsonLib --package com.example.json --json"
    Then the output should be valid JSON
    And the JSON should specify status "SUCCESS", template "multiplatform-library", and the list of created files

  Scenario: Preserve and enforce execution permissions on wrappers
    When a Gradle or Toolchain project is scaffolded
    Then the wrapper scripts "gradlew" and "kotlin" should have executable permissions
    And if permissions cannot be set, an actionable hint "chmod +x" should be displayed
