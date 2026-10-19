package com.jesusdmedinac.kmp.core.project.parser

import com.jesusdmedinac.kmp.core.project.model.PlatformRunnerInfo
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

/**
 * Detects active and missing executable platform runners (Android, iOS, Desktop, Wasm)
 * for declared targets in Kotlin Multiplatform projects.
 */
class PlatformRunnerDetector(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    fun detect(projectRoot: String = ".", declaredTargets: List<String> = emptyList()): List<PlatformRunnerInfo> {
        val root = if (projectRoot == "." || projectRoot.isEmpty()) "" else projectRoot.trimEnd('/') + "/"
        val allFiles = systemEnvironment.listFilesRecursively(projectRoot)

        val androidRunner = findAndroidRunner(root, allFiles)
        val iosRunner = findIosRunner(root, allFiles)
        val desktopRunner = findDesktopRunner(root, allFiles)
        val wasmRunner = findWasmRunner(root, allFiles)

        if (declaredTargets.isEmpty()) {
            return listOfNotNull(androidRunner, iosRunner, desktopRunner, wasmRunner)
        }

        return declaredTargets.map { target ->
            val normalized = target.lowercase()
            when {
                normalized.contains("android") -> {
                    androidRunner?.copy(target = target) ?: PlatformRunnerInfo(
                        target = target,
                        present = false,
                        entryPoint = null,
                        runnerType = "Android Activity",
                    )
                }
                normalized.startsWith("ios") || normalized.contains("apple") -> {
                    iosRunner?.copy(target = target) ?: PlatformRunnerInfo(
                        target = target,
                        present = false,
                        entryPoint = null,
                        runnerType = "iOS UIViewController / Swift Entry",
                    )
                }
                normalized == "desktop" || normalized == "jvm" -> {
                    desktopRunner?.copy(target = target) ?: PlatformRunnerInfo(
                        target = target,
                        present = false,
                        entryPoint = null,
                        runnerType = "Desktop Application Entry",
                    )
                }
                normalized.startsWith("wasm") || normalized == "js" || normalized.contains("web") -> {
                    wasmRunner?.copy(target = target) ?: PlatformRunnerInfo(
                        target = target,
                        present = false,
                        entryPoint = null,
                        runnerType = "Wasm/Web Canvas/Viewport Entry",
                    )
                }
                else -> {
                    val genericRunner = findGenericRunner(root, allFiles, target)
                    genericRunner ?: PlatformRunnerInfo(
                        target = target,
                        present = false,
                        entryPoint = null,
                        runnerType = "Executable Entry Point",
                    )
                }
            }
        }
    }

    private fun findAndroidRunner(root: String, files: List<String>): PlatformRunnerInfo? {
        val candidate = files.firstOrNull { file ->
            if (isIgnoredPath(file)) return@firstOrNull false

            val isAndroidPath = file.contains("androidMain") || file.contains("androidApp") || file.contains("src/main/")
            if (isAndroidPath && (file.endsWith("Activity.kt") || file.endsWith("MainActivity.kt"))) {
                return@firstOrNull true
            }

            if (isAndroidPath && file.endsWith(".kt")) {
                val fullPath = if (file.startsWith("/")) file else "$root$file"
                val content = systemEnvironment.readFileText(fullPath) ?: ""
                content.contains("ComponentActivity") || content.contains(": Activity") ||
                        (content.contains("setContent") && content.contains("@Composable"))
            } else {
                false
            }
        }

        return candidate?.let {
            val entryPath = if (root.isNotEmpty() && !it.startsWith(root)) "$root$it" else it
            PlatformRunnerInfo(
                target = "android",
                present = true,
                entryPoint = entryPath,
                runnerType = "Android Activity",
            )
        }
    }

    private fun findIosRunner(root: String, files: List<String>): PlatformRunnerInfo? {
        val candidate = files.firstOrNull { file ->
            if (isIgnoredPath(file)) return@firstOrNull false

            if (file.endsWith("iOSApp.swift") || file.endsWith("App.swift") && file.contains("iosApp")) {
                return@firstOrNull true
            }

            val isIosPath = file.contains("iosMain") || file.contains("iosApp") || file.contains("src/ios/")
            if (isIosPath && (file.endsWith("MainViewController.kt") || file.endsWith("ViewController.kt"))) {
                return@firstOrNull true
            }

            if (isIosPath && file.endsWith(".kt")) {
                val fullPath = if (file.startsWith("/")) file else "$root$file"
                val content = systemEnvironment.readFileText(fullPath) ?: ""
                content.contains("ComposeUIViewController") || content.contains("UIViewController")
            } else {
                false
            }
        }

        return candidate?.let {
            val entryPath = if (root.isNotEmpty() && !it.startsWith(root)) "$root$it" else it
            PlatformRunnerInfo(
                target = "ios",
                present = true,
                entryPoint = entryPath,
                runnerType = "iOS UIViewController / Swift Entry",
            )
        }
    }

    private fun findDesktopRunner(root: String, files: List<String>): PlatformRunnerInfo? {
        val candidate = files.firstOrNull { file ->
            if (isIgnoredPath(file)) return@firstOrNull false

            val isDesktopPath = file.contains("desktopApp") || file.contains("desktopMain") ||
                    file.contains("jvmMain") || file.contains("src/jvm/") || file.contains("desktop/")
            if (isDesktopPath && (file.endsWith("main.kt") || file.endsWith("Main.kt"))) {
                val fullPath = if (file.startsWith("/")) file else "$root$file"
                val content = systemEnvironment.readFileText(fullPath) ?: ""
                content.contains("application") || content.contains("Window(") ||
                        content.contains("singleWindowApplication") || content.contains("fun main(")
            } else {
                false
            }
        }

        return candidate?.let {
            val entryPath = if (root.isNotEmpty() && !it.startsWith(root)) "$root$it" else it
            PlatformRunnerInfo(
                target = "desktop",
                present = true,
                entryPoint = entryPath,
                runnerType = "Desktop Application Entry",
            )
        }
    }

    private fun findWasmRunner(root: String, files: List<String>): PlatformRunnerInfo? {
        val candidate = files.firstOrNull { file ->
            if (isIgnoredPath(file)) return@firstOrNull false

            val isWebPath = file.contains("wasmJsMain") || file.contains("wasmMain") ||
                    file.contains("webApp") || file.contains("jsMain") || file.contains("webMain")
            if (isWebPath && (file.endsWith("main.kt") || file.endsWith("Main.kt"))) {
                val fullPath = if (file.startsWith("/")) file else "$root$file"
                val content = systemEnvironment.readFileText(fullPath) ?: ""
                content.contains("ComposeViewport") || content.contains("CanvasBasedWindow") ||
                        content.contains("renderComposable") || content.contains("fun main(")
            } else if (file.contains("webApp") && (file.endsWith("App.tsx") || file.endsWith("index.html"))) {
                true
            } else {
                false
            }
        }

        return candidate?.let {
            val entryPath = if (root.isNotEmpty() && !it.startsWith(root)) "$root$it" else it
            PlatformRunnerInfo(
                target = "wasm",
                present = true,
                entryPoint = entryPath,
                runnerType = "Wasm/Web Canvas/Viewport Entry",
            )
        }
    }

    private fun findGenericRunner(root: String, files: List<String>, target: String): PlatformRunnerInfo? {
        val candidate = files.firstOrNull { file ->
            if (isIgnoredPath(file)) return@firstOrNull false
            val matchesTarget = file.contains(target) || file.contains("${target}Main")
            if (matchesTarget && (file.endsWith("main.kt") || file.endsWith("Main.kt"))) {
                val fullPath = if (file.startsWith("/")) file else "$root$file"
                val content = systemEnvironment.readFileText(fullPath) ?: ""
                content.contains("fun main(")
            } else {
                false
            }
        }

        return candidate?.let {
            val entryPath = if (root.isNotEmpty() && !it.startsWith(root)) "$root$it" else it
            PlatformRunnerInfo(
                target = target,
                present = true,
                entryPoint = entryPath,
                runnerType = "Executable Entry Point",
            )
        }
    }

    private fun isIgnoredPath(path: String): Boolean {
        return path.startsWith("build/") || path.contains("/build/") ||
                path.startsWith(".gradle/") || path.contains("/.gradle/") ||
                path.startsWith(".git/") || path.contains("/.git/") ||
                path.startsWith(".idea/") || path.contains("/.idea/") ||
                path.startsWith(".kmp-cache/") || path.contains("/.kmp-cache/") ||
                path.startsWith("templates/") || path.startsWith("playground/") ||
                path.startsWith("wizard/") || path.startsWith("jetbrains/") ||
                path.contains("/test/") || path.contains("/commonTest/") ||
                path.contains("/jvmTest/") || path.contains("/androidTest/") ||
                path.endsWith("Test.kt")
    }
}
