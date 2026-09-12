---
name: kmp-cli
description: Procedural guide for AI agents and human developers on using the KMP CLI for environment diagnostics, skills management, and project analysis.
version: 1.0.0
author: jesusdmedinac
tags:
  - cli
  - tooling
  - doctor
  - skills
  - diagnostics
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - kmp cli
  - kmp doctor
  - diagnose environment
  - kmp skills
compatibility:
  kotlin: ">=2.0.0"
---

# KMP CLI Developer & Agent Guide

The Kotlin Multiplatform CLI (`kmp`) provides unified environment diagnostics, agent skill management, and project inspection for Kotlin Multiplatform projects.

## 1. Environment Diagnostics (`kmp doctor`)

Run diagnostics before compiling or creating projects to ensure host prerequisites are met.

### Interactive Mode:
```bash
kmp doctor
```
Displays ANSI-formatted checkmarks (`✓`), warnings (`!`), and failures (`✗`) along with actionable remediation commands.

### Agent / Machine Mode (`--json`):
```bash
kmp doctor --json
```
Emits a structured JSON report adhering to `DiagnosticReport`:
- `overallStatus`: `"SUCCESS"`, `"WARNING"`, or `"FAILURE"`.
- `isHealthy`: Boolean (`false` if any critical failure exists).
- `checks`: Array of check items (`kdoctor`, `jdk`, `kotlinToolchain`), each containing `id`, `title`, `status`, `message`, `details`, and `remediation`.
- If `remediation` is present, execute `remediation.command` to automatically resolve the environment issue.

## 2. Managing Agent Skills (`kmp skills`)

Discover and install certified procedural skills formatted as `SKILL.md`:

```bash
# List all certified skills in the catalog
kmp skills list

# Search for skills matching keywords
kmp skills find compose

# Inspect detailed instructions and metadata
kmp skills describe kmp-compose-adaptive

# Install skill into active project (.agents/skills/<id>/)
kmp skills add kmp-compose-adaptive

# Install skill globally (~/.kmp/skills/<id>/)
kmp skills add kmp-compose-adaptive --global
```

## 3. Exit Codes
- `0`: Success or warnings (environment is operable).
- `1`: Critical failure detected in required tools.
