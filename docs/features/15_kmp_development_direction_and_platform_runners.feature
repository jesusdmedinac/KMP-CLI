Feature: KMP Development Direction & Platform Runners
  As a Kotlin Multiplatform developer or AI agent
  I want kmp-cli to support the canonical 4-stage development direction (Living Foundation, Shell & Routing, Capabilities & Data, Feature Delivery)
  So that projects establish verified runnable entry points before screen iteration and make informed library choices via klibs.io

  Scenario: Describe active platform runners in project inspection
    Given a KMP project with declared targets in `build.gradle.kts`
    When I run `kmp describe --json`
    Then the output should report the status of executable runners for each declared target
    And identify whether Desktop, Wasm, iOS, or Android entry points are present

  Scenario: Guide developer to establish living foundation when runners are missing
    Given a KMP application with targets declared but missing platform runners
    When I run `kmp doctor --architecture`
    Then the diagnostic should provide constructive guidance on establishing the living foundation
    And suggest the minimum entry point code for each active target to enable immediate testing

  Scenario: Scaffold projects with complete platform runners per target
    Given I scaffold a new application using `kmp create` with specific targets
    When the template processor completes
    Then executable runners must be generated exclusively for the selected targets (Zero-Bloat)
    And embedded templates should include runnable Desktop, Wasm, iOS, and Android entry points

  Scenario: Query klibs.io for verified library compatibility in Stage 3
    Given an agent or developer exploring libraries for shared capabilities
    When querying library recommendations via kmp-cli or klibs.io MCP
    Then coordinates and versions verified for the project's exact declared targets should be returned
    And pre-stable or incompatible library targets should be flagged before build time
