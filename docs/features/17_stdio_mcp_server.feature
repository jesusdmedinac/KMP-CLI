Feature: Stdio Model Context Protocol (MCP) Server
  As an AI coding assistant (Claude, Cursor, Antigravity) or developer
  I want kmp-cli to run as a native stdio Model Context Protocol (MCP) server via `kmp mcp`
  So that AI agents can autonomously inspect KMP environments, run architecture guards, analyze library compatibility, and install skills

  Scenario: Initialize MCP session and negotiate protocol capabilities
    Given a client connects to `kmp mcp` via stdio
    When the client sends an "initialize" request with protocol version "2024-11-05"
    Then the server responds with protocol version "2024-11-05" and server info "kmp-cli"
    And declares "tools" support in its capabilities

  Scenario: List available KMP tools with input schemas
    Given an initialized MCP session
    When the client sends a "tools/list" request
    Then the server returns all core KMP diagnostic and skills tools:
      | tool_name                 | description_keyword                          |
      | kmp_doctor                | environment health diagnostic                |
      | kmp_doctor_architecture   | clean architecture guard linting             |
      | kmp_describe              | project targets modules and platform runners |
      | kmp_analyze_dependencies  | version catalog dependencies and updates     |
      | kmp_analyze_libraries     | klibs.io library compatibility by target     |
      | kmp_list_skills           | curated KMP skills catalog                   |
      | kmp_add_skill             | install canonical or remote skill into project|
    And each tool provides a valid JSON Schema for its parameters

  Scenario: Execute environment diagnostic tool calling (kmp_doctor and kmp_doctor_architecture)
    Given an initialized MCP session
    When the client calls "kmp_doctor"
    Then the result contains structured health diagnostics for JDK and Kotlin Toolchain
    And when the client calls "kmp_doctor_architecture" for a target project path
    Then the result reports architectural rule violations and living foundation status

  Scenario: Execute project inspection and library analysis tools (kmp_describe and kmp_analyze_libraries)
    Given an initialized MCP session
    When the client calls "kmp_describe" with a project path
    Then the result details declared targets, modules, and platform runners
    And when the client calls "kmp_analyze_libraries" with a query and project path
    Then the result evaluates target-aware compatibility against klibs.io

  Scenario: Execute skills management tools (kmp_list_skills and kmp_add_skill)
    Given an initialized MCP session
    When the client calls "kmp_list_skills"
    Then the catalog of available canonical and grant-winner skills is returned
    And when the client calls "kmp_add_skill" with a skill ID and destination path
    Then the skill is installed and the result confirms installation status

  Scenario: Gracefully handle unknown methods, invalid parameters, and ping requests
    Given an initialized MCP session
    When the client sends a "ping" request
    Then the server returns an empty result object
    And when the client sends an unsupported method or invalid tool arguments
    Then the server returns standard JSON-RPC 2.0 error codes (-32601, -32602) without crashing
