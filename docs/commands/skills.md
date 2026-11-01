# `kmp skills`

The open package manager for certified Kotlin Multiplatform Agent Skills (`SKILL.md`). Empowers AI coding assistants (Antigravity, Claude Code, Cursor, Copilot) with authoritative runbooks and recipes.

---

## Commands

### `kmp skills list`
List all certified skills in the catalog:
```bash
kmp skills list
```

### `kmp skills search <query>`
Search for skills by keyword, category, or technology:
```bash
kmp skills search sdui
kmp skills search persistence
```

### `kmp skills install <id>`
Installs a skill into your current repository or agent configuration:
```bash
kmp skills install kotlin-ui-sdui-compose
```

### `kmp skills verify`
Validates that installed skills match the active project build configuration and targets.
```bash
kmp skills verify
```

---

## The Three-Tier Hierarchy of Truth

All skills bundled in `kmp-cli` strictly follow the canonical hierarchy of truth:

1. **Tier 1: Contextual Truth**: The developer's real project files (`build.gradle.kts` / `module.yaml`, active targets).
2. **Tier 2: Canonical Authority**: Kotlin Foundation ecosystem (JetBrains, AndroidX KMP, CashApp, Touchlab).
3. **Tier 3: Procedural Operational Runbooks**: Certified recipes from the KMP Skills Hub.
