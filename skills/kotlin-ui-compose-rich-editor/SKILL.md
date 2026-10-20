---
name: kotlin-ui-compose-rich-editor
description: WYSIWYG rich text editing, formatting toolbars, and Markdown/HTML conversion with Compose Rich Editor.
version: 1.0.0
author: jesusdmedinac
tags:
  - rich-editor
  - markdown
  - html
  - text
  - compose
  - ui
  - grant-winner
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - compose rich editor
  - wysiwyg compose
  - markdown editor kmp
  - html text editor compose
  - rich text state
  - text formatting toolbar
compatibility:
  kotlin: ">=2.0.0"
  compose-multiplatform: ">=1.7.0"
  richeditor: ">=1.0.0"
---

# Multiplatform Rich Text Editing with Compose Rich Editor

Compose Rich Editor is the canonical WYSIWYG text editing and markdown/HTML conversion library for Compose Multiplatform, sponsored by the **Kotlin Foundation Grants Program (2024)**. It provides a pure Compose API for formatting text across Android, iOS, Desktop, and Web without platform text view bridges.

---

## 1. Multiplatform Dependencies

### `module.yaml` (Kotlin Toolchain)
```yaml
dependencies:
  - com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc10
```

### `libs.versions.toml` (Gradle)
```toml
[versions]
richeditor = "1.0.0-rc10"

[libraries]
richeditor-compose = { module = "com.mohamedrejeb.richeditor:richeditor-compose", version.ref = "richeditor" }
```

In `shared/build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation(libs.richeditor.compose)
}
```

---

## 2. Rich Text State & Editor Composable

Manage formatting state using `rememberRichTextState()`:

```kotlin
package com.example.app.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

@Composable
fun DocumentEditor(
    modifier: Modifier = Modifier,
    initialMarkdown: String = "",
    onContentChanged: (String) -> Unit = {},
) {
    val state = rememberRichTextState()

    // Initialize content from Markdown
    androidx.compose.runtime.LaunchedEffect(initialMarkdown) {
        if (initialMarkdown.isNotEmpty() && state.toMarkdown().isEmpty()) {
            state.setMarkdown(initialMarkdown)
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Formatting Toolbar
        FormattingToolbar(state = state)

        // Editor
        RichTextEditor(
            state = state,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
fun FormattingToolbar(
    state: RichTextState,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        IconButton(onClick = { state.toggleSpanStyle(com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi::class) {
            state.toggleBold()
        } }) {
            Icon(Icons.Default.FormatBold, contentDescription = "Bold")
        }

        IconButton(onClick = { state.toggleItalic() }) {
            Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
        }

        IconButton(onClick = { state.toggleUnderline() }) {
            Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline")
        }
    }
}
```

---

## 3. Bi-Directional Export & Serialization

Convert styled text to standard Markdown or HTML for storage or API transfer:

```kotlin
package com.example.app.ui.editor

import com.mohamedrejeb.richeditor.model.RichTextState

fun exportMarkdown(state: RichTextState): String {
    return state.toMarkdown()
}

fun exportHtml(state: RichTextState): String {
    return state.toHtml()
}

fun importMarkdown(state: RichTextState, markdown: String) {
    state.setMarkdown(markdown)
}

fun importHtml(state: RichTextState, html: String) {
    state.setHtml(html)
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Rely on Compose Snapshot State**: Allow `rememberRichTextState()` to handle internal undo/redo and selections natively within Compose.
2. **Export at Boundaries**: Convert to raw Markdown or HTML strings when saving to databases or sending payloads via Ktor.
3. **Use Material 3 Integration**: Leverage `com.mohamedrejeb.richeditor.ui.material3.RichTextEditor` for proper theming and cursor colors.

### ❌ Anti-Patterns to Avoid:
1. **Never use Android `SpannableStringBuilder`**: Keep all formatting multiplatform using `RichTextState`.
2. **Never parse Markdown with regex**: Use `state.setMarkdown()` or `state.toMarkdown()` which handle edge cases and nested tokens.
3. **Never block the main thread with massive HTML exports**: Offload parsing of 100+ page documents to `Dispatchers.Default`.
