package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.danger
import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Command to inspect and describe Kotlin Multiplatform project architecture, modules, and target platforms.
 */
class DescribeCommand(
    private val parser: UnifiedProjectParser = UnifiedProjectParser(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "describe",
) {
    val jsonOutput by option(
        "--json",
        help = "Output project metadata as structured JSON for AI agents and automation.",
    ).flag(default = false)

    val projectPath by argument(
        "path",
        help = "Path to the KMP project root. Defaults to current directory.",
    ).default(".")

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() {
        val descriptor = parser.parse(projectPath)

        if (descriptor == null) {
            if (jsonOutput) {
                terminal.println("""{"error":"No Kotlin Multiplatform project found at '$projectPath'"}""")
            } else {
                terminal.danger("No Kotlin Multiplatform project found at '$projectPath'. Ensure build.gradle(.kts) or project.yaml exists.")
            }
            throw ProgramResult(1)
        }

        if (jsonOutput) {
            terminal.println(json.encodeToString(descriptor))
        } else {
            renderHumanReport(descriptor)
        }
    }

    private fun renderHumanReport(descriptor: ProjectDescriptor) {
        val buildSystemName = when (descriptor.buildSystem) {
            com.jesusdmedinac.kmp.core.project.model.BuildSystem.GRADLE -> "Gradle"
            com.jesusdmedinac.kmp.core.project.model.BuildSystem.KOTLIN_TOOLCHAIN -> "Kotlin Toolchain"
            com.jesusdmedinac.kmp.core.project.model.BuildSystem.UNKNOWN -> "Unknown"
        }

        terminal.println(bold("KMP Project Architecture: ${descriptor.name}"))
        terminal.println("${cyan("Build System:")} $buildSystemName")
        descriptor.kotlinVersion?.let {
            terminal.println("${cyan("Kotlin Version:")} $it")
        }
        terminal.println("${cyan("Targets (${descriptor.targets.size}):")} ${descriptor.targets.joinToString(", ")}")

        if (descriptor.modules.isNotEmpty()) {
            terminal.println(bold("\nModules (${descriptor.modules.size}):"))
            for (module in descriptor.modules) {
                val targetsStr = if (module.targets.isNotEmpty()) " [${module.targets.joinToString(", ")}]" else ""
                terminal.println("  • ${bold(module.name)} (${module.path})${dim(targetsStr)}")
            }
        }
    }
}
