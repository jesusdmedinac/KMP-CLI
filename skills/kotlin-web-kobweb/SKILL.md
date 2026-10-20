---
name: kotlin-web-kobweb
description: Full-stack web applications and static sites using Compose HTML, Silk UI design system, and file-based routing with Kobweb.
version: 1.0.0
author: jesusdmedinac
tags:
  - kobweb
  - web
  - compose-html
  - fullstack
  - silk
  - wasm
  - grant-winner
targets:
  - wasm
  - js
triggers:
  - kobweb
  - compose html
  - kobweb silk
  - web compose kmp
  - fullstack web kotlin
  - static site generator kotlin
compatibility:
  kotlin: ">=2.0.0"
  kobweb: ">=0.19.0"
---

# Modern Web Development with Kobweb & Silk UI

Kobweb (Varabyte) is the canonical full-stack web and static-site framework for Kotlin Multiplatform, sponsored by the **Kotlin Foundation Grants Program (2025)**. Built on top of Compose HTML and Wasm/JS, Kobweb brings file-based routing, responsive styling via Silk UI, and static site export to Kotlin developers.

---

## 1. Multiplatform Dependencies & Plugins

Kobweb utilizes dedicated Gradle plugins to manage code generation and DOM compilation:

### `libs.versions.toml`
```toml
[versions]
kobweb = "0.20.0"

[plugins]
kobweb-application = { id = "com.varabyte.kobweb.application", version.ref = "kobweb" }

[libraries]
kobweb-core = { module = "com.varabyte.kobweb:kobweb-core", version.ref = "kobweb" }
kobweb-silk = { module = "com.varabyte.kobweb:kobweb-silk", version.ref = "kobweb" }
kobweb-silk-icons = { module = "com.varabyte.kobweb:silk-icons-fa", version.ref = "kobweb" }
```

In `site/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kobweb.application)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }
}
```

---

## 2. File-Based Routing & Silk UI Layouts

Define web pages under the `pages/` directory using the `@Page` annotation:

```kotlin
package com.example.site.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.padding
import org.jetbrains.compose.web.css.dp
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P

@Page
@Composable
fun HomePage() {
    SimpleGrid(
        numColumns = numColumns(base = 1, md = 2),
        modifier = Modifier.fillMaxSize().padding(24.px),
    ) {
        H1 { SpanText("Welcome to Kobweb") }
        P { SpanText("Full-stack Compose web development with Kotlin Multiplatform.") }

        Button(onClick = { /* navigate or trigger action */ }) {
            SpanText("Get Started")
        }
    }
}
```

---

## 3. Server-Side API Endpoints (`@Api`)

Kobweb supports unified server routes in the same codebase using the `@Api` annotation:

```kotlin
package com.example.site.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText

@Api
fun healthCheck(ctx: ApiContext) {
    ctx.res.setBodyText("""{"status": "UP", "service": "kobweb-server"}""")
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Responsive Breakpoints with Silk**: Use `numColumns(base = 1, sm = 2, md = 3)` to ensure responsive layouts across mobile and desktop browsers.
2. **Leverage CSS Variables**: Use Silk's design tokens and theme palettes (`SilkTheme.palette`) rather than hardcoded CSS values.
3. **Static Generation**: Use `kobweb export` for blazingly fast CDN hosting when dynamic SSR is not required.

### ❌ Anti-Patterns to Avoid:
1. **Never import Compose Multiplatform canvas (Skiko)**: Kobweb is tailored for DOM and HTML elements (`org.jetbrains.compose.web.dom.*`); mixing Canvas-based Compose can cause bundle bloat and layout bugs.
2. **Never hardcode pixel widths**: Avoid `Modifier.width(1200.px)`; use flex layouts and fluid containers.
3. **Never block on server endpoints**: Keep `@Api` handlers asynchronous and non-blocking.
