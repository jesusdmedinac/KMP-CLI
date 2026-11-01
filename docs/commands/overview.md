# CLI Commands Overview

The `kmp` command line tool provides a cohesive suite of commands for the entire Kotlin Multiplatform application lifecycle.

---

## Command Reference Matrix

| Command | Purpose | Key Flags | Primary Output |
| :--- | :--- | :--- | :--- |
| [`kmp doctor`](doctor.md) | Workstation environment health & prerequisites check | `--json` | Diagnostics tree |
| [`kmp create`](create.md) | Multiplatform project scaffolding | `--template`, `--targets`, `--package`, `--wizard` | Ready-to-build project |
| [`kmp describe`](describe.md) | Dual build-system inspector (Gradle & `module.yaml`) | `--json` | Architecture & platform targets |
| [`kmp analyze`](analyze.md) | Dependency matrix & version catalog audit | `--json` | Dependency audit table |
| [`kmp migrate`](migrate.md) | Strangler Fig migration for React Native & Flutter | `audit`, `plan`, `execute`, `--pathway` | Migrated KMP project |
| [`kmp skills`](skills.md) | AI Agent Skills package manager | `list`, `search`, `install`, `verify` | Certified `SKILL.md` runbooks |
| [`kmp mcp`](mcp.md) | Stdio Model Context Protocol server | None | JSON-RPC 2.0 endpoint |

---

## Global Options

- `--help`, `-h`: Show help and command options.
- `--version`: Show CLI version and build metadata.
