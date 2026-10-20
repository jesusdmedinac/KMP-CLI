---
name: kotlin-data-multiplatform-settings
description: Reactive key-value persistence across Android, iOS, Desktop, and Web using Multiplatform-Settings and Coroutines Flow.
version: 1.0.0
author: jesusdmedinac
tags:
  - settings
  - preferences
  - persistence
  - key-value
  - flow
  - grant-winner
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - multiplatform settings
  - kmp preferences
  - shared preferences kmp
  - nsuserdefaults kmp
  - local storage kmp
  - key value storage kmp
compatibility:
  kotlin: ">=2.0.0"
  multiplatform-settings: ">=1.2.0"
---

# Multiplatform Key-Value Storage with Multiplatform-Settings

Multiplatform-Settings is the canonical key-value persistence library for Kotlin Multiplatform, sponsored by the **Kotlin Foundation Grants Program (2024)**. It provides a unified abstraction over native preferences: Android `SharedPreferences`, Apple `NSUserDefaults`, Desktop `java.util.prefs.Preferences`, and Web `localStorage`.

---

## 1. Multiplatform Dependencies

### `module.yaml` (Kotlin Toolchain)
```yaml
dependencies:
  - com.russhwolf:multiplatform-settings:1.3.0
  - com.russhwolf:multiplatform-settings-coroutines:1.3.0
```

### `libs.versions.toml` (Gradle)
```toml
[versions]
multiplatform-settings = "1.3.0"

[libraries]
settings-core = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatform-settings" }
settings-coroutines = { module = "com.russhwolf:multiplatform-settings-coroutines", version.ref = "multiplatform-settings" }
```

In `shared/build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation(libs.settings.core)
    implementation(libs.settings.coroutines)
}
```

---

## 2. Shared Settings Abstraction in `commonMain`

Create a reactive preferences repository in `commonMain` using `ObservableSettings` and Coroutines Flow:

```kotlin
package com.example.app.data.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isDarkModeFlow: Flow<Boolean>
    val selectedLanguageFlow: Flow<String?>

    fun setDarkMode(enabled: Boolean)
    fun setSelectedLanguage(languageCode: String?)
    fun clear()
}

class SettingsUserPreferencesRepository(
    private val settings: ObservableSettings,
) : UserPreferencesRepository {

    override val isDarkModeFlow: Flow<Boolean> =
        settings.getBooleanFlow(KEY_DARK_MODE, defaultValue = true)

    override val selectedLanguageFlow: Flow<String?> =
        settings.getStringOrNullFlow(KEY_LANGUAGE)

    override fun setDarkMode(enabled: Boolean) {
        settings[KEY_DARK_MODE] = enabled
    }

    override fun setSelectedLanguage(languageCode: String?) {
        if (languageCode != null) {
            settings[KEY_LANGUAGE] = languageCode
        } else {
            settings.remove(KEY_LANGUAGE)
        }
    }

    override fun clear() {
        settings.clear()
    }

    companion object {
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_LANGUAGE = "pref_selected_language"
    }
}
```

---

## 3. Platform Factory Instantiation

Instantiate `ObservableSettings` in target platform source sets or dependency injection modules:

### Android (`androidMain`):
```kotlin
import android.content.Context
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

fun createSettings(context: Context): ObservableSettings {
    val delegate = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(delegate)
}
```

### Apple iOS / macOS (`iosMain`):
```kotlin
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import platform.Foundation.NSUserDefaults

fun createSettings(): ObservableSettings {
    val delegate = NSUserDefaults.standardUserDefaults
    return NSUserDefaultsSettings(delegate)
}
```

### Desktop JVM (`desktopMain`):
```kotlin
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

fun createSettings(): ObservableSettings {
    val delegate = Preferences.userRoot().node("com.example.app")
    return PreferencesSettings(delegate)
}
```

### Web Wasm / JS (`wasmJsMain` / `jsMain`):
```kotlin
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.StorageSettings

fun createSettings(): ObservableSettings {
    return StorageSettings()
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Prefer `ObservableSettings`**: Always wrap settings with observable support to allow reactive UI collection via `StateFlow` or Compose `collectAsState()`.
2. **Use Typed Accessors**: Leverage operator overloading (`settings[key] = value`) for clean readability.
3. **Keep Keys Centralized**: Maintain private constants inside the repository implementation to avoid magic strings.

### ❌ Anti-Patterns to Avoid:
1. **Do not use for relational or indexed data**: Use `kotlin-data-room-kmp` or `kotlin-data-sqldelight-database` for lists, queryable tables, or datasets > 1 MB.
2. **Never store plaintext secrets or passwords**: Plain preferences are stored in plaintext XML, plist, or LocalStorage. Delegate secrets to platform Keychains or EncryptedSharedPreferences.
3. **Never expose raw `Settings` to UI Composables**: Wrap preferences inside a repository interface emitting typed `Flow<T>`.
