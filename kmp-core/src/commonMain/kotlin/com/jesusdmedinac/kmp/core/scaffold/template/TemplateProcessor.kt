package com.jesusdmedinac.kmp.core.scaffold.template

data class TemplateContext(
    val variables: Map<String, String>,
    val activeTargets: Set<String> = emptySet(),
)

object TemplateProcessor {
    private val startTargetRegex = Regex("""(?://\s*|#\s*)?\{\{#TARGET:([a-zA-Z0-9_-]+)\}""")
    private val endTargetRegex = Regex("""(?://\s*|#\s*)?\{\{/TARGET:([a-zA-Z0-9_-]+)\}""")

    fun shouldIncludePath(path: String, activeTargets: Set<String>): Boolean {
        if (activeTargets.isEmpty()) return true
        val normalized = activeTargets.map { it.lowercase().trim() }.toSet()

        val hasDesktop = normalized.contains("desktop") || normalized.contains("jvm")
        val hasWeb = normalized.contains("web") || normalized.contains("wasm") || normalized.contains("js")
        val hasServer = normalized.contains("server")
        val hasIos = normalized.any { it.startsWith("ios") }
        val hasAndroid = normalized.contains("android")

        // Desktop module
        if (!hasDesktop && (path.startsWith("desktopApp/") || path.startsWith("app/desktopApp/") || path == ".run/desktopApp.run.xml")) {
            return false
        }

        // Web module
        if (!hasWeb && (path.startsWith("webApp/") || path.startsWith("app/webApp/") || path.startsWith("kotlin-js-store/") || path == "package.json" || path == ".run/webApp.run.xml" || path.contains("src/jsMain/"))) {
            return false
        }

        // Server module
        if (!hasServer && (path.startsWith("server/") || path == ".run/server.run.xml")) {
            return false
        }

        // iOS module
        if (!hasIos && (path.startsWith("iosApp/") || path.startsWith("app/iosApp/") || path.contains("src/iosMain/"))) {
            return false
        }

        // Android module
        if (!hasAndroid && (path.startsWith("androidApp/") || path.startsWith("app/androidApp/") || path.contains("src/androidMain/"))) {
            return false
        }

        return true
    }

    fun process(
        rawFiles: Map<String, String>,
        context: TemplateContext,
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((rawPath, rawContent) in rawFiles) {
            val resolvedPath = processPath(rawPath, context.variables)
            if (!shouldIncludePath(resolvedPath, context.activeTargets)) {
                continue
            }
            val resolvedContent = processContent(rawContent, context, resolvedPath)
            result[resolvedPath] = resolvedContent
        }
        return result
    }

    fun processPath(path: String, variables: Map<String, String>): String {
        var resolved = path
        for ((key, value) in variables) {
            resolved = resolved.replace("{{$key}}", value)
        }
        return resolved
    }

    fun processContent(content: String, context: TemplateContext, path: String = ""): String {
        val filtered = processConditionals(content, context.activeTargets)
        val pruned = pruneProjectWiring(filtered, context.activeTargets, path)
        var resolved = pruned
        for ((key, value) in context.variables) {
            resolved = resolved.replace("{{$key}}", value)
        }
        return resolved
    }

    private fun pruneProjectWiring(content: String, activeTargets: Set<String>, path: String): String {
        if (activeTargets.isEmpty()) return content
        val normalized = activeTargets.map { it.lowercase().trim() }.toSet()
        val hasDesktop = normalized.contains("desktop") || normalized.contains("jvm")
        val hasWeb = normalized.contains("web") || normalized.contains("wasm") || normalized.contains("js")
        val hasServer = normalized.contains("server")
        val hasAndroid = normalized.contains("android")

        if (path.endsWith("settings.gradle.kts")) {
            return content.lines().filter { line ->
                val trimmed = line.trim()
                val isDesktopLine = trimmed.contains("desktopApp")
                val isWebLine = trimmed.contains("webApp")
                val isServerLine = trimmed.contains(":server")
                val isAndroidLine = trimmed.contains("androidApp")

                when {
                    !hasDesktop && isDesktopLine -> false
                    !hasWeb && isWebLine -> false
                    !hasServer && isServerLine -> false
                    !hasAndroid && isAndroidLine -> false
                    else -> true
                }
            }.joinToString("\n")
        }

        if (path == "build.gradle.kts") {
            var updated = content
            if (!hasDesktop && !hasServer) {
                updated = updated.lines().filter { !it.contains("alias(libs.plugins.kotlinJvm)") }.joinToString("\n")
            }
            if (!hasWeb && (updated.contains("webInstall") || updated.contains("npmExecutable"))) {
                updated = updated.lines().filter {
                    !it.contains("val isWindows =") && !it.contains("val npmExecutable =")
                }.joinToString("\n")
                updated = removeBlock(updated, "val webInstall =")
                updated = removeBlock(updated, "tasks.register<Exec>(\"webRun\")")
            }
            return updated
        }

        if (path.endsWith("shared/build.gradle.kts") || path.endsWith("sharedUI/build.gradle.kts") || path.endsWith("core/build.gradle.kts")) {
            var updated = content
            if (!hasWeb) {
                updated = removeBlock(updated, "js {")
                updated = removeBlock(updated, "jsMain.dependencies {")
            }
            if (!hasDesktop && !hasServer) {
                updated = removeBlock(updated, "jvm()")
                updated = removeBlock(updated, "jvmMain.dependencies {")
            }
            return updated
        }

        return content
    }

    private fun removeBlock(content: String, marker: String): String {
        if (!content.contains(marker)) return content
        val lines = content.lines()
        val result = mutableListOf<String>()
        var openBraces = 0
        var inBlock = false

        for (line in lines) {
            if (line.contains(marker)) {
                if (marker == "jvm()") {
                    continue
                }
                inBlock = true
                openBraces += line.count { it == '{' } - line.count { it == '}' }
                continue
            }
            if (inBlock) {
                openBraces += line.count { it == '{' } - line.count { it == '}' }
                if (openBraces <= 0) {
                    inBlock = false
                }
                continue
            }
            result.add(line)
        }
        return result.joinToString("\n")
    }

    private fun processConditionals(content: String, activeTargets: Set<String>): String {
        val normalizedTargets = activeTargets.map { it.lowercase().trim() }.toSet()
        val lines = content.lines()
        val result = mutableListOf<String>()
        var skipCurrentBlock = false

        for (line in lines) {
            val trimmed = line.trim()
            val startMatch = startTargetRegex.find(trimmed)
            if (startMatch != null) {
                val targetName = startMatch.groupValues[1].lowercase()
                skipCurrentBlock = !normalizedTargets.contains(targetName)
                continue
            }

            val endMatch = endTargetRegex.find(trimmed)
            if (endMatch != null) {
                skipCurrentBlock = false
                continue
            }

            if (!skipCurrentBlock) {
                result.add(line)
            }
        }
        return result.joinToString("\n")
    }
}
