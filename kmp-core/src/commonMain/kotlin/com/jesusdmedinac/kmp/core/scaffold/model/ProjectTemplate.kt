package com.jesusdmedinac.kmp.core.scaffold.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProjectTemplate(
    val id: String,
    val displayName: String,
    val description: String,
    val aliases: List<String> = emptyList(),
) {
    SHARED_UI(
        id = "shared-ui",
        displayName = "Shared UI Multiplatform App",
        description = "Official JetBrains template with Compose Multiplatform on all platforms",
        aliases = listOf("compose-app", "compose-multiplatform"),
    ),
    NATIVE_UI(
        id = "native-ui",
        displayName = "Native UI Multiplatform App",
        description = "Official JetBrains template with Compose Desktop/Web and native SwiftUI on iOS",
        aliases = listOf("compose-native"),
    ),
    MULTIPLATFORM_LIBRARY(
        id = "multiplatform-library",
        displayName = "Multiplatform Library",
        description = "Official JetBrains template ready for Maven Central publishing via vanniktech",
        aliases = listOf("kmp-library"),
    ),
    TOOLCHAIN_SHARED_UI(
        id = "toolchain-shared-ui",
        displayName = "Shared UI Multiplatform App configured with Kotlin Toolchain",
        description = "Official JetBrains declarative Kotlin Toolchain app with Compose UI",
        aliases = listOf("toolchain-app"),
    ),
    TOOLCHAIN_NATIVE_UI(
        id = "toolchain-native-ui",
        displayName = "Native UI Multiplatform App configured with Kotlin Toolchain",
        description = "Official JetBrains declarative Kotlin Toolchain app with native iOS UI",
        aliases = listOf("toolchain-native"),
    ),
    FULLSTACK(
        id = "fullstack",
        displayName = "Fullstack KMP",
        description = "Fullstack Kotlin with Ktor backend server and Compose Multiplatform client sharing models",
    ),
    JSON_TO_COMPOSE_SAMPLE(
        id = "json-to-compose-sample-app",
        displayName = "JSON to Compose Sample App",
        description = "Sample multiplatform app demonstrating Server-Driven UI with json-to-compose",
        aliases = listOf("json-to-compose", "sdui-starter"),
    );

    companion object {
        val COMPOSE_MULTIPLATFORM = SHARED_UI
        val TOOLCHAIN_APP = TOOLCHAIN_SHARED_UI
        val KMP_LIBRARY = MULTIPLATFORM_LIBRARY

        fun fromId(id: String): ProjectTemplate? = entries.find { entry ->
            entry.id.equals(id.trim(), ignoreCase = true) ||
                entry.aliases.any { it.equals(id.trim(), ignoreCase = true) }
        }
    }
}
