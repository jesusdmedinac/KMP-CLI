# `kmp-auto`

**Cross-Platform In-Car Framework bridging Android Auto & Apple CarPlay in Kotlin Multiplatform.**

[![GitHub](https://img.shields.io/badge/GitHub-jesusdmedinac%2Fkmp--auto-7F52FF?logo=github)](https://github.com/jesusdmedinac/kmp-auto)

---

## Overview

Automotive mobile development is notoriously fragmented: Android uses `androidx.car.app` templates, while iOS uses Apple's `CarPlay` framework Scene Delegates.

`kmp-auto` unifies both under a single declarative Kotlin API:
- **`AutoScreen` & `AutoTemplate`**: Declarative List, Grid, Message, and Information templates that automatically render into native Android Auto and CarPlay surfaces.
- **Backstack Safety Engine**: Enforces car safety standards (preventing distracted driving violations and crashes from exceeding the 5-step car navigation stack).
- **Reactive State Flow**: Automatic UI invalidation driven by Kotlin Coroutines `StateFlow`.
