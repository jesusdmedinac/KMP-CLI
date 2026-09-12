package com.jesusdmedinac.kmp.core.checker

import com.jesusdmedinac.kmp.core.model.CheckResult
import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.model.Remediation
import com.jesusdmedinac.kmp.core.system.OperatingSystem
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class KDoctorChecker(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) : DiagnosticChecker {
    override val id: String = "kdoctor"
    override val title: String = "KDoctor (Mobile & Tooling Diagnostics)"

    override suspend fun check(): CheckResult {
        if (systemEnvironment.operatingSystem != OperatingSystem.MACOS) {
            return CheckResult(
                id = id,
                title = title,
                status = CheckStatus.SUCCESS,
                message = "Skipped (kdoctor is only applicable on macOS).",
                details = listOf("Operating system is ${systemEnvironment.operatingSystem}"),
                remediation = null
            )
        }

        val versionResult = systemEnvironment.execute(listOf("kdoctor", "--version"))
        if (versionResult.exitCode != 0) {
            return CheckResult(
                id = id,
                title = title,
                status = CheckStatus.WARNING,
                message = "kdoctor is not installed. Mobile iOS/Xcode diagnostics skipped.",
                details = listOf("Install kdoctor for full Xcode, Android Studio, and CocoaPods environment diagnostics."),
                remediation = Remediation(
                    description = "Install kdoctor via Homebrew",
                    command = "brew install kdoctor",
                    docUrl = "https://github.com/Kotlin/kdoctor"
                )
            )
        }

        val runResult = systemEnvironment.execute(listOf("kdoctor"))
        val rawOutput = runResult.output
        val cleanedLines = rawOutput
            .replace("\r", "\n")
            .lines()
            .map { cleanSpinner(it) }
            .filter { it.isNotBlank() }

        val hasFailure = runResult.exitCode != 0 || cleanedLines.any { line ->
            line.contains("[x]") || line.contains("[✕]") || line.contains("[✖]")
        }
        val hasWarning = cleanedLines.any { it.contains("[!]") }

        val summaryDetails = cleanedLines
            .filter { line ->
                line.startsWith("[✓]") || line.startsWith("[!]") || line.startsWith("[x]") ||
                        line.startsWith("[✕]") || line.startsWith("[✖]") || line.startsWith("!") ||
                        line.startsWith("x") || line.startsWith("Install")
            }

        return when {
            hasFailure -> CheckResult(
                id = id,
                title = title,
                status = CheckStatus.FAILURE,
                message = "kdoctor reported failures in mobile development environment.",
                details = summaryDetails.ifEmpty { listOf(rawOutput) },
                remediation = Remediation(
                    description = "Review kdoctor failure details above and resolve missing requirements",
                    command = "kdoctor -v",
                    docUrl = "https://github.com/Kotlin/kdoctor"
                )
            )
            hasWarning -> CheckResult(
                id = id,
                title = title,
                status = CheckStatus.WARNING,
                message = "kdoctor reported warnings in mobile environment.",
                details = summaryDetails.ifEmpty { listOf(rawOutput) },
                remediation = Remediation(
                    description = "Review kdoctor recommendations or run verbose diagnosis",
                    command = "kdoctor -v",
                    docUrl = "https://github.com/Kotlin/kdoctor"
                )
            )
            else -> CheckResult(
                id = id,
                title = title,
                status = CheckStatus.SUCCESS,
                message = "kdoctor: Environment is ready for Kotlin Multiplatform Mobile development.",
                details = summaryDetails.ifEmpty { listOf(rawOutput) },
                remediation = null
            )
        }
    }

    private fun cleanSpinner(line: String): String {
        val noAnsi = line.replace(Regex("""\u001B\[[0-9;]*[a-zA-Z]"""), "")
        val cleaned = noAnsi.replace(Regex("""\[[\u2800-\u28FF]\]\s*[^\[]*"""), "").trim()
        return cleaned.ifBlank { noAnsi.trim() }
    }
}
