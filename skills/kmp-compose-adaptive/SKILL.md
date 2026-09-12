---
name: kmp-compose-adaptive
description: Responsive and adaptive UI patterns for Compose Multiplatform across Android, iOS, Desktop, and Web.
version: 1.0.0
author: jesusdmedinac
tags:
  - compose
  - ui
  - adaptive
  - layout
  - navigation
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - adaptive compose
  - responsive layout
  - window size classes
  - multi-window
compatibility:
  kotlin: ">=2.0.0"
  compose: ">=1.6.0"
---

# Adaptive UI in Compose Multiplatform

When building user interfaces that target mobile (phones, foldables, tablets), desktop (macOS, Windows, Linux), and web (Wasm), Compose Multiplatform apps must adapt fluidly to screen dimensions, input modalities, and window state changes.

## 1. Window Size Classes

Instead of branching on device models or raw pixel dimensions, categorize layouts using standard Window Size Classes:
- **Compact**: Width < 600dp (standard portrait phones)
- **Medium**: Width 600dp - 839dp (tablets, foldables, portrait small laptops)
- **Expanded**: Width >= 840dp (desktop monitors, tablet landscape)

### Adaptive Layout Strategy:
- **Compact**: Single-column vertical list with Bottom Navigation Bar.
- **Medium**: List-detail with Navigation Rail on the start side.
- **Expanded**: Two-pane or three-pane scaffold (Navigation Rail + List Pane + Detail/Inspector Pane).

```kotlin
@Composable
fun AdaptiveAppScaffold(
    windowWidthSizeClass: WindowWidthSizeClass,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        if (windowWidthSizeClass != WindowWidthSizeClass.Compact) {
            AppNavigationRail()
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
```

## 2. Dynamic Pane Navigation (Navigation 3 / Supporting Pane)

When transitioning from Compact to Expanded:
- In **Compact**, navigating to a item pushes a detail screen onto the backstack.
- In **Expanded**, the detail pane is rendered side-by-side without popping the list pane.

## 3. Input Modalities (Mouse, Keyboard, Touch)
- **Hover effects**: Use `Modifier.hoverable()` and pointer cursors on Desktop/Wasm.
- **Keyboard shortcuts**: Attach `Modifier.onPreviewKeyEvent` for desktop accelerators (`Cmd+K`, `Escape`, arrow navigation).
- **Minimum Touch Targets**: Ensure interactive touch elements maintain at least 48x48dp on touch screens, relaxing to compact density on desktop mouse pointers.
