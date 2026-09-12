package com.jesusdmedinac.kmp.core.engine

import com.jesusdmedinac.kmp.core.checker.DiagnosticChecker
import com.jesusdmedinac.kmp.core.checker.JdkChecker
import com.jesusdmedinac.kmp.core.checker.KDoctorChecker
import com.jesusdmedinac.kmp.core.checker.KotlinToolchainChecker
import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.model.DiagnosticReport
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DiagnosticEngine(
    private val checkers: List<DiagnosticChecker>,
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    constructor(systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment()) : this(
        checkers = listOf(
            KDoctorChecker(systemEnvironment),
            JdkChecker(systemEnvironment),
            KotlinToolchainChecker(systemEnvironment),
        ),
        systemEnvironment = systemEnvironment,
    )

    suspend fun diagnose(version: String = "0.1.0"): DiagnosticReport = coroutineScope {
        val checkResults = checkers.map { checker ->
            async { checker.check() }
        }.awaitAll()

        val overallStatus = when {
            checkResults.any { it.status == CheckStatus.FAILURE } -> CheckStatus.FAILURE
            checkResults.any { it.status == CheckStatus.WARNING } -> CheckStatus.WARNING
            else -> CheckStatus.SUCCESS
        }

        DiagnosticReport(
            version = version,
            timestamp = systemEnvironment.nowIso8601(),
            overallStatus = overallStatus,
            checks = checkResults,
        )
    }
}
