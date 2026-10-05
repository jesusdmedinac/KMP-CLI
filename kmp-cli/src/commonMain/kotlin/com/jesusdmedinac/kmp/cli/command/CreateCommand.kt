package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.danger
import com.jesusdmedinac.kmp.core.scaffold.ScaffoldingEngine
import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Command to scaffold new Kotlin Multiplatform projects with modern templates and architectures.
 */
class CreateCommand(
    private val engine: ScaffoldingEngine = ScaffoldingEngine(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "create",
) {
    val nameArgument by argument(
        "project-name",
        help = "Name of the project to create. Can also be specified via --name.",
    ).optional()

    val nameOption by option(
        "--name",
        help = "Name of the project to create.",
    )

    val templateOption by option(
        "--template",
        help = "Project template: ${ProjectTemplate.entries.joinToString(", ") { it.id }}",
    ).default("compose-multiplatform")

    val packageOption by option(
        "--package",
        help = "Base application package (e.g. com.example.app).",
    )

    val targetsOption by option(
        "--targets",
        help = "Comma-separated target platforms (e.g. android,ios,desktop,wasm).",
    ).default("android,ios,desktop,wasm")

    val formatOption by option(
        "--format",
        help = "Build format: gradle or toolchain.",
    ).default("gradle")

    val outputOption by option(
        "--output",
        "-o",
        help = "Output destination directory. Defaults to project name.",
    )

    val jsonOutput by option(
        "--json",
        help = "Output result as structured JSON for AI agents and automation.",
    ).flag(default = false)

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun run() {
        val projectName = nameOption ?: nameArgument

        if (projectName.isNullOrBlank()) {
            if (jsonOutput) {
                terminal.println("""{"error":"Project name is required. Use --name <name> or specify as an argument."}""")
            } else {
                terminal.danger("Project name is required. Use --name <name> or specify as an argument.")
            }
            throw ProgramResult(1)
        }

        val template = if (formatOption.equals("toolchain", ignoreCase = true) && templateOption == "compose-multiplatform") {
            ProjectTemplate.TOOLCHAIN_APP
        } else {
            ProjectTemplate.fromId(templateOption)
        }

        if (template == null) {
            val valid = ProjectTemplate.entries.joinToString(", ") { it.id }
            if (jsonOutput) {
                terminal.println("""{"error":"Unknown template '$templateOption'. Valid templates are: $valid"}""")
            } else {
                terminal.danger("Unknown template '$templateOption'. Available templates: $valid")
            }
            throw ProgramResult(1)
        }

        val packageName = packageOption ?: "com.example.${projectName.lowercase().replace("-", "").replace("_", "")}"
        val targetList = targetsOption.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        val outputDirectory = outputOption ?: projectName

        val options = ScaffoldingOptions(
            name = projectName,
            packageName = packageName,
            template = template,
            targets = targetList,
            outputDir = outputDirectory,
        )

        val result = engine.scaffold(options)

        if (result.status == "FAILED") {
            if (jsonOutput) {
                terminal.println(json.encodeToString(result))
            } else {
                terminal.danger("Scaffolding failed: ${result.errorMessage}")
            }
            throw ProgramResult(1)
        }

        if (jsonOutput) {
            terminal.println(json.encodeToString(result))
        } else {
            renderSuccessSummary(result, options, template)
        }
    }

    private fun renderSuccessSummary(
        result: ScaffoldingResult,
        options: ScaffoldingOptions,
        template: ProjectTemplate,
    ) {
        terminal.println(bold(green("✔ Project created successfully!")))
        terminal.println("${cyan("  • Project Name:")} ${bold(options.name)}")
        terminal.println("${cyan("  • Template:")}     ${template.displayName} (${dim(template.id)})")
        terminal.println("${cyan("  • Package:")}      ${options.packageName}")
        terminal.println("${cyan("  • Targets:")}      ${options.targets.joinToString(", ")}")
        terminal.println("${cyan("  • Directory:")}    ${result.projectPath}")
        terminal.println("${cyan("  • Files:")}        ${result.createdFiles.size} generated files")

        terminal.println(bold("\nNext steps to get started:"))
        terminal.println("  1. ${cyan("cd")} ${result.projectPath}")
        terminal.println("  2. ${cyan("kmp describe")}")
        terminal.println("  3. ${cyan("./gradlew check")}")
    }
}
