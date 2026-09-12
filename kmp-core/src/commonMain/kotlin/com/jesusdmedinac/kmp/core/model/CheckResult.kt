package com.jesusdmedinac.kmp.core.model

import kotlinx.serialization.Serializable

/**
 * Result of executing an individual environment check.
 *
 * @property id Unique machine-readable identifier (e.g., "jdk", "kdoctor", "kotlin-toolchain").
 * @property title Human-readable title of the check.
 * @property status Severity and health status of the check.
 * @property message Concise summary message describing the outcome.
 * @property details Granular log lines, paths, or diagnostic notes.
 * @property remediation Optional remediation guidance if the check produced warnings or errors.
 */
@Serializable
data class CheckResult(
    val id: String,
    val title: String,
    val status: CheckStatus,
    val message: String,
    val details: List<String> = emptyList(),
    val remediation: Remediation? = null,
)
