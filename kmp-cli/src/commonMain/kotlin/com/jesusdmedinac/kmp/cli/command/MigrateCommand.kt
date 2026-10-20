package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextColors.magenta
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextColors.yellow
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.jesusdmedinac.kmp.core.migration.MigrationEngine
import com.jesusdmedinac.kmp.core.migration.model.ArchitectureType
import com.jesusdmedinac.kmp.core.migration.model.LegacyFramework
import com.jesusdmedinac.kmp.core.migration.model.MigrationAuditReport
import com.jesusdmedinac.kmp.core.migration.model.MigrationPlan
import com.jesusdmedinac.kmp.core.migration.model.ModernizationPathway
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Command group for cross-platform modernization and migration analysis (React Native, Flutter -> KMP).
 */
class MigrateCommand(
    private val migrationEngine: MigrationEngine = MigrationEngine(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "migrate",
) {
    override val invokeWithoutSubcommand get() = true

    init {
        subcommands(
            MigrateAuditCommand(migrationEngine, terminal),
            MigratePlanCommand(migrationEngine, terminal),
        )
    }

    override fun run() {
        if (currentContext.invokedSubcommand != null) return
        terminal.println("Run 'kmp migrate audit' to analyze legacy codebases or 'kmp migrate plan' to generate a phased roadmap.")
    }
}

class MigrateAuditCommand(
    private val migrationEngine: MigrationEngine = MigrationEngine(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "audit",
) {
    val jsonOutput by option(
        "--json",
        help = "Output modernization audit as structured JSON for AI agents.",
    ).flag(default = false)

    val projectPath by argument(
        "path",
        help = "Path to the legacy project root. Defaults to current directory.",
    ).default(".")

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() {
        val report = migrationEngine.audit(projectPath)

        if (jsonOutput) {
            terminal.println(json.encodeToString(report))
        } else {
            renderAuditTui(report)
        }
    }

    private fun renderAuditTui(report: MigrationAuditReport) {
        val frameworkName = when (report.framework) {
            LegacyFramework.REACT_NATIVE -> "React Native"
            LegacyFramework.FLUTTER -> "Flutter"
            LegacyFramework.UNKNOWN -> "Unknown Mobile"
        }

        val archName = when (report.architecture) {
            ArchitectureType.OLD_BRIDGE -> yellow("Old Architecture (JS Bridge)")
            ArchitectureType.NEW_ARCHITECTURE_TURBOMODULES -> green("New Architecture (TurboModules)")
            ArchitectureType.FLUTTER_PLATFORM_CHANNELS -> yellow("Platform Channels (MethodChannel)")
            ArchitectureType.STANDARD -> green("Standard")
        }

        val scoreColor = if (report.compatibilityScore >= 80) green else yellow

        terminal.println(bold("\n🔍 Cross-Platform Modernization Audit: $frameworkName"))
        terminal.println("  • ${bold("Framework Version:")} ${report.frameworkVersion ?: "unknown"}")
        terminal.println("  • ${bold("Architecture:")} $archName")
        terminal.println("  • ${bold("Platform Targets:")} ${report.platformTargets.joinToString(", ")}")
        terminal.println("  • ${bold("Compatibility Score:")} ${scoreColor("${report.compatibilityScore}/100")}\n")

        if (report.dependencies.isNotEmpty()) {
            terminal.println(bold("Dependency Replacement Matrix (${report.dependencies.size}):"))
            val t = table {
                header {
                    row(bold("Legacy Package"), bold("Category"), bold("Recommended KMP Equivalent"), bold("Effort"))
                }
                body {
                    for (dep in report.dependencies) {
                        val effortStyled = when (dep.migrationEffort) {
                            "LOW" -> green("LOW")
                            "MEDIUM" -> yellow("MEDIUM")
                            else -> red("HIGH")
                        }
                        row(dep.legacyName, dep.category, cyan(dep.kmpEquivalent), effortStyled)
                    }
                }
            }
            terminal.println(t)
        }

        val pathwayTitle = when (report.recommendedPathway) {
            ModernizationPathway.DUAL_NATIVE -> "Dual Native (100% SwiftUI + 100% Jetpack Compose)"
            ModernizationPathway.NATIVE_UI_SHARED_CORE -> "Native UI + Shared KMP Core"
            ModernizationPathway.COMPOSE_MULTIPLATFORM -> "100% Compose Multiplatform"
        }

        terminal.println(bold("\nRecommended Modernization Pathway:"))
        terminal.println("  🚀 ${magenta(bold(pathwayTitle))}")
        terminal.println("  ${dim(report.pathwayRationale)}")

        if (report.riskFactors.isNotEmpty()) {
            terminal.println(bold("\nRisk Factors & Bottlenecks:"))
            for (risk in report.riskFactors) {
                terminal.println("  ⚠️ ${yellow(risk)}")
            }
        }

        terminal.println(dim("\nRun 'kmp migrate plan' to generate an incremental Strangler Fig roadmap."))
    }
}

class MigratePlanCommand(
    private val migrationEngine: MigrationEngine = MigrationEngine(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "plan",
) {
    val jsonOutput by option(
        "--json",
        help = "Output migration roadmap as structured JSON for AI agents.",
    ).flag(default = false)

    val pathwayOption by option(
        "--pathway",
        help = "Override recommended pathway: 'dual-native', 'native-ui-shared-core', or 'compose-multiplatform'",
    )

    val projectPath by argument(
        "path",
        help = "Path to the legacy project root. Defaults to current directory.",
    ).default(".")

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() {
        val override = pathwayOption?.let {
            val normalized = it.lowercase().replace('_', '-')
            when (normalized) {
                "dual-native" -> ModernizationPathway.DUAL_NATIVE
                "native-ui-shared-core" -> ModernizationPathway.NATIVE_UI_SHARED_CORE
                "compose-multiplatform" -> ModernizationPathway.COMPOSE_MULTIPLATFORM
                else -> null
            }
        }

        val plan = migrationEngine.plan(projectPath, override)

        if (jsonOutput) {
            terminal.println(json.encodeToString(plan))
        } else {
            renderPlanTui(plan)
        }
    }

    private fun renderPlanTui(plan: MigrationPlan) {
        val pathwayTitle = when (plan.pathway) {
            ModernizationPathway.DUAL_NATIVE -> "Dual Native (100% SwiftUI + 100% Jetpack Compose)"
            ModernizationPathway.NATIVE_UI_SHARED_CORE -> "Native UI + Shared KMP Core"
            ModernizationPathway.COMPOSE_MULTIPLATFORM -> "100% Compose Multiplatform"
        }

        terminal.println(bold("\n🗺️ Strangler Fig Modernization Roadmap"))
        terminal.println("Target Pathway: ${magenta(bold(pathwayTitle))}\n")

        for (phase in plan.phases) {
            terminal.println(bold("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"))
            terminal.println("${cyan(bold("Phase ${phase.phaseNumber}:"))} ${bold(phase.name)}")
            terminal.println("  ${bold("Objectives:")}")
            for (obj in phase.objectives) {
                terminal.println("    • $obj")
            }
            terminal.println("  ${bold("Deliverables:")}")
            for (del in phase.deliverables) {
                terminal.println("    ✓ ${green(del)}")
            }
            if (phase.recommendedSkills.isNotEmpty()) {
                terminal.println("  ${bold("Recommended KMP Skills:")} ${phase.recommendedSkills.joinToString(", ") { cyan(it) }}")
            }
            terminal.println()
        }
    }
}
