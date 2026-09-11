package com.jesusdmedinac.kmp.core.model

import kotlinx.serialization.Serializable

/**
 * Status level for individual checks and overall diagnostic reports.
 */
@Serializable
enum class CheckStatus {
    /** The check passed without issues. */
    SUCCESS,

    /** The check detected non-fatal warnings or optional missing tools. */
    WARNING,

    /** The check encountered a critical error preventing KMP development. */
    FAILURE,
}
