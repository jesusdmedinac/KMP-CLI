Feature: KMP Project Analysis and Metadata
  As an AI agent or developer tool
  I want to analyze a Kotlin Multiplatform project
  So that I can understand its modules, targets, and dependencies without manual inspection

  Scenario: Describe KMP project modules and targets
    Given a valid KMP project with Android, iOS, and Desktop targets
    When I execute "kmp describe --json"
    Then the output should be valid JSON
    And the JSON should list "android", "iosArm64", "iosX64", and "jvm" under "targets"
    And the JSON should specify the Kotlin version and root project name

  Scenario: Analyze dependencies from version catalog
    Given a KMP project with a "gradle/libs.versions.toml" file
    When I execute "kmp analyze dependencies --json"
    Then the output should enumerate declared libraries, plugins, and versions
