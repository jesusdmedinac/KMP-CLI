package com.jesusdmedinac.kmp.core.scaffold.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProjectTemplate(val id: String, val displayName: String, val description: String) {
    COMPOSE_MULTIPLATFORM(
        id = "compose-multiplatform",
        displayName = "Compose Multiplatform",
        description = "Adaptive Compose UI targeting Android, iOS, Desktop, and Wasm",
    ),
    TOOLCHAIN_APP(
        id = "toolchain-app",
        displayName = "Kotlin Toolchain App",
        description = "Declarative Kotlin Toolchain project with module.yaml",
    ),
    KMP_LIBRARY(
        id = "kmp-library",
        displayName = "KMP Library",
        description = "Cross-platform library skeleton ready for Maven publishing",
    ),
    FULLSTACK(
        id = "fullstack",
        displayName = "Fullstack KMP",
        description = "Ktor backend server and Compose Multiplatform client sharing models",
    ),
    SDUI_STARTER(
        id = "sdui-starter",
        displayName = "Server-Driven UI Starter",
        description = "Dynamic Server-Driven UI app powered by json-to-compose & caching",
    );

    companion object {
        fun fromId(id: String): ProjectTemplate? = entries.find {
            it.id.equals(id.trim(), ignoreCase = true)
        }
    }
}
