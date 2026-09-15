Feature: Canonical KMP Skills Curation & Ethical Attribution
  As a Kotlin Multiplatform developer or AI coding assistant
  I want a rich, curated catalog of production-ready, ethically-attributed KMP skills
  So that I can build scalable multiplatform applications adhering to canonical community best practices

  Scenario: Skill manifest conforms strictly to SKILL.md schema
    Given a new canonical skill directory under "skills/<skill-id>"
    When the "SKILL.md" document is parsed by SkillParser
    Then the frontmatter must contain mandatory fields "name", "description", and "version"
    And "tags" and "targets" must be non-empty lists
    And "compatibility" must define Kotlin and Compose Multiplatform minimum versions

  Scenario: Canonical skill includes transparent and ethical attribution
    Given a canonical skill markdown document
    When the document body is validated
    Then it must contain a "References & Canonical Sources" section
    And it must cite the original author, library repository, and license
    And it must provide runnable, multiplatform-compatible code recipes for commonMain

  Scenario: Skill is registered in catalog and resolvable via CLI
    Given a valid curated skill in "skills/<skill-id>/SKILL.md"
    When the skill is registered in "skills/catalog.json"
    Then "kmp skills list" must include the new skill in the catalog output
    And "kmp skills describe <skill-id>" must output its complete metadata and instructions
    And "kmp skills add <skill-id>" must successfully install the skill into ".agents/skills/<skill-id>/SKILL.md"
