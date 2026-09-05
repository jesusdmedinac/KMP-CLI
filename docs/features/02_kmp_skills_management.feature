Feature: KMP Skills Repository Management
  As a developer or AI assistant
  I want to discover, list, and install curated KMP skills
  So that my local environment or agent is equipped with up-to-date best practices and procedural knowledge

  Scenario: List available skills in the catalog
    Given an initialized KMP CLI environment
    When I execute "kmp skills list"
    Then the output should display a formatted list of available skills
    And the list should contain "kmp-compose-adaptive" with its summary

  Scenario: Search for specific skills by keyword
    Given an initialized KMP CLI environment
    When I execute "kmp skills find compose"
    Then only skills matching "compose" should be displayed

  Scenario: Describe a skill to inspect its instructions and metadata
    Given an initialized KMP CLI environment
    When I execute "kmp skills describe kmp-ktor-networking"
    Then the output should show the skill description, compatibility, and usage guidelines

  Scenario: Install a skill into the agent configuration directory
    Given an initialized KMP CLI environment
    When I execute "kmp skills add kmp-sqldelight-database"
    Then the skill directory should be created under the active agents skills path
    And the file "SKILL.md" should be installed with valid frontmatter
