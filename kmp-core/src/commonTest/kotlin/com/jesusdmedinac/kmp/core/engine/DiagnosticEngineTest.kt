package com.jesusdmedinac.kmp.core.engine

import com.jesusdmedinac.kmp.core.checker.DiagnosticChecker
import com.jesusdmedinac.kmp.core.model.CheckResult
import com.jesusdmedinac.kmp.core.model.CheckStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiagnosticEngineTest {
    private class MockChecker(
        override val id: String,
        override val title: String,
        private val result: CheckResult,
    ) : DiagnosticChecker {
        override suspend fun check(): CheckResult = result
    }

    @Test
    fun `diagnose produces overall SUCCESS when all checkers succeed`() = runTest {
        val checker1 = MockChecker("c1", "Checker 1", CheckResult("c1", "Checker 1", CheckStatus.SUCCESS, "Ok"))
        val checker2 = MockChecker("c2", "Checker 2", CheckResult("c2", "Checker 2", CheckStatus.SUCCESS, "Ok"))
        val engine = DiagnosticEngine(listOf(checker1, checker2))

        val report = engine.diagnose("1.0.0")

        assertEquals(CheckStatus.SUCCESS, report.overallStatus)
        assertTrue(report.isHealthy)
        assertEquals(2, report.checks.size)
    }

    @Test
    fun `diagnose produces overall WARNING when one checker has warning and none fail`() = runTest {
        val checker1 = MockChecker("c1", "Checker 1", CheckResult("c1", "Checker 1", CheckStatus.SUCCESS, "Ok"))
        val checker2 = MockChecker("c2", "Checker 2", CheckResult("c2", "Checker 2", CheckStatus.WARNING, "Warn"))
        val engine = DiagnosticEngine(listOf(checker1, checker2))

        val report = engine.diagnose("1.0.0")

        assertEquals(CheckStatus.WARNING, report.overallStatus)
        assertTrue(report.isHealthy)
        assertEquals(2, report.checks.size)
    }

    @Test
    fun `diagnose produces overall FAILURE when any checker fails`() = runTest {
        val checker1 = MockChecker("c1", "Checker 1", CheckResult("c1", "Checker 1", CheckStatus.SUCCESS, "Ok"))
        val checker2 = MockChecker("c2", "Checker 2", CheckResult("c2", "Checker 2", CheckStatus.FAILURE, "Fail"))
        val engine = DiagnosticEngine(listOf(checker1, checker2))

        val report = engine.diagnose("1.0.0")

        assertEquals(CheckStatus.FAILURE, report.overallStatus)
        assertFalse(report.isHealthy)
        assertEquals(2, report.checks.size)
    }
}
