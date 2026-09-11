package com.jesusdmedinac.kmp.core.checker

import com.jesusdmedinac.kmp.core.model.CheckResult
import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.model.Remediation
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class JdkChecker(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) : DiagnosticChecker {
    override val id: String = "jdk"
    override val title: String = "Java Development Kit (JDK)"

    override suspend fun check(): CheckResult {
        val versionResult = systemEnvironment.execute(listOf("java", "-version"))
        if (versionResult.exitCode != 0) {
            return CheckResult(
                id = id,
                title = title,
                status = CheckStatus.FAILURE,
                message = "Java is not installed or not found in PATH.",
                details = listOfNotNull(versionResult.output.ifBlank { null } ?: "Exit code: ${versionResult.exitCode}"),
                remediation = Remediation(
                    description = "Install OpenJDK 17 or higher (recommended: brew install openjdk@17 or sdkman)",
                    command = "brew install openjdk@17",
                    docUrl = "https://kmp.jetbrains.com/docs"
                )
            )
        }

        val rawOutput = versionResult.output
        val versionString = extractJavaVersion(rawOutput)
        val majorVersion = parseMajorVersion(versionString)

        if (majorVersion == null || majorVersion < 17) {
            val detected = versionString ?: "unknown version"
            return CheckResult(
                id = id,
                title = title,
                status = CheckStatus.FAILURE,
                message = "Installed JDK version ($detected) is not supported. Kotlin Multiplatform and Gradle 8+ require JDK 17 or higher.",
                details = listOf("Detected version: $detected", "Required minimum: JDK 17"),
                remediation = Remediation(
                    description = "Upgrade to OpenJDK 17 or 21 (recommended: brew install openjdk@17)",
                    command = "brew install openjdk@17",
                    docUrl = "https://kmp.jetbrains.com/docs"
                )
            )
        }

        val javaHome = systemEnvironment.getEnv("JAVA_HOME")
        if (javaHome.isNullOrBlank()) {
            return CheckResult(
                id = id,
                title = title,
                status = CheckStatus.WARNING,
                message = "JDK $versionString is installed, but JAVA_HOME environment variable is not set.",
                details = listOf("Version: $versionString in PATH", "JAVA_HOME: not set"),
                remediation = Remediation(
                    description = "Set JAVA_HOME in your shell profile (e.g. ~/.zshrc or ~/.bashrc)",
                    command = "export JAVA_HOME=\$(/usr/libexec/java_home)",
                    docUrl = "https://kmp.jetbrains.com/docs"
                )
            )
        }

        if (!systemEnvironment.fileExists(javaHome)) {
            return CheckResult(
                id = id,
                title = title,
                status = CheckStatus.WARNING,
                message = "JAVA_HOME is set to '$javaHome', but the directory does not exist.",
                details = listOf("Version: $versionString in PATH", "JAVA_HOME: $javaHome (invalid)"),
                remediation = Remediation(
                    description = "Update JAVA_HOME to point to a valid JDK directory",
                    command = "export JAVA_HOME=\$(/usr/libexec/java_home)",
                    docUrl = "https://kmp.jetbrains.com/docs"
                )
            )
        }

        return CheckResult(
            id = id,
            title = title,
            status = CheckStatus.SUCCESS,
            message = "JDK $versionString detected.",
            details = listOf("Version: $versionString", "JAVA_HOME: $javaHome"),
            remediation = null
        )
    }

    private fun extractJavaVersion(output: String): String? {
        val regex = Regex("""version\s+"([^"]+)"""", RegexOption.IGNORE_CASE)
        return regex.find(output)?.groupValues?.get(1)
    }

    private fun parseMajorVersion(version: String?): Int? {
        if (version == null) return null
        val clean = version.trim()
        return if (clean.startsWith("1.")) {
            val parts = clean.split(".")
            parts.getOrNull(1)?.toIntOrNull()
        } else {
            val prefix = clean.substringBefore(".").substringBefore("-")
            prefix.toIntOrNull()
        }
    }
}
