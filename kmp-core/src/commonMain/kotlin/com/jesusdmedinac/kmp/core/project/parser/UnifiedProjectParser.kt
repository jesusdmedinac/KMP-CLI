package com.jesusdmedinac.kmp.core.project.parser

import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

/**
 * Unified facade parser for Kotlin Multiplatform projects supporting both Gradle and Kotlin Toolchain.
 *
 * Detection precedence:
 * 1. **Kotlin Toolchain (Amper)**: Checks for `project.yaml` or `module.yaml`. If present and valid,
 *    returns the toolchain project descriptor.
 * 2. **Gradle**: Checks for `settings.gradle(.kts)` or root `build.gradle(.kts)`. If present and valid,
 *    returns the Gradle project descriptor.
 * 3. Returns `null` if neither build system is detected.
 */
class UnifiedProjectParser(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    private val gradleParser = GradleProjectParser(systemEnvironment)
    private val toolchainParser = KotlinToolchainParser(systemEnvironment)

    /**
     * Inspects the given [projectRoot] directory and parses its build descriptors into a unified [ProjectDescriptor].
     *
     * @param projectRoot Root directory to inspect. Defaults to `.`.
     * @return A [ProjectDescriptor] if a recognized project is found, or `null` otherwise.
     */
    fun parse(projectRoot: String = "."): ProjectDescriptor? {
        val root = if (projectRoot == "." || projectRoot.isEmpty()) "" else projectRoot.trimEnd('/') + "/"

        val isKotlinToolchain = systemEnvironment.fileExists("${root}project.yaml") ||
                systemEnvironment.fileExists("${root}module.yaml")

        if (isKotlinToolchain) {
            val descriptor = toolchainParser.parse(projectRoot)
            if (descriptor != null) return descriptor
        }

        val isGradle = systemEnvironment.fileExists("${root}settings.gradle.kts") ||
                systemEnvironment.fileExists("${root}settings.gradle") ||
                systemEnvironment.fileExists("${root}build.gradle.kts") ||
                systemEnvironment.fileExists("${root}build.gradle")

        if (isGradle) {
            val descriptor = gradleParser.parse(projectRoot)
            if (descriptor != null) return descriptor
        }

        return null
    }
}
