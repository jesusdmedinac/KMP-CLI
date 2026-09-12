package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.jesusdmedinac.kmp.core.checker.DiagnosticChecker
import com.jesusdmedinac.kmp.core.engine.DiagnosticEngine
import com.jesusdmedinac.kmp.core.model.CheckResult
import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.model.DiagnosticReport
import com.jesusdmedinac.kmp.core.model.Remediation
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DoctorCommandTest {
    private class FakeChecker(
        override val id: String,
        override val title: String,
        private val result: CheckResult,
    ) : DiagnosticChecker {
        override suspend fun check(): CheckResult = result
    }

    @Test
    fun `human output displays title checks and conclusion`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        val checker = FakeChecker(
            id = "mockJdk",
            title = "Mock JDK Check",
            result = CheckResult(
                id = "mockJdk",
                title = "Mock JDK Check",
                status = CheckStatus.SUCCESS,
                message = "JDK is configured properly",
                details = listOf("Version 21.0.2")
            )
        )
        val engine = DiagnosticEngine(listOf(checker))
        val command = DoctorCommand(engine = engine, terminal = terminal)

        CommandLineParser.parseAndRun(command, emptyList()) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("KMP Environment Doctor"))
        assertTrue(output.contains("Mock JDK Check"))
        assertTrue(output.contains("JDK is configured properly"))
        assertTrue(output.contains("fully configured for Kotlin Multiplatform"))
    }

    @Test
    fun `json flag outputs valid parseable DiagnosticReport JSON`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        val checker = FakeChecker(
            id = "mockToolchain",
            title = "Mock Toolchain",
            result = CheckResult(
                id = "mockToolchain",
                title = "Mock Toolchain",
                status = CheckStatus.WARNING,
                message = "Toolchain is missing",
                remediation = Remediation(
                    description = "Install toolchain",
                    command = "brew install kotlin"
                )
            )
        )
        val engine = DiagnosticEngine(listOf(checker))
        val command = DoctorCommand(engine = engine, terminal = terminal)

        CommandLineParser.parseAndRun(command, listOf("--json")) { it.run() }

        val output = recorder.output().trim()
        val parsedReport = Json.decodeFromString<DiagnosticReport>(output)

        assertEquals(CheckStatus.WARNING, parsedReport.overallStatus)
        assertEquals(1, parsedReport.checks.size)
        assertEquals("mockToolchain", parsedReport.checks.first().id)
        assertEquals("brew install kotlin", parsedReport.checks.first().remediation?.command)
    }

    @Test
    fun `throws ProgramResult with exit code 1 when critical check fails`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        val checker = FakeChecker(
            id = "failingCheck",
            title = "Failing Component",
            result = CheckResult(
                id = "failingCheck",
                title = "Failing Component",
                status = CheckStatus.FAILURE,
                message = "Missing critical component"
            )
        )
        val engine = DiagnosticEngine(listOf(checker))
        val command = DoctorCommand(engine = engine, terminal = terminal)

        val exception = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, emptyList()) { it.run() }
        }

        assertEquals(1, exception.statusCode)
    }
}
