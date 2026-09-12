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
import com.jesusdmedinac.kmp.core.model.DiagnosticReport
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DoctorCommand(
    private val engine: DiagnosticEngine = DiagnosticEngine(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "doctor",
) {
    val jsonOutput by option(
        "--json",
        help = "Output diagnostic report as structured JSON for AI agents and automation.",
    ).flag(default = false)

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() = runBlocking {
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
