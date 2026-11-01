# `kmp create`

Scaffold clean, modern Kotlin Multiplatform projects adhering strictly to the **Zero-Bloat Principle** — only generating the platforms and runners you explicitly request.

---

## Usage

```bash
kmp create [project-name] [options]
```

## Options

- `--template <id>`: Project template to use. Defaults to `shared-ui`.
- `--targets <list>`: Comma-separated target platforms (e.g. `android,ios` or `android,ios,desktop,wasm`).
- `--package <pkg>`: Base application package ID (e.g. `com.example.myapp`).
- `--output <dir>`, `-o`: Target output directory.
- `--wizard`: Launch the interactive console wizard.
- `--json`: Output scaffolding result as structured JSON.

---

## Available Project Templates

| Template ID | Name | Description | Active Targets |
| :--- | :--- | :--- | :--- |
| `shared-ui` | Shared UI App | Compose Multiplatform across all platforms | Android, iOS, Desktop, Wasm |
| `native-ui` | Native UI App | Compose on Android/Desktop with native SwiftUI on iOS | Android, iOS, Desktop, Web |
| `multiplatform-library` | Multiplatform Library | Library ready for Maven Central publication | All KMP targets |
| `toolchain-shared-ui` | Kotlin Toolchain App | Modern declarative `module.yaml` with Compose UI | Android, iOS, Desktop |
| `toolchain-native-ui` | Kotlin Toolchain Native | Modern declarative `module.yaml` with SwiftUI | Android, iOS |
| `fullstack` | Fullstack KMP | Shared data models with Ktor backend and Compose client | Backend, Android, iOS, Web |
| `json-to-compose-sample-app` | JSON to Compose Starter | Server-Driven UI starter powered by `json-to-compose` | Android, iOS, Desktop, Wasm |
| `kmp-shared-core-bridge` | Shared Core Bridge | Lightweight KMP module configured for ingestion by existing native/hybrid shells | Android (AAR), iOS (SPM/CocoaPods) |

---

## Example Usage

### Mobile Only Compose App
```bash
kmp create MyMobileApp --targets android,ios --template shared-ui --package com.mycompany.app
```

### Server-Driven UI Starter
```bash
kmp create SduiDemo --template json-to-compose-sample-app --targets android,ios
```
