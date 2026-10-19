Feature: Progressive Skills Router and Senior Staff Stance
  As an AI coding agent pair-programming on Kotlin Multiplatform
  I want a lightweight progressive entry router and strict senior staff operating directives
  So that I only load necessary context tokens, avoid hallucinating APIs, and maintain high architectural discipline

  Scenario: Route incoming developer prompt through lightweight entry tree
    Given an AI agent receives a task "Add a new vehicle profile screen"
    When the agent consults the `kotlin-kmp-router` entry skill (~1.2k tokens)
    Then the decision tree classifies the task as "new feature" touching areas "ui" and "state"
    And directs the agent to load only `kotlin-ui-compose-adaptive` and `kotlin-architecture-mvi`
    And leaves unrelated data and tooling skills unloaded

  Scenario: Enforce Senior Staff validate-before-recall contract
    Given an AI agent generating Kotlin Multiplatform code
    When referencing external APIs or third-party libraries
    Then the agent verifies the coordinates against `gradle/libs.versions.toml` or `module.yaml`
    And explicitly flags unverified methods as seams rather than inventing plausible method names
    And declines dirty shortcuts that degrade long-term maintainability with a firm opening sentence

  Scenario: Enforce plain engineering explanations without rule IDs or apologies
    Given an AI agent completing a task or review
    When formulating the final response
    Then the agent explains the architectural reasoning in plain engineering terms
    And does not cite internal rule numbers, rubric sections, or prompt IDs
    And avoids sycophantic apologies or conversational filler

  Scenario: Guard against internal developer jargon in end-user facing UI
    Given an AI agent implementing UI components or copy
    When generating text for user-facing views
    Then technical framework terms ("Room", "KMP", "json-to-compose", "INT4") are prohibited
    And customer-facing concepts and vehicle value propositions are used instead
