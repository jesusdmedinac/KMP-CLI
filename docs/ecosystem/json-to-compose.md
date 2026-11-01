# `json-to-compose`

**Server-Driven UI & Generative UI Runtime for Compose Multiplatform.**

[![GitHub](https://img.shields.io/badge/GitHub-jesusdmedinac%2Fjson--to--compose-7F52FF?logo=github)](https://github.com/jesusdmedinac/json-to-compose)

---

## Overview

`json-to-compose` transforms declarative JSON payloads into fully interactive, native Jetpack Compose UI trees at runtime. It eliminates app store release cycles for dynamic screens, promotional flows, remote forms, and AI-generated chat widgets.

---

## Integration with `kmp-cli`

### 1. Scaffold a Ready-to-Run SDUI App
```bash
kmp create MyDynamicApp --template json-to-compose-sample-app
```

### 2. Install the Certified SDUI Skill
```bash
kmp skills install kotlin-ui-sdui-compose
```

### 3. Automatic Detection in `kmp migrate`
When running `kmp migrate audit` on a React Native project, `kmp-cli` automatically flags dynamic JSON layouts and maps them to `json-to-compose` runtime parsers.
