# Agent Skills Hub Overview

The **Agent Skills Hub** is the world's first curated, open-source skill repository built specifically for Kotlin Multiplatform AI agent workflows.

---

## What is a Skill?

A **Skill** is a self-contained, versioned directory containing a `SKILL.md` instruction file (with YAML frontmatter) and optional reference materials, code samples, or validation scripts. 

When an AI coding agent works on a project, it uses **progressive disclosure**:
1. Only the skill name, description, and triggers are indexed initially.
2. The agent reads the full `SKILL.md` runbook **only when the task matches the skill's domain**.
3. The agent follows the certified procedures, avoiding AI hallucinations, outdated APIs, or invalid platform imports.

---

## Anatomy of a KMP Skill

```markdown
---
name: kotlin-ui-sdui-compose
description: Server-Driven UI architecture with json-to-compose, remote document fetching, dual-tier caching, and Stale-While-Revalidate in Kotlin Multiplatform.
version: 1.0.0
author: jesusdmedinac
category: ui
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - server-driven ui
  - sdui
  - json to compose
---

# Instructions and Certified Architecture Runbook...
```
