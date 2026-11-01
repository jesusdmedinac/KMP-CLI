# Quickstart

Get up and running with `kmp-cli` in less than 2 minutes.

---

## 1. Diagnose Your Environment

Before scaffolding or building a Kotlin Multiplatform project, ensure your workstation has the required tools configured (JDK, Android SDK, Xcode, CocoaPods):

```bash
kmp doctor
```

Sample output:
```text
Environment Diagnostics:
  ✓ JDK: 21.0.10 (Amazon.com Inc.)
  ✓ Android SDK: /Users/dev/Library/Android/sdk (Platforms: android-35)
  ✓ Xcode: 16.2 (Build 16C5032a)
  ✓ CocoaPods: 1.16.2
  ✓ Node.js & Wasm: v22.14.0
Everything looks healthy for Kotlin Multiplatform development!
```

!!! tip "For AI Coding Agents"
    Run with `--json` to produce machine-parsable diagnostics:
    ```bash
    kmp doctor --json
    ```

---

## 2. Scaffold a New Project

Create a modern, zero-bloat Kotlin Multiplatform app using the interactive wizard or command-line flags:

```bash
kmp create MyAwesomeApp --targets android,ios --template shared-ui
```

### Available Official Templates:
- `shared-ui`: Compose Multiplatform on Android, iOS, Desktop, and Web.
- `native-ui`: Compose Multiplatform on Android/Desktop with 100% native SwiftUI on iOS.
- `json-to-compose-sample-app`: Server-Driven UI starter powered by `json-to-compose`.
- `toolchain-shared-ui`: Declarative Kotlin Toolchain 0.12+ (`module.yaml`).
- `fullstack`: Fullstack Kotlin with Ktor server and Compose Multiplatform client.

---

## 3. Inspect an Existing Project

Navigate into any existing Kotlin Multiplatform repository and inspect its build system, targets, and compiler configuration:

```bash
cd MyAwesomeApp
kmp describe
```

`kmp` automatically detects whether the project uses Gradle (`build.gradle.kts`) or declarative Kotlin Toolchain (`module.yaml`).

---

## 4. Install Agent Skills

Supercharge your AI coding assistant (Antigravity, Claude Code, Cursor) with certified operational runbooks:

```bash
# List available skills in the catalog
kmp skills list

# Install a skill to your project
kmp skills install kotlin-ui-sdui-compose
```

Now your AI assistant knows exactly how to implement Server-Driven UI using `json-to-compose` according to Kotlin Multiplatform best practices!
