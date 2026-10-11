Feature: Universal Remote Skills Hub
  As a developer or AI coding agent using KMP CLI
  I want to install skills directly from remote Git repositories like JetBrains and community repos
  So that KMP CLI acts as a universal package manager for Kotlin agent skills

  Scenario: Install skill pack directly from GitHub repository
    Given a developer runs `kmp skills add Kotlin/kotlin-agent-skills`
    When the CLI executes the command
    Then it fetches the skills from GitHub repository "Kotlin/kotlin-agent-skills"
    And installs all discovered skills into the active skills directory
    And outputs success status with installed skill count

  Scenario: Install scoped skill from remote repository
    Given a developer runs `kmp skills add Meet-Miyani/compose-skill:compose-architecture`
    When the CLI executes the command
    Then it downloads only the scoped sub-skill "skills/compose-architecture"
    And installs it into the local project skills directory
    And prints confirmation with path to installed skill

  Scenario: Auto-detect target agent skills directory
    Given a project containing directory ".claude/skills"
    When `kmp skills add` is executed without explicit path flags
    Then the CLI detects the Claude Code environment
    And installs the skill into ".claude/skills/"
    Given a project containing directory ".agents/skills"
    When `kmp skills add` is executed
    Then the CLI detects Antigravity/Cursor environment and installs into ".agents/skills/"

  Scenario: Update remote installed skills via kmp skills update
    Given a previously installed remote skill "Kotlin/kotlin-agent-skills"
    When the developer executes `kmp skills update`
    Then the CLI checks for remote updates and pulls the latest versions
    And reports updated skills
