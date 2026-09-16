package com.jesusdmedinac.kmp.core.project.parser

import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class UnifiedProjectParser(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    private val gradleParser = GradleProjectParser(systemEnvironment)
    private val toolchainParser = KotlinToolchainParser(systemEnvironment)

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
