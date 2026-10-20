package com.jesusdmedinac.kmp.core.klibs

import com.jesusdmedinac.kmp.core.klibs.model.KlibsLibraryInfo
import com.jesusdmedinac.kmp.core.klibs.model.LibraryRecommendationResult
import com.jesusdmedinac.kmp.core.klibs.model.VerifiedLibrary

/**
 * Curated catalog of Tier 2 Kotlin Multiplatform libraries aligned with klibs.io and Kotlin Foundation.
 * Provides target-aware compatibility verification and flags pre-stable or incompatible library targets.
 */
class KlibsCatalog(
    private val libraries: List<KlibsLibraryInfo> = defaultLibraries,
) {
    fun query(
        query: String = "",
        projectTargets: List<String> = emptyList(),
    ): LibraryRecommendationResult {
        val normalizedQuery = query.trim().lowercase()
        val filtered = if (normalizedQuery.isEmpty()) {
            libraries
        } else {
            libraries.filter { lib ->
                lib.name.lowercase().contains(normalizedQuery) ||
                        lib.coordinate.lowercase().contains(normalizedQuery) ||
                        lib.category.lowercase().contains(normalizedQuery) ||
                        lib.description.lowercase().contains(normalizedQuery)
            }
        }

        val verifiedList = filtered.map { lib ->
            val missing = projectTargets.filter { target -> !isTargetSupported(target, lib.supportedTargets) }
            VerifiedLibrary(
                coordinate = lib.coordinate,
                name = lib.name,
                version = lib.version,
                status = lib.status,
                supportedTargets = lib.supportedTargets,
                compatibleWithProject = missing.isEmpty(),
                missingTargets = missing,
                category = lib.category,
                description = lib.description,
                docUrl = lib.docUrl,
            )
        }

        return LibraryRecommendationResult(
            query = query,
            projectTargets = projectTargets,
            libraries = verifiedList,
        )
    }

    companion object {
        fun isTargetSupported(projectTarget: String, supportedTargets: List<String>): Boolean {
            val normTarget = projectTarget.lowercase()
            val normSupported = supportedTargets.map { it.lowercase() }

            return when {
                normTarget.contains("android") -> normSupported.any { it.contains("android") }
                normTarget.startsWith("ios") -> normSupported.any { it.startsWith("ios") || it == "apple" }
                normTarget == "jvm" || normTarget == "desktop" -> normSupported.any { it == "jvm" || it == "desktop" }
                normTarget.startsWith("wasm") -> normSupported.any { it.startsWith("wasm") }
                normTarget == "js" -> normSupported.any { it == "js" || it.startsWith("wasm") }
                normTarget.startsWith("macos") -> normSupported.any { it.startsWith("macos") || it == "apple" }
                normTarget.startsWith("linux") -> normSupported.any { it.startsWith("linux") }
                normTarget.startsWith("mingw") -> normSupported.any { it.startsWith("mingw") || it == "windows" }
                else -> normSupported.any { it == normTarget }
            }
        }

        val defaultLibraries: List<KlibsLibraryInfo> = listOf(
            KlibsLibraryInfo(
                coordinate = "androidx.room:room-runtime",
                name = "Room KMP",
                version = "2.7.0-alpha13",
                description = "AndroidX Room multiplatform SQLite database and data persistence",
                status = "Pre-stable",
                supportedTargets = listOf("android", "ios", "jvm", "macos", "linux"),
                category = "Persistence",
                docUrl = "https://developer.android.com/kotlin/multiplatform/room",
            ),
            KlibsLibraryInfo(
                coordinate = "app.cash.sqldelight:runtime",
                name = "SQLDelight",
                version = "2.0.2",
                description = "Cross-platform typesafe SQL database for Kotlin Multiplatform",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos", "linux", "mingw"),
                category = "Persistence",
                docUrl = "https://cashapp.github.io/sqldelight",
            ),
            KlibsLibraryInfo(
                coordinate = "io.ktor:ktor-client-core",
                name = "Ktor Client",
                version = "3.1.1",
                description = "Multiplatform asynchronous HTTP client with serialization and coroutines",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos", "linux", "mingw"),
                category = "Networking",
                docUrl = "https://ktor.io",
            ),
            KlibsLibraryInfo(
                coordinate = "io.insert-koin:koin-core",
                name = "Koin",
                version = "4.0.2",
                description = "Pragmatic lightweight dependency injection framework for Kotlin Multiplatform",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos", "linux"),
                category = "Dependency Injection",
                docUrl = "https://insert-koin.io",
            ),
            KlibsLibraryInfo(
                coordinate = "com.arkivanov.decompose:decompose",
                name = "Decompose",
                version = "3.3.0",
                description = "Kotlin Multiplatform component-based routing and navigation lifecycle tree",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos"),
                category = "Navigation & Architecture",
                docUrl = "https://arkivanov.github.io/Decompose",
            ),
            KlibsLibraryInfo(
                coordinate = "com.russhwolf:multiplatform-settings",
                name = "Multiplatform Settings",
                version = "1.3.0",
                description = "Key-value persistent storage library across Android, iOS, Desktop and Web",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos", "linux", "mingw"),
                category = "Persistence",
                docUrl = "https://github.com/russhwolf/multiplatform-settings",
            ),
            KlibsLibraryInfo(
                coordinate = "io.coil-kt.coil3:coil-compose",
                name = "Coil 3",
                version = "3.1.0",
                description = "Asynchronous image loading for Compose Multiplatform",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js"),
                category = "Media & Images",
                docUrl = "https://coil-kt.github.io/coil",
            ),
            KlibsLibraryInfo(
                coordinate = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
                name = "Kotlinx Coroutines",
                version = "1.10.1",
                description = "Official Kotlin structured concurrency, Flows and background dispatchers",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos", "linux", "mingw"),
                category = "Concurrency",
                docUrl = "https://github.com/Kotlin/kotlinx.coroutines",
            ),
            KlibsLibraryInfo(
                coordinate = "org.jetbrains.kotlinx:kotlinx-serialization-json",
                name = "Kotlinx Serialization",
                version = "1.8.0",
                description = "Official Kotlin cross-platform JSON serialization compiler plugin and runtime",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos", "linux", "mingw"),
                category = "Serialization",
                docUrl = "https://github.com/Kotlin/kotlinx.serialization",
            ),
            KlibsLibraryInfo(
                coordinate = "app.cash.turbine:turbine",
                name = "Turbine",
                version = "1.2.0",
                description = "Small testing library for kotlinx.coroutines Flow",
                status = "Stable",
                supportedTargets = listOf("android", "ios", "jvm", "wasm", "js", "macos", "linux", "mingw"),
                category = "Testing",
                docUrl = "https://github.com/cashapp/turbine",
            ),
        )
    }
}
