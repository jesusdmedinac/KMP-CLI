package com.jesusdmedinac.kmp.core.system

data class ProcessResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String = "",
) {
    val isSuccess: Boolean get() = exitCode == 0

    val output: String
        get() = when {
            stdout.isBlank() -> stderr.trim()
            stderr.isBlank() -> stdout.trim()
            else -> "${stdout.trim()}\n${stderr.trim()}"
        }
}
