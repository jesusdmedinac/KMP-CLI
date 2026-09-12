---
name: kmp-toolchain-migration
description: Step-by-step procedural guide to migrate from legacy build.gradle.kts to declarative Kotlin Toolchain 0.12+ module.yaml.
version: 1.0.0
author: jesusdmedinac
tags:
  - toolchain
  - amper
  - gradle-migration
  - module-yaml
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - kotlin toolchain
  - module.yaml
  - migrate gradle to amper
compatibility:
  kotlin: ">=2.1.0"
  toolchain: ">=0.12.0"
---

# Migrating from Gradle to The Kotlin Toolchain (`module.yaml`)

Starting with Kotlin 2.0+ and JetBrains Toolchain 0.12+, projects can replace verbose Gradle DSL boilerplate with declarative `module.yaml` configuration.

## 1. Project Root Configuration

A Kotlin Toolchain project replaces `settings.gradle.kts` and root `build.gradle.kts` with:
1. `project.yaml`: Declares root repository metadata, modules list, and toolchain version.
2. `./kotlin`: The standalone native toolchain executable wrapper.

### `project.yaml`:
```yaml
modules:
  - shared
  - app

settings:
  kotlin:
    version: 2.1.10
```

## 2. Converting `build.gradle.kts` to `module.yaml`

Replace `shared/build.gradle.kts` with `shared/module.yaml`:

```yaml
product:
  type: lib
  platforms:
    - jvm
    - android
    - iosArm64
    - iosSimulatorArm64

dependencies:
  - org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0
  - org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3

settings:
  compose: enabled
```

## 3. Key Differences from Gradle
- **Source set conventions**: Default source layout matches `src/` (common code) and `src@<platform>/` (target-specific code) instead of nested `src/commonMain/kotlin`.
- **Hot Reload**: Applications configured with `toolchain-app` support instant Compose UI Hot Reload on desktop and mobile simulators without restarting Gradle daemons.
- **Fast cold-starts**: Toolchain invocations execute in sub-second time without Gradle daemon warmup.
