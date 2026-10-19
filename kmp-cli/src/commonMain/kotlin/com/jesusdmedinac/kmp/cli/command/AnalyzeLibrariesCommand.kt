package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextColors.yellow
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import com.jesusdmedinac.kmp.core.klibs.KlibsCatalog
import com.jesusdmedinac.kmp.core.klibs.model.LibraryRecommendationResult
import com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Subcommand to query and verify library recommendations against the project's exact declared targets
 * using the klibs.io authoritative multiplatform library catalog.
 */
class AnalyzeLibrariesCommand(
    private val catalog: KlibsCatalog = KlibsCatalog(),
    private val projectParser: UnifiedProjectParser = UnifiedProjectParser(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "libraries",
) {
    val jsonOutput by option(
        "--json",
        help = "Output library recommendations and compatibility verification as JSON for AI agents.",
    ).flag(default = false)

    val projectPath by option(
        "--path",
        "-p",
        help = "Path to the KMP project root to evaluate declared targets against.",
    )

    val query by argument(
        "query",
        help = "Search query for library name, coordinate, category, or capability (e.g. room, ktor, koin).",
    ).default("")

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() {
        val root = projectPath ?: "."
        val descriptor = projectParser.parse(root)
        val targets = descriptor?.targets ?: emptyList()

        val result = catalog.query(query = query, projectTargets = targets)

        if (jsonOutput) {
            terminal.println(json.encodeToString(result))
        } else {
            renderHumanReport(result)
        }
    }

    private fun renderHumanReport(result: LibraryRecommendationResult) {
        terminal.println(bold("KMP Library Recommendations & Compatibility (klibs.io)"))
        if (result.projectTargets.isNotEmpty()) {
            terminal.println("${cyan("Project Targets (${result.projectTargets.size}):")} ${result.projectTargets.joinToString(", ")}")
        } else {
            terminal.println(dim("No project targets detected in current directory; displaying general ecosystem status."))
        }

        if (result.query.isNotBlank()) {
            terminal.println("${cyan("Search Filter:")} '${result.query}'")
        }
        terminal.println()

        if (result.libraries.isEmpty()) {
            terminal.println(yellow("  No verified libraries found matching query '${result.query}'. Try searching for 'persistence', 'networking', or 'navigation'."))
            return
        }

        for (lib in result.libraries) {
            val statusTag = if (lib.status == "Stable") {
                green("[Stable]")
            } else {
                yellow("[${lib.status}]")
            }

            terminal.println("  • ${bold(lib.coordinate)}:${lib.version} $statusTag")
            terminal.println("    ${dim("Name:")} ${lib.name} | ${dim("Category:")} ${lib.category}")
            terminal.println("    ${dim(lib.description)}")
            terminal.println("    ${dim("Supported Targets:")} ${lib.supportedTargets.joinToString(", ")}")

            if (result.projectTargets.isNotEmpty()) {
                if (lib.compatibleWithProject) {
                    val preStableNote = if (lib.status != "Stable") " (Warning: ${lib.status})" else ""
                    terminal.println("    ${cyan("Compatibility:")} ${green("✓ All project targets supported")}$preStableNote")
                } else {
                    terminal.println("    ${cyan("Compatibility:")} ${red("✗ Missing target support:")} ${lib.missingTargets.joinToString(", ")}")
                }
            }

            lib.docUrl?.let {
                terminal.println("    ${dim("Docs:")} $it")
            }
            terminal.println()
        }
    }
}
