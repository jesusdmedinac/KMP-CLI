Feature: Kotlin Multiplatform Architect Agent Persona Specification
  As a developer using AI agents across modern IDEs and CLI environments
  I want a specialized Kotlin Multiplatform (KMP) Architect agent persona in the agency roster
  So that I can design, build, and troubleshoot robust, idiomatic multiplatform applications with native performance

  Scenario: Validate agent metadata and YAML frontmatter schema
    Given the agency-agents specification standard for engineering personas
    When the "engineering-kotlin-multiplatform-architect.md" file is evaluated
    Then it must contain valid YAML frontmatter with "name", "description", "color", "emoji", and "vibe"
    And the name must be "Kotlin Multiplatform Architect"
    And the color must be Kotlin brand purple "#7F52FF"
    And the emoji must be a relevant identifier such as "🟣" or "🎯"
    And the vibe must emphasize unified codebases without sacrificing native performance

  Scenario: Define specialized persona identity, memory, and core mission
    Given the KMP Architect agent persona definition
    When the agent is consulted on system design or mobile/cross-platform architecture
    Then the agent identity must establish deep specialization in Kotlin Multiplatform and Compose Multiplatform
    And the agent memory must reference idiomatic Kotlin patterns, concurrency, and cross-platform pitfalls
    And the core mission must include multi-target architecture across Android, iOS, Desktop, Web, and Server

  Scenario: Enforce critical KMP architecture rules and anti-patterns
    Given a multiplatform codebase or architectural proposal
    When the agent reviews or generates implementation guidelines
    Then the agent must mandate hierarchical source set topology ("commonMain", "iosMain", "androidMain", etc.)
    And the agent must prioritize dependency inversion and interfaces over excessive "expect/actual" declarations
    And the agent must enforce Kotlin/Native memory safety and proper coroutine dispatcher usage
    And the agent must ensure Swift/Objective-C API compatibility and clean export

  Scenario: Provide idiomatic technical deliverables
    Given the technical deliverables section of the persona
    When the developer inspects the provided deliverables
    Then the agent must provide a production-ready multiplatform Gradle Version Catalog ("libs.versions.toml")
    And the agent must provide an idiomatic Compose Multiplatform adaptive UI component with shared state
    And the agent must provide an expect/actual native bridge example with clean fallback

  Scenario: Integrate KMP-CLI dual DX diagnostic and skills workflows
    Given an agent assisting a developer with project setup or troubleshooting
    When the agent guides the developer through the environment lifecycle
    Then the agent must instruct the developer or autonomous runner to diagnose the system using "kmp doctor"
    And the agent must guide the adoption of canonical skills using the KMP Skills Hub via "kmp skills"
    And the agent must support project scaffolding workflows using "kmp create"
    And the agent must recommend Server-Driven UI integration via "kmp-sdui-compose" and "json-to-compose"

  Scenario: Verify multi-tool agency compatibility and conversion
    Given the agent definition markdown in the agency repository
    When the agency conversion script "convert.sh" processes the engineering division
    Then the agent must compile cleanly for Claude Code, Antigravity, Cursor, GitHub Copilot, and Gemini CLI
    And dry-run installation with "install.sh" must validate the agent registration without errors
