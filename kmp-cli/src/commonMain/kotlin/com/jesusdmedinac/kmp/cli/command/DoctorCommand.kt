package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import com.jesusdmedinac.kmp.core.KmpCore
import com.jesusdmedinac.kmp.core.engine.DiagnosticEngine
import com.jesusdmedinac.kmp.core.model.CheckResult
import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker
import com.jesusdmedinac.kmp.core.architecture.model.ArchitectureReport
import com.jesusdmedinac.kmp.core.architecture.model.ViolationSeverity
import com.jesusdmedinac.kmp.core.model.DiagnosticReport
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DoctorCommand(
    private val engine: DiagnosticEngine = DiagnosticEngine(),
    private val architectureChecker: ArchitectureRuleChecker = ArchitectureRuleChecker(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "doctor",
) {
    val jsonOutput by option(
        "--json",
        help = "Output diagnostic report as structured JSON for AI agents and automation.",
    ).flag(default = false)

    val architectureOnly by option(
        "--architecture",
        "-a",
        help = "Run architectural linting checks on Kotlin source code and project boundaries.",
    ).flag(default = false)

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() = runBlocking {
        if (architectureOnly) {
            val report = architectureChecker.checkProject(".")
            if (jsonOutput) {
                terminal.println(json.encodeToString(report))
            } else {
                renderArchitectureReport(report)
            }

            if (!report.passed) {
                throw ProgramResult(1)
            }
            return@runBlocking
        }

        if (!jsonOutput) {
            terminal.println(bold("KMP Environment Doctor (v${KmpCore.VERSION})"))
            terminal.println(dim("Diagnosing Kotlin Multiplatform development environment...\n"))
        }

        val report = engine.diagnose(version = KmpCore.VERSION)

        if (jsonOutput) {
            terminal.println(json.encodeToString(report))
        } else {
            renderHumanReport(report)
        }

        if (!report.isHealthy) {
            throw ProgramResult(1)
        }
    }

    private fun renderArchitectureReport(report: ArchitectureReport) {
        terminal.println(bold("KMP Architectural Guard Report"))
        terminal.println(dim("Scanned ${report.scannedFilesCount} files for architectural boundaries and clean code rules.\n"))

        if (report.violations.isEmpty()) {
            terminal.println(green("  ✓ All architectural checks passed! Clean Architecture boundaries are strictly obeyed."))
            return
        }

        for (violation in report.violations) {
            val icon = when (violation.severity) {
                ViolationSeverity.ERROR -> red("✗")
                ViolationSeverity.WARNING -> yellow("!")
                ViolationSeverity.INFO -> cyan("ℹ")
            }
            terminal.println("  [$icon] ${bold(violation.ruleId)} at ${violation.filePath}:${violation.lineNumber}")
            terminal.println("      ${violation.message}")
            terminal.println(cyan("      ↳ Fix: ${violation.remediation}"))
            terminal.println()
        }

        terminal.println(bold("Conclusion:"))
        if (report.passed) {
            terminal.println(yellow("  ! Architectural checks passed with warnings."))
        } else {
            terminal.println(red("  ✗ Architectural violations detected. Please resolve errors before merging."))
        }
    }

    private fun renderHumanReport(report: DiagnosticReport) {
        for (check in report.checks) {
            renderCheck(check)
        }

        terminal.println()
        terminal.println(bold("Conclusion:"))
        when (report.overallStatus) {
            CheckStatus.SUCCESS -> {
                terminal.println(green("  ✓ Your environment is fully configured for Kotlin Multiplatform development!"))
            }
            CheckStatus.WARNING -> {
                terminal.println(yellow("  ! Your environment has warnings, but you can build and run KMP projects."))
            }
            CheckStatus.FAILURE -> {
                terminal.println(red("  ✗ Critical requirements missing. Please resolve the issues highlighted above."))
            }
        }
    }

    private fun renderCheck(check: CheckResult) {
        val icon = when (check.status) {
            CheckStatus.SUCCESS -> green("✓")
            CheckStatus.WARNING -> yellow("!")
            CheckStatus.FAILURE -> red("✗")
        }

        terminal.println("  [$icon] ${bold(check.title)}")
        terminal.println("      ${check.message}")

        for (detail in check.details) {
            terminal.println(dim("      $detail"))
        }

        check.remediation?.let { rem ->
            terminal.println(cyan("      ↳ Fix: ${rem.description}"))
            rem.command?.let { cmd ->
                terminal.println(brightCyan("      ↳ Run: $cmd"))
            }
            rem.docUrl?.let { url ->
                terminal.println(dim("      ↳ Docs: $url"))
            }
        }
        terminal.println()
    }
}
