Feature: KMP Environment Doctor
  As a Kotlin Multiplatform developer or AI agent
  I want to diagnose my development environment
  So that I know if all required tools (JDK, Android SDK, Xcode, Kotlin Toolchain, Wasm runtimes) are configured properly before building

  Scenario: Run doctor delegating to kdoctor on macOS when available
    Given a macOS system with "kdoctor" installed in PATH
    When I execute "kmp doctor"
    Then "kmp doctor" should invoke "kdoctor" for core mobile diagnostics
    And the output should merge kdoctor results with extended checks
    And the output should check for Kotlin Toolchain 0.12+ and Web/Wasm prerequisites
    And the exit code should reflect overall health

  Scenario: Run doctor with JSON output for AI agent consumption
    Given a system with valid JDK, installed kdoctor, but missing Kotlin Toolchain
    When I execute "kmp doctor --json"
    Then the exit code should be 0
    And the output should be valid JSON adhering to the doctor report schema
    And the JSON should contain "checks" with "kdoctor", "jdk", and "kotlinToolchain"
    And the JSON should include actionable "remediations" for missing tools

  Scenario: Run doctor when kdoctor is not installed on macOS
    Given a macOS system where "kdoctor" is not found in PATH
    When I execute "kmp doctor"
    Then "kmp doctor" should execute its built-in native checks
    And the output should suggest installing kdoctor via "brew install kdoctor"

  Scenario: Run doctor when a critical requirement is missing
    Given a system without Java installed
    When I execute "kmp doctor"
    Then the exit code should be 1
    And the output should highlight "Java Development Kit (JDK)" in red
    And an actionable remediation instruction for installing OpenJDK should be provided
