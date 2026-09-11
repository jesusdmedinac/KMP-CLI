package com.jesusdmedinac.kmp.core.checker

import com.jesusdmedinac.kmp.core.model.CheckResult

interface DiagnosticChecker {
    val id: String
    val title: String
    suspend fun check(): CheckResult
}
