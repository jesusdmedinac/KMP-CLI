package com.jesusdmedinac.kmp.core.model

import kotlinx.serialization.Serializable

/**
 * Actionable remediation advice for resolving a diagnostic issue.
 *
 * @property description Human-readable explanation of how to fix the issue.
 * @property command Optional terminal command that can be executed directly by a developer or AI agent.
 * @property docUrl Optional documentation URL providing extended troubleshooting guidance.
 */
@Serializable
data class Remediation(
    val description: String,
    val command: String? = null,
    val docUrl: String? = null,
)
