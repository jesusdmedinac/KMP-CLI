package com.jesusdmedinac.kmp.core.project.parser

import com.jesusdmedinac.kmp.core.project.model.BuildSystem
import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.project.model.ProjectModule
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class GradleProjectParser(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    fun parse(projectRoot: String = "."): ProjectDescriptor? {
        val root = if (projectRoot == "." || projectRoot.isEmpty()) "" else projectRoot.trimEnd('/') + "/"

        val settingsPathKts = "${root}settings.gradle.kts"
        val settingsPathGroovy = "${root}settings.gradle"
        val rootBuildKts = "${root}build.gradle.kts"
        val rootBuildGroovy = "${root}build.gradle"

        val hasSettings = systemEnvironment.fileExists(settingsPathKts) || systemEnvironment.fileExists(settingsPathGroovy)
        val hasBuild = systemEnvironment.fileExists(rootBuildKts) || systemEnvironment.fileExists(rootBuildGroovy)

        if (!hasSettings && !hasBuild) return null

        val settingsContent = when {
            systemEnvironment.fileExists(settingsPathKts) -> systemEnvironment.readFileText(settingsPathKts)
            systemEnvironment.fileExists(settingsPathGroovy) -> systemEnvironment.readFileText(settingsPathGroovy)
            else -> null
        } ?: ""

        val rootName = parseRootProjectName(settingsContent) ?: "kmp-project"
        val declaredModulePaths = parseIncludedModules(settingsContent)

        var detectedKotlinVersion = parseKotlinVersionFromBuild(rootBuildKts)
            ?: parseKotlinVersionFromBuild(rootBuildGroovy)

        val allTargets = mutableSetOf<String>()
        val modules = mutableListOf<ProjectModule>()

        // Check root build file targets
        val rootTargets = extractTargetsFromBuildFile(rootBuildKts) + extractTargetsFromBuildFile(rootBuildGroovy)
        allTargets.addAll(rootTargets)

        // Check each declared module
        for (modulePath in declaredModulePaths) {
            val relativeDir = modulePath.removePrefix(":").replace(":", "/")
            val moduleName = relativeDir.substringAfterLast("/")
            val moduleBuildKts = "${root}${relativeDir}/build.gradle.kts"
            val moduleBuildGroovy = "${root}${relativeDir}/build.gradle"

            val moduleTargets = extractTargetsFromBuildFile(moduleBuildKts) + extractTargetsFromBuildFile(moduleBuildGroovy)
            allTargets.addAll(moduleTargets)

            if (detectedKotlinVersion == null) {
                detectedKotlinVersion = parseKotlinVersionFromBuild(moduleBuildKts)
                    ?: parseKotlinVersionFromBuild(moduleBuildGroovy)
            }

            modules.add(
                ProjectModule(
                    name = moduleName,
                    path = modulePath,
                    targets = moduleTargets.toList().sorted(),
                )
            )
        }

        // Check version catalog for fallback kotlin version
        val catalogPath = "${root}gradle/libs.versions.toml"
        val versionCatalog = VersionCatalogParser(systemEnvironment).parse(catalogPath)
        if (detectedKotlinVersion == null) {
            detectedKotlinVersion = versionCatalog?.versions?.get("kotlin")
        }

        return ProjectDescriptor(
            name = rootName,
            buildSystem = BuildSystem.GRADLE,
            kotlinVersion = detectedKotlinVersion,
            targets = allTargets.toList().sorted(),
            modules = modules,
            dependencies = versionCatalog,
        )
    }

    private fun parseRootProjectName(content: String): String? {
        val regex = Regex("""rootProject\.name\s*=\s*["']([^"']+)["']""")
        return regex.find(content)?.groupValues?.get(1)
    }

    private fun parseIncludedModules(content: String): List<String> {
        val modules = mutableListOf<String>()
        val regex = Regex("""include\s*\(?\s*["']([^"']+)["']\s*\)?""")
        for (match in regex.findAll(content)) {
            val path = match.groupValues[1].trim()
            if (path.isNotEmpty()) {
                modules.add(path)
            }
        }
        return modules
    }

    private fun parseKotlinVersionFromBuild(path: String): String? {
        if (!systemEnvironment.fileExists(path)) return null
        val content = systemEnvironment.readFileText(path) ?: return null

        val multiplatformRegex = Regex("""(?:id\("org\.jetbrains\.kotlin\.multiplatform"\)|kotlin\("multiplatform"\))\s+version\s+["']([^"']+)["']""")
        multiplatformRegex.find(content)?.groupValues?.get(1)?.let { return it }

        val aliasRegex = Regex("""alias\(libs\.plugins\.kotlin(?:Multiplatform|\.multiplatform)\)""")
        // Handled via version catalog
        return null
    }

    private fun extractTargetsFromBuildFile(path: String): Set<String> {
        if (!systemEnvironment.fileExists(path)) return emptySet()
        val content = systemEnvironment.readFileText(path) ?: return emptySet()

        val targets = mutableSetOf<String>()

        val targetMatchers = listOf(
            Regex("""\bandroidTarget\b""") to "android",
            Regex("""\bandroid\s*\{""") to "android",
            Regex("""\biosX64\b""") to "iosX64",
            Regex("""\biosArm64\b""") to "iosArm64",
            Regex("""\biosSimulatorArm64\b""") to "iosSimulatorArm64",
            Regex("""\bjvm\b""") to "jvm",
            Regex("""\bdesktop\b""") to "jvm",
            Regex("""\bwasmJs\b""") to "wasmJs",
            Regex("""\bjs\b""") to "js",
            Regex("""\blinuxX64\b""") to "linuxX64",
            Regex("""\blinuxArm64\b""") to "linuxArm64",
            Regex("""\bmacosArm64\b""") to "macosArm64",
            Regex("""\bmacosX64\b""") to "macosX64",
            Regex("""\bmingwX64\b""") to "mingwX64",
        )

        for ((regex, targetName) in targetMatchers) {
            if (regex.containsMatchIn(content)) {
                targets.add(targetName)
            }
        }

        return targets
    }
}
