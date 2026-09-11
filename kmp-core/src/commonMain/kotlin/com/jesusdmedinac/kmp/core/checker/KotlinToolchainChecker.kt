package com.jesusdmedinac.kmp.core.checker

import com.jesusdmedinac.kmp.core.model.CheckResult
import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.model.Remediation
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class KotlinToolchainChecker(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) : DiagnosticChecker {
    override val id: String = "kotlinToolchain"
    override val title: String = "Kotlin Toolchain (CLI / 0.12+)"

    override suspend fun check(): CheckResult {
        // 1. Check for local ./kotlin wrapper
        if (systemEnvironment.fileExists("./kotlin")) {
            val wrapperResult = systemEnvironment.execute(listOf("./kotlin", "version"))
            if (wrapperResult.exitCode == 0) {
                val output = wrapperResult.output
                return CheckResult(
                    id = id,
                    title = title,
                    status = CheckStatus.SUCCESS,
                    message = "Local Kotlin Toolchain wrapper detected: $output",
                    details = listOf("Path: ./kotlin", "Version: $output"),
                    remediation = null
                )
            }
        }

        // 2. Check for global kotlin CLI
        val globalResult = systemEnvironment.execute(listOf("kotlin", "-version"))
        if (globalResult.exitCode == 0) {
            val output = globalResult.output
            return CheckResult(
                id = id,
                title = title,
                status = CheckStatus.SUCCESS,
                message = "Kotlin CLI detected: $output",
                details = listOf("Version: $output"),
                remediation = null
            )
        }

        // 3. Neither found - return WARNING with remediation
        return CheckResult(
            id = id,
            title = title,
            status = CheckStatus.WARNING,
            message = "Kotlin CLI / Toolchain is not installed.",
            details = listOf(
                "Local wrapper './kotlin' not found",
                "Global 'kotlin' binary not found in PATH"
            ),
            remediation = Remediation(
                description = "Install Kotlin CLI / Toolchain (optional for Gradle, required for standalone Kotlin Toolchain projects)",
                command = "brew install kotlin",
                docUrl = "https://kotlinlang.org/docs/command-line.html"
            )
        )
    }
}
