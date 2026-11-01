# `zora-community`

**Production-Grade Open Source Automotive Copilot Reference Application.**

[![GitHub](https://img.shields.io/badge/GitHub-jesusdmedinac%2Fzora--community-7F52FF?logo=github)](https://github.com/jesusdmedinac/zora-community)

---

## Overview

`zora-community` is a fully featured, production-ready vehicle maintenance tracker, parts catalog, and AI copilot built entirely with Kotlin Multiplatform. It serves two complementary roles:
1. **Standalone Production Reference:** Shows how to combine Compose Multiplatform (Material 3 Dark Titanium), SQLDelight 2.x, Ktor 3, Koin, and `json-to-compose` in a real-world app across Android, iOS, and Desktop.
2. **Official Open-Core Template for `kmp-cli`:** Developers can scaffold directly from this architecture using:
   ```bash
   kmp kit create zora-community
   ```

---

## Continuous Integration (GitHub Actions)

`zora-community` includes an automated GitHub Actions workflow (`.github/workflows/ci.yml`) running on macOS runners:
- **Domain & Unit Testing:** Executes `./gradlew --no-daemon jvmTest` verifying core logic, Koin dependency injection, and SQLDelight 2.x persistence.
- **Headless Skia Offscreen Rendering:** Runs offscreen UI screenshot tests against the Dark Titanium design system tokens (`PreviewScreenshotGeneratorTest`).
- **Toolchain Environment Sanity:** Validates Kotlin Toolchain declarative `module.yaml` configuration.
