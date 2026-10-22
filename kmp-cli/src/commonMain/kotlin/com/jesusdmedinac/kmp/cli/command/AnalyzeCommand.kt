package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.danger
import com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker
import com.jesusdmedinac.kmp.core.architecture.model.ArchitectureReport
import com.jesusdmedinac.kmp.core.architecture.model.ViolationSeverity
import com.jesusdmedinac.kmp.core.migration.MigrationEngine
import com.jesusdmedinac.kmp.core.migration.model.LegacyDetectionResult
import com.jesusdmedinac.kmp.core.migration.model.LegacyFramework
import com.jesusdmedinac.kmp.core.project.model.ProjectDependencies
import com.jesusdmedinac.kmp.core.project.parser.VersionCatalogParser
import com.github.ajalt.mordant.rendering.TextColors
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Command group for performing static inspection and dependency analysis on Kotlin Multiplatform projects.
 */
class AnalyzeCommand(
    versionCatalogParser: VersionCatalogParser = VersionCatalogParser(),
    private val architectureChecker: ArchitectureRuleChecker = ArchitectureRuleChecker(),
    klibsCatalog: com.jesusdmedinac.kmp.core.klibs.KlibsCatalog = com.jesusdmedinac.kmp.core.klibs.KlibsCatalog(),
    projectParser: com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser = com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser(),
    private val migrationEngine: MigrationEngine = MigrationEngine(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "analyze",
) {
    override val invokeWithoutSubcommand get() = true

    val rules by option(
        "--rules",
        "-r",
        help = "Audit architectural rules and module.yaml dependencies.",
    ).flag(default = false)

    val jsonOutput by option(
        "--json",
        help = "Output analysis as structured JSON",
    ).flag(default = false)

    private val json = Json { prettyPrint = true }

    init {
        subcommands(
            AnalyzeDependenciesCommand(versionCatalogParser, migrationEngine, terminal),
            AnalyzeRulesCommand(architectureChecker, terminal),
            AnalyzeLibrariesCommand(klibsCatalog, projectParser, terminal),
        )
    }

    override fun run() {
        if (currentContext.invokedSubcommand != null) return
        if (rules) {
            val report = architectureChecker.checkProject(".")
            if (jsonOutput) {
                terminal.println(json.encodeToString(report))
            } else {
                renderRulesReport(report, terminal)
            }
            if (!report.passed) {
                throw ProgramResult(1)
            }
        } else {
            val legacy = migrationEngine.detectLegacyFramework(".")
            if (legacy.detected) {
                if (jsonOutput) {
                    terminal.println(json.encodeToString(legacy))
                } else {
                    val frameworkName = when (legacy.framework) {
                        LegacyFramework.REACT_NATIVE -> "React Native"
                        LegacyFramework.FLUTTER -> "Flutter"
                        LegacyFramework.UNKNOWN -> "Unknown"
                    }
                    terminal.println(TextColors.yellow(bold("💡 Legacy cross-platform codebase detected!")))
                    terminal.println("Detected: ${cyan(frameworkName)} via '${legacy.signatureFile}'.")
                    terminal.println("Recommendation: Run '${green("kmp migrate audit")}' to analyze native bridge dependencies and modernization pathways.")
                }
            } else {
                terminal.println("Run 'kmp analyze dependencies', 'kmp analyze libraries', or 'kmp analyze --rules'.")
            }
        }
    }
}

class AnalyzeRulesCommand(
    private val architectureChecker: ArchitectureRuleChecker = ArchitectureRuleChecker(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "rules",
) {
    val jsonOutput by option(
        "--json",
        help = "Output rules analysis as structured JSON",
    ).flag(default = false)

    val projectPath by argument(
        "path",
        help = "Path to project root. Defaults to current directory.",
    ).default(".")

    private val json = Json { prettyPrint = true }

    override fun run() {
        val report = architectureChecker.checkProject(projectPath)
        if (jsonOutput) {
            terminal.println(json.encodeToString(report))
        } else {
            renderRulesReport(report, terminal)
        }
        if (!report.passed) {
            throw ProgramResult(1)
        }
    }
}

private fun renderRulesReport(report: ArchitectureReport, terminal: Terminal) {
    terminal.println(bold("KMP Architectural & Dependency Rules Audit"))
    terminal.println(dim("Scanned ${report.scannedFilesCount} files for architectural drift and module.yaml alignment.\n"))

    if (report.violations.isEmpty()) {
        terminal.println(TextColors.green("  ✓ All architectural rules and dependencies are verified!"))
        return
    }

    for (violation in report.violations) {
        val icon = when (violation.severity) {
            ViolationSeverity.ERROR -> TextColors.red("✗")
            ViolationSeverity.WARNING -> TextColors.yellow("!")
            ViolationSeverity.INFO -> TextColors.cyan("ℹ")
        }
        terminal.println("  [$icon] ${bold(violation.ruleId)} at ${violation.filePath}:${violation.lineNumber}")
        terminal.println("      ${violation.message}")
        terminal.println(TextColors.cyan("      ↳ Fix: ${violation.remediation}"))
        terminal.println()
    }

    terminal.println(bold("Conclusion:"))
    if (report.passed) {
        terminal.println(TextColors.yellow("  ! Architectural audit passed with warnings."))
    } else {
        terminal.println(TextColors.red("  ✗ Architectural violations detected. Discrepancies must be resolved."))
    }
}

/**
 * Subcommand to inspect and enumerate declared versions, libraries, and plugins from the version catalog.
 */
class AnalyzeDependenciesCommand(
    private val versionCatalogParser: VersionCatalogParser = VersionCatalogParser(),
    private val migrationEngine: MigrationEngine = MigrationEngine(),
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
            val legacy = migrationEngine.detectLegacyFramework(projectPath)
            if (legacy.detected) {
                if (jsonOutput) {
                    terminal.println(json.encodeToString(legacy))
                } else {
                    val frameworkName = when (legacy.framework) {
                        LegacyFramework.REACT_NATIVE -> "React Native"
                        LegacyFramework.FLUTTER -> "Flutter"
                        LegacyFramework.UNKNOWN -> "Unknown"
                    }
                    terminal.println(TextColors.yellow(bold("💡 Legacy cross-platform codebase detected!")))
                    terminal.println("Detected: ${cyan(frameworkName)} via '${legacy.signatureFile}'.")
                    terminal.println("Recommendation: Run '${green("kmp migrate audit")}' to analyze native bridge dependencies and modernization pathways.")
                }
            } else {
                if (jsonOutput) {
                    terminal.println("""{"error":"No version catalog found at '$catalogPath'"}""")
                } else {
                    terminal.danger("No version catalog found at '$catalogPath'. Ensure gradle/libs.versions.toml exists.")
                }
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
