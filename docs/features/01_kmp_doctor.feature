Feature: KMP Environment Doctor
  As a Kotlin Multiplatform developer or AI agent
  I want to diagnose my development environment
  So that I know if all required tools (JDK, Android SDK, Xcode) are configured properly before building

  Scenario: Run doctor with human-friendly output when all tools are installed
    Given a system with valid JDK, Android SDK, and Xcode installed
    When I execute "kmp doctor"
    Then the exit code should be 0
    And the output should display checkmarks for JDK, Android SDK, and Xcode
    And a summary message "Your environment is ready for KMP development!" should appear

  Scenario: Run doctor with JSON output for AI agent consumption
    Given a system with valid JDK and missing CocoaPods
    When I execute "kmp doctor --json"
    Then the exit code should be 0
    And the output should be valid JSON
    And the JSON should contain an array "checks" with status "PASS" for "jdk"
    And the JSON should contain a recommendation for installing "cocoapods"

  Scenario: Run doctor when a critical requirement is missing
    Given a system without Java installed
    When I execute "kmp doctor"
    Then the exit code should be 1
    And the output should highlight "Java Development Kit (JDK)" in red
    And a remediation instruction for installing OpenJDK should be suggested
