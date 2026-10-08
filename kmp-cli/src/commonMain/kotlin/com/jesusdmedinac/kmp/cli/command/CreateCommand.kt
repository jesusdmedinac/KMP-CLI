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
    private val readInput: (prompt: String) -> String? = { prompt ->
        terminal.print(prompt)
        readlnOrNull()?.trim()
    },
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
    )

    val wizardOption by option(
        "--wizard",
        help = "Launch interactive project creation wizard.",
    ).flag(default = false)

    val packageOption by option(
        "--package",
        help = "Base application package (e.g. com.example.app).",
    )

    val targetsOption by option(
        "--targets",
        help = "Comma-separated target platforms (e.g. android,ios,desktop,wasm,server).",
    ).default("android,ios,desktop,wasm")

    val formatOption by option(
        "--format",
        help = "Build format: gradle or toolchain.",
    ).default("gradle")

    val buildSystemOption by option(
        "--build-system",
        help = "Build system alias: gradle or toolchain.",
    )

    val iosUiOption by option(
        "--ios-ui",
        help = "iOS UI framework: compose or swiftui.",
    )

    val webUiOption by option(
        "--web-ui",
        help = "Web UI framework: compose or react.",
    )

    val remoteOption by option(
        "--remote",
        help = "Fetch latest upstream template from GitHub with local fallback.",
    ).flag(default = false)

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
        var projectName = nameOption ?: nameArgument
        var buildSystem = buildSystemOption ?: formatOption
        var iosUi = iosUiOption
        var webUi = webUiOption
        var packageName = packageOption
        var targets = targetsOption

        if (wizardOption && !jsonOutput) {
            terminal.println(bold(cyan("\n✨ Kotlin Multiplatform Project Wizard ✨\n")))

            if (projectName.isNullOrBlank()) {
                val inputName = readInput("Project Name [KotlinProject]: ")
                projectName = if (!inputName.isNullOrBlank()) inputName else "KotlinProject"
            }

            if (packageName.isNullOrBlank()) {
                val defaultPkg = "com.example.${projectName.lowercase().replace("-", "").replace("_", "")}"
                val inputPkg = readInput("Project ID / Package [$defaultPkg]: ")
                packageName = if (!inputPkg.isNullOrBlank()) inputPkg else defaultPkg
            }

            terminal.println("\nBuild System:")
            terminal.println("  1) Gradle (Flexible and backed by a mature plugin ecosystem) [Default]")
            terminal.println("  2) Kotlin Toolchain (Easy to use, declarative, and AI-friendly)")
            val bsInput = readInput("Select build system [1]: ")
            buildSystem = if (bsInput == "2" || bsInput?.lowercase() == "toolchain") "toolchain" else "gradle"

            val isToolchainChoice = buildSystem.equals("toolchain", ignoreCase = true)

            if (!isToolchainChoice) {
                terminal.println("\niOS UI framework:")
                terminal.println("  1) Share UI via Compose Multiplatform [Default]")
                terminal.println("  2) Do not share UI - Use SwiftUI")
                val iosInput = readInput("Select iOS UI [1]: ")
                iosUi = if (iosInput == "2" || iosInput?.lowercase() in listOf("swiftui", "native")) "swiftui" else "compose"

                terminal.println("\nWeb UI framework:")
                terminal.println("  1) Share UI via Compose Multiplatform (Wasm) [Default]")
                terminal.println("  2) Do not share UI - Use React with TypeScript")
                val webInput = readInput("Select Web UI [1]: ")
                webUi = if (webInput == "2" || webInput?.lowercase() in listOf("react", "vite")) "react" else "compose"
            } else {
                iosUi = "compose"
                webUi = "compose"
            }

            val includeDesktop = readInput("\nInclude Desktop target? (Y/n) [Y]: ")
            val hasDesktop = includeDesktop?.lowercase() != "n"

            val includeServer = readInput("Include Ktor Server backend? (y/N) [N]: ")
            val hasServer = includeServer?.lowercase() == "y"

            val targetList = mutableListOf("android", "ios")
            if (hasDesktop) targetList.add("desktop")
            targetList.add("web")
            if (hasServer) targetList.add("server")
            targets = targetList.joinToString(",")
        }

        if (projectName.isNullOrBlank()) {
            if (jsonOutput) {
                terminal.println("""{"error":"Project name is required. Use --name <name> or specify as an argument."}""")
            } else {
                terminal.danger("Project name is required. Use --name <name>, specify as an argument, or use --wizard.")
            }
            throw ProgramResult(1)
        }

        val isToolchain = buildSystem.equals("toolchain", ignoreCase = true)
        val isNativeIos = iosUi?.lowercase() in listOf("swiftui", "native")
        val isReactWeb = webUi?.lowercase() in listOf("react", "vite")
        val hasServerTarget = targets.split(',').any { it.trim().equals("server", ignoreCase = true) }

        val template = if (templateOption != null) {
            when {
                isToolchain && isNativeIos -> ProjectTemplate.TOOLCHAIN_NATIVE_UI
                isToolchain && (templateOption in listOf("shared-ui", "compose-app", "compose-multiplatform")) -> ProjectTemplate.TOOLCHAIN_SHARED_UI
                !isToolchain && isNativeIos && (templateOption in listOf("shared-ui", "compose-app", "compose-multiplatform")) -> ProjectTemplate.NATIVE_UI
                else -> ProjectTemplate.fromId(templateOption!!)
            }
        } else {
            when {
                isToolchain && isNativeIos -> ProjectTemplate.TOOLCHAIN_NATIVE_UI
                isToolchain -> ProjectTemplate.TOOLCHAIN_SHARED_UI
                isNativeIos || isReactWeb -> ProjectTemplate.NATIVE_UI
                hasServerTarget -> ProjectTemplate.FULLSTACK
                else -> ProjectTemplate.SHARED_UI
            }
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

        val resolvedPackage = packageName ?: "com.example.${projectName.lowercase().replace("-", "").replace("_", "")}"
        val targetList = targets.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        val outputDirectory = outputOption ?: projectName

        val options = ScaffoldingOptions(
            name = projectName,
            packageName = resolvedPackage,
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
            renderSuccessSummary(result, options, template, isToolchain)
        }
    }

    private fun renderSuccessSummary(
        result: ScaffoldingResult,
        options: ScaffoldingOptions,
        template: ProjectTemplate,
        isToolchain: Boolean,
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
        if (isToolchain) {
            terminal.println("  3. ${cyan("./kotlin run")}")
            terminal.println(dim("\n💡 Tip: Ensure execution permissions with: chmod +x kotlin"))
        } else {
            terminal.println("  3. ${cyan("./gradlew check")}")
            terminal.println(dim("\n💡 Tip: Ensure execution permissions with: chmod +x gradlew"))
        }
    }
}
