---
name: kmp-sdui-compose
description: Server-Driven UI architecture with json-to-compose, remote document fetching, dual-tier caching, and Stale-While-Revalidate in Kotlin Multiplatform.
version: 1.0.0
author: jesusdmedinac
tags:
  - sdui
  - compose
  - json-to-compose
  - ktor
  - caching
  - remote-engine
targets:
  - android
  - ios
  - desktop
  - wasm
triggers:
  - server-driven ui
  - sdui
  - json to compose
  - remote compose
  - dynamic ui
compatibility:
  kotlin: ">=2.0.0"
  compose: ">=1.7.0"
---

# Server-Driven UI with `json-to-compose` in Kotlin Multiplatform

Server-Driven UI (SDUI) shifts UI layout and dynamic interaction definitions from compiled client binaries to remote backend servers or CDNs. Using `json-to-compose`, applications parse declarative JSON payloads into fully native Jetpack Compose component trees with reactive state and interactive action dispatching.

---

## 1. Core Architecture: `ComposeDocument`

A modern SDUI screen is defined by a `ComposeDocument` encapsulating state, actions, and the visual tree:

```json
{
  "initialState": {
    "user_name": "Developer",
    "notifications_enabled": true,
    "cart_count": 3
  },
  "actions": {
    "toggle_notifications": [
      { "action": "toggleState", "stateKey": "notifications_enabled" }
    ],
    "checkout": [
      { "action": "log", "message": "Initiating checkout" },
      { "action": "custom", "type": "navigate", "params": { "route": "checkout_flow" } }
    ]
  },
  "root": {
    "type": "Column",
    "modifiers": [
      { "type": "padding", "all": 16 },
      { "type": "fillMaxWidth" }
    ],
    "children": [
      {
        "type": "Text",
        "properties": {
          "text": "Welcome back!",
          "fontSize": 20,
          "fontWeight": "Bold"
        }
      },
      {
        "type": "Button",
        "properties": {
          "onClickEventName": "checkout"
        },
        "children": [
          { "type": "Text", "properties": { "text": "Proceed to Checkout" } }
        ]
      }
    ]
  }
}
```

### Rendering Locally
```kotlin
val document = json.decodeFromString<ComposeDocument>(jsonString)
document.ToCompose()
```
The runtime automatically instantiates `MutableStateHost` instances for every `initialState` key and wires `Behavior` callbacks for all named `actions`.

---

## 2. Remote Engine & Caching (Phase 9 Runtime)

In production apps, JSON layouts are hosted on remote endpoints. The Remote Engine provides zero-latency cold starts, offline resilience, and automatic background revalidation:

```kotlin
@Composable
fun HomeScreen() {
    RemoteComposeDocument(
        url = "https://cdn.example.com/screens/home.json",
        loadingContent = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        },
        errorContent = { error, retry ->
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Failed to load screen: ${error.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = retry) { Text("Retry") }
            }
        }
    )
}
```

### Key Remote Capabilities:
- **Dual-Tier Caching (L1 + L2)**: Fast in-memory LRU cache (L1) renders instantly (<16ms) on navigation; local disk/persistent storage (L2) survives app restarts.
- **Stale-While-Revalidate (SWR)**: Instantly displays cached UI (`isStale = true`), simultaneously verifies with the server via HTTP `If-None-Match` (ETag), and transitions smoothly if updated (`200 OK`) without layout flicker.
- **HTTP 304 Not Modified**: Saves client battery and bandwidth by preserving cached layouts when no backend changes occurred.
- **Offline Fallback**: Displays the last known good layout with an offline indicator if connectivity is unavailable.

---

## 3. Extensibility via Custom Action Handlers

Native capabilities (hardware camera, biometric auth, native navigation, analytics) can be hooked into declarative actions:

```kotlin
val customHandlers = mapOf<String, (ComposeAction.Custom) -> Unit>(
    "navigate" to { action ->
        val route = action.params["route"]?.jsonPrimitive?.content ?: return@mapOf
        navController.navigate(route)
    },
    "analytics" to { action ->
        val event = action.params["event"]?.jsonPrimitive?.content ?: return@mapOf
        AnalyticsTracker.logEvent(event)
    }
)

CompositionLocalProvider(
    LocalCustomActionHandlers provides customHandlers
) {
    document.ToCompose()
}
```

---

## 4. Best Practices for SDUI Versioning

1. **Backwards Compatibility**: When evolving backend schemas, always provide sensible default values for new properties so older app builds do not crash.
2. **Component Graceful Degradation**: Unrecognized node types or modifiers should fall back safely to a placeholder or empty container without halting execution.
3. **Cache Invalidation Tokens**: Use unique version hashes or ETags on backend deployments to ensure instant cache invalidation upon publishing layout updates.

---

## 5. References & Canonical Sources

- **Library Repository**: [jesusdmedinac/json-to-compose](https://github.com/jesusdmedinac/json-to-compose)
- **Author & Maintainer**: Jesús Daniel Medina Cruz
- **License**: MIT License
- **DeepWiki Reference**: [deepwiki.com/jesusdmedinac/json-to-compose](https://deepwiki.com/jesusdmedinac/json-to-compose)
