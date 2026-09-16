package com.jesusdmedinac.kmp.core.project.parser

import com.jesusdmedinac.kmp.core.project.model.BuildSystem
import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.project.model.ProjectModule
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class KotlinToolchainParser(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    fun parse(projectRoot: String = "."): ProjectDescriptor? {
        val root = if (projectRoot == "." || projectRoot.isEmpty()) "" else projectRoot.trimEnd('/') + "/"

        val projectYamlPath = "${root}project.yaml"
        val rootModuleYamlPath = "${root}module.yaml"

        val hasProjectYaml = systemEnvironment.fileExists(projectYamlPath)
        val hasRootModuleYaml = systemEnvironment.fileExists(rootModuleYamlPath)

        if (!hasProjectYaml && !hasRootModuleYaml) return null

        var detectedKotlinVersion: String? = null
        val declaredModules = mutableListOf<String>()

        if (hasProjectYaml) {
            val content = systemEnvironment.readFileText(projectYamlPath) ?: ""
            detectedKotlinVersion = parseKotlinVersionFromYaml(content)
            declaredModules.addAll(parseModulesFromProjectYaml(content))
        }

        val allTargets = mutableSetOf<String>()
        val modules = mutableListOf<ProjectModule>()

        if (hasRootModuleYaml) {
            val rootModuleTargets = extractPlatformsFromModuleYaml(rootModuleYamlPath)
            allTargets.addAll(rootModuleTargets)
            modules.add(
                ProjectModule(
                    name = "root",
                    path = ".",
                    targets = rootModuleTargets.sorted(),
                )
            )
        }

        for (moduleName in declaredModules) {
            val modulePath = "${root}${moduleName}/module.yaml"
            val moduleTargets = extractPlatformsFromModuleYaml(modulePath)
            allTargets.addAll(moduleTargets)
            modules.add(
                ProjectModule(
                    name = moduleName,
                    path = moduleName,
                    targets = moduleTargets.sorted(),
                )
            )
        }

        val projectName = if (hasProjectYaml) {
            val content = systemEnvironment.readFileText(projectYamlPath) ?: ""
            parseProjectName(content) ?: "kotlin-toolchain-project"
        } else {
            "kotlin-toolchain-project"
        }

        return ProjectDescriptor(
            name = projectName,
            buildSystem = BuildSystem.KOTLIN_TOOLCHAIN,
            kotlinVersion = detectedKotlinVersion,
            targets = allTargets.toList().sorted(),
            modules = modules,
        )
    }

    private fun parseProjectName(content: String): String? {
        val regex = Regex("""name:\s*["']?([a-zA-Z0-9_.-]+)["']?""")
        return regex.find(content)?.groupValues?.get(1)
    }

    private fun parseKotlinVersionFromYaml(content: String): String? {
        val versionRegex = Regex("""(?:kotlin|version):\s*["']?([0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9_.-]+)?)["']?""")
        return versionRegex.find(content)?.groupValues?.get(1)
    }

    private fun parseModulesFromProjectYaml(content: String): List<String> {
        val modules = mutableListOf<String>()
        var inModulesSection = false

        for (rawLine in content.lines()) {
            val line = rawLine.substringBefore("#").trimEnd()
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("modules:")) {
                inModulesSection = true
                continue
            }

            if (inModulesSection) {
                if (line.startsWith("  - ") || line.startsWith("- ")) {
                    val mod = trimmed.removePrefix("-").trim().removeSurrounding("\"").removeSurrounding("'")
                    if (mod.isNotEmpty()) modules.add(mod)
                } else if (!line.startsWith(" ") && !line.startsWith("\t")) {
                    inModulesSection = false
                }
            }
        }
        return modules
    }

    private fun extractPlatformsFromModuleYaml(path: String): Set<String> {
        if (!systemEnvironment.fileExists(path)) return emptySet()
        val content = systemEnvironment.readFileText(path) ?: return emptySet()

        val platforms = mutableSetOf<String>()
        var inPlatformsSection = false

        for (rawLine in content.lines()) {
            val line = rawLine.substringBefore("#").trimEnd()
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("platforms:")) {
                inPlatformsSection = true
                continue
            }

            if (inPlatformsSection) {
                if (line.startsWith("  - ") || line.startsWith("    - ") || line.startsWith("- ")) {
                    val platform = trimmed.removePrefix("-").trim().removeSurrounding("\"").removeSurrounding("'")
                    if (platform.isNotEmpty()) platforms.add(platform)
                } else if (!line.startsWith(" ") && !line.startsWith("\t")) {
                    inPlatformsSection = false
                }
            }
        }

        return platforms
    }
}
