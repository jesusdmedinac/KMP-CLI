package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.danger
import com.jesusdmedinac.kmp.core.project.model.ProjectDependencies
import com.jesusdmedinac.kmp.core.project.parser.VersionCatalogParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Command group for performing static inspection and dependency analysis on Kotlin Multiplatform projects.
 */
class AnalyzeCommand(
    versionCatalogParser: VersionCatalogParser = VersionCatalogParser(),
    terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "analyze",
) {
    init {
        subcommands(AnalyzeDependenciesCommand(versionCatalogParser, terminal))
    }

    override fun run() = Unit
}

/**
 * Subcommand to inspect and enumerate declared versions, libraries, and plugins from the version catalog.
 */
class AnalyzeDependenciesCommand(
    private val versionCatalogParser: VersionCatalogParser = VersionCatalogParser(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "dependencies",
) {
    val jsonOutput by option(
        "--json",
        help = "Output dependency analysis as structured JSON",
    ).flag(default = false)

    val projectPath by argument(
        "path",
        help = "Path to the KMP project root or directly to a version catalog .toml file. Defaults to current directory.",
    ).default(".")

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() {
        val catalogPath = if (projectPath.endsWith(".toml")) {
            projectPath
        } else {
            val root = if (projectPath == "." || projectPath.isEmpty()) "" else projectPath.trimEnd('/') + "/"
            "${root}gradle/libs.versions.toml"
        }
        val catalog = versionCatalogParser.parse(catalogPath)

        if (catalog == null) {
            if (jsonOutput) {
                terminal.println("""{"error":"No version catalog found at '$catalogPath'"}""")
            } else {
                terminal.danger("No version catalog found at '$catalogPath'. Ensure gradle/libs.versions.toml exists.")
            }
            throw ProgramResult(1)
        }

        if (jsonOutput) {
            terminal.println(json.encodeToString(catalog))
        } else {
            renderHumanReport(catalog, catalogPath)
        }
    }

    private fun renderHumanReport(catalog: ProjectDependencies, catalogPath: String) {
        terminal.println(bold("KMP Project Dependencies (from $catalogPath):"))

        if (catalog.versions.isNotEmpty()) {
            terminal.println("\n${cyan("Versions (${catalog.versions.size}):")}")
            for ((key, version) in catalog.versions) {
                terminal.println("  • $key = $version")
            }
        }

        if (catalog.libraries.isNotEmpty()) {
            terminal.println("\n${cyan("Libraries (${catalog.libraries.size}):")}")
            for (lib in catalog.libraries) {
                val moduleOrCoord = lib.module ?: "${lib.group}:${lib.name}"
                val verStr = lib.version?.let { ":$it" } ?: ""
                terminal.println("  • ${bold(lib.alias)} -> $moduleOrCoord$verStr")
            }
        }

        if (catalog.plugins.isNotEmpty()) {
            terminal.println("\n${cyan("Plugins (${catalog.plugins.size}):")}")
            for (plugin in catalog.plugins) {
                val verStr = plugin.version?.let { " (v$it)" } ?: ""
                terminal.println("  • ${bold(plugin.alias)} -> ${plugin.id}$verStr")
            }
        }
    }
}
