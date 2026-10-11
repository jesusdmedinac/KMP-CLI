package com.jesusdmedinac.kmp.core.architecture.model

import kotlinx.serialization.Serializable

@Serializable
enum class ViolationSeverity {
    ERROR,
    WARNING,
    INFO,
}

@Serializable
data class RuleViolation(
    val ruleId: String,
    val severity: ViolationSeverity = ViolationSeverity.ERROR,
    val filePath: String,
    val lineNumber: Int,
    val message: String,
    val remediation: String,
)

@Serializable
data class ArchitectureReport(
    val passed: Boolean,
    val violations: List<RuleViolation> = emptyList(),
    val scannedFilesCount: Int = 0,
)
