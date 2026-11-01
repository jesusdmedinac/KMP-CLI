# `kmp describe`

Inspect any Kotlin Multiplatform project to understand its build system, active target platforms, declared source sets, and compiler configurations.

---

## Usage

```bash
kmp describe [path] [options]
```

## Options

- `path`: Path to the project root directory. Defaults to `.`.
- `--json`: Output machine-readable JSON representation.

---

## Dual Build-System Parser

`kmp describe` seamlessly analyzes both types of KMP projects:

1. **Gradle Multiplatform**: Inspects root `build.gradle.kts`, `settings.gradle.kts`, and subprojects (`:shared`, `:composeApp`, `:androidApp`).
2. **Declarative Kotlin Toolchain 0.12+**: Parses `module.yaml` and product declarations (`android/app`, `ios/app`, `jvm/app`).

---

## Example Output

```text
📋 Project Architecture Description

Project: ReactNativeExpoKmp
Build System: Gradle (build.gradle.kts)
Kotlin Version: 2.1.20
Compose Multiplatform: 1.7.3

Declared Target Platforms (2):
  • Android (minSdk: 24, compileSdk: 35)
  • iOS (iosArm64, iosSimulatorArm64)

Subprojects & Modules (2):
  • :shared (Multiplatform Library)
  • :androidApp (Android Application Runner)
```
