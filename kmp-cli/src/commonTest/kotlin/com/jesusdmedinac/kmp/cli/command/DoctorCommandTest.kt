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

    @Test
    fun `doctor with architecture flag flags violations and exits with code 1`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        val fakeEnv = com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment()
        fakeEnv.writeFileText(
            "src/presentation/VehicleScreen.kt",
            """
                package com.example.presentation
                import com.example.data.VehicleDao
            """.trimIndent()
        )
        val archChecker = com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker(fakeEnv)
        val command = DoctorCommand(architectureChecker = archChecker, terminal = terminal)

        val exception = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, listOf("--architecture")) { it.run() }
        }

        assertEquals(1, exception.statusCode)
        val output = recorder.output()
        assertTrue(output.contains("ARCH001_UI_DATA_LEAK"))
        assertTrue(output.contains("VehicleDao"))
        assertTrue(output.contains("Inject the domain interface"))
    }

    @Test
    fun `doctor with architecture and json outputs parseable ArchitectureReport`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        val fakeEnv = com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment()
        fakeEnv.writeFileText(
            "src/presentation/VehicleViewModel.kt",
            """
                package com.example.presentation
                import kotlinx.coroutines.flow.MutableStateFlow
                class VehicleViewModel {
                    val state = MutableStateFlow("")
                }
            """.trimIndent()
        )
        val archChecker = com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker(fakeEnv)
        val command = DoctorCommand(architectureChecker = archChecker, terminal = terminal)

        val exception = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, listOf("--architecture", "--json")) { it.run() }
        }

        assertEquals(1, exception.statusCode)
        val output = recorder.output().trim()
        val report = Json.decodeFromString<com.jesusdmedinac.kmp.core.architecture.model.ArchitectureReport>(output)
        assertEquals(false, report.passed)
        assertEquals(1, report.violations.size)
        assertEquals("ARCH002_MUTABLE_STATE_EXPOSURE", report.violations.first().ruleId)
    }

    @Test
    fun `doctor with architecture guides developer when living foundation runners are missing`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        val fakeEnv = com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment()
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """
            rootProject.name = "MyAwesomeApp"
            include(":composeApp")
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            plugins {
                id("org.jetbrains.compose")
            }
            kotlin {
                androidTarget()
                jvm("desktop")
            }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "composeApp/src/androidMain/kotlin/MainActivity.kt",
            "class MainActivity : ComponentActivity()"
        )

        val archChecker = com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker(fakeEnv)
        val command = DoctorCommand(architectureChecker = archChecker, terminal = terminal)

        CommandLineParser.parseAndRun(command, listOf("--architecture")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("ARCH004_MISSING_PLATFORM_RUNNER"))
        assertTrue(output.contains("Stage 1: Living Foundation"))
        assertTrue(output.contains("fun main() = application"))
    }
}
