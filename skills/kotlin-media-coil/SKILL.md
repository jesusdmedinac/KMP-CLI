---
name: kotlin-media-coil
description: Multiplatform async image loading, memory and disk caching, and Ktor 3 network fetching with Coil 3 for Compose Multiplatform.
version: 1.0.0
author: jesusdmedinac
tags:
  - coil
  - images
  - media
  - cache
  - async-image
  - ktor
  - grant-winner
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - coil 3
  - coil kmp
  - async image compose
  - multiplatform image loading
  - coil network fetcher
  - image caching kmp
compatibility:
  kotlin: ">=2.0.0"
  compose-multiplatform: ">=1.7.0"
  coil: ">=3.0.0"
---

# Multiplatform Image Loading with Coil 3

Coil 3 is the canonical image-loading library for Compose Multiplatform, sponsored by the **Kotlin Foundation Grants Program (2023)**. It provides native multiplatform rendering via Skia/Compose, memory and disk caching with Okio, and network engine flexibility with Ktor 3.

---

## 1. Multiplatform Dependencies

### `module.yaml` (Kotlin Toolchain)
```yaml
dependencies:
  - io.coil-kt.coil3:coil-compose:3.0.4
  - io.coil-kt.coil3:coil-network-ktor3:3.0.4
```

### `libs.versions.toml` (Gradle)
```toml
[versions]
coil = "3.0.4"
ktor = "3.1.1"

[libraries]
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor3 = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }
```

In `shared/build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
}
```

---

## 2. Global ImageLoader Setup in `commonMain`

Configure a shared `ImageLoader` in `commonMain` with cross-platform disk caching and network engine support:

```kotlin
package com.example.app.media

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import okio.FileSystem

fun newImageLoader(
    context: PlatformContext,
    debug: Boolean = false,
): ImageLoader = ImageLoader.Builder(context)
    .components {
        add(KtorNetworkFetcherFactory())
    }
    .memoryCache {
        MemoryCache.Builder()
            .maxSizePercent(context, 0.25)
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
            .maxSizeBytes(64L * 1024 * 1024) // 64 MB
            .build()
    }
    .crossfade(true)
    .build()
```

Provide the `ImageLoader` at the root of your Compose Multiplatform tree:

```kotlin
import androidx.compose.runtime.Composable
import coil3.compose.setSingletonImageLoaderFactory

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        newImageLoader(context)
    }

    MainContent()
}
```

---

## 3. Idiomatic UI Usage with `AsyncImage`

Use `AsyncImage` directly in shared Composables across Android, iOS, Desktop, and Wasm:

```kotlin
package com.example.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning

@Composable
fun RemoteVehicleImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
        loading = {
            CircularProgressIndicator()
        },
        error = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Failed to load image",
            )
        },
    )
}
```

---

## 4. Architectural Guardrails & Anti-Patterns

### ✅ Recommended Practices:
1. **Use `coil3.PlatformContext`**: Do not import Android `android.content.Context` into `commonMain`. Coil 3 abstracts context via `PlatformContext`.
2. **Reuse the Singleton ImageLoader**: Configure disk and memory cache policies once at application ignition via `setSingletonImageLoaderFactory`.
3. **Use Ktor 3 Fetcher**: Bind `KtorNetworkFetcherFactory` to reuse the application's authenticated Ktor HTTP client if custom headers or cert pinning are required.

### ❌ Anti-Patterns to Avoid:
1. **Never import `android.net.Uri`**: Pass plain `String` URLs or Coil `coil3.Uri` in shared code.
2. **Never decode bitmaps synchronously**: Always let Coil offload disk and network decoding to `Dispatchers.IO`.
3. **Never hardcode Android cache paths**: Use Okio `FileSystem.SYSTEM_TEMPORARY_DIRECTORY` or `PlatformContext` storage resolvers for cross-platform disk paths.
