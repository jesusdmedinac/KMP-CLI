package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import com.jesusdmedinac.kmp.core.skill.InstallResult
import com.jesusdmedinac.kmp.core.skill.SkillsRepository
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SkillsCommand(
    private val repository: SkillsRepository = SkillsRepository(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "skills",
) {
    init {
        subcommands(
            SkillsListCommand(repository, terminal),
            SkillsFindCommand(repository, terminal),
            SkillsDescribeCommand(repository, terminal),
            SkillsAddCommand(repository, terminal),
        )
    }

    override fun run() = Unit
}

class SkillsListCommand(
    private val repository: SkillsRepository = SkillsRepository(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "list",
) {
    private val jsonOutput by option(
        "--json",
        help = "Output skills catalog as structured JSON",
    ).flag(default = false)

    private val json = Json { prettyPrint = true }

    override fun run() {
        val catalog = repository.getCatalog()

        if (jsonOutput) {
            terminal.println(json.encodeToString(catalog.skills))
            return
        }

        terminal.println(bold("KMP Skills Catalog (${catalog.skills.size} available)\n"))
        for (skill in catalog.skills) {
            renderSkillSummary(skill, terminal)
        }
    }
}

class SkillsFindCommand(
    private val repository: SkillsRepository = SkillsRepository(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "find",
) {
    val query by argument("query", help = "Keyword to search in skills")
    private val jsonOutput by option(
        "--json",
        help = "Output search results as JSON",
    ).flag(default = false)

    private val json = Json { prettyPrint = true }

    override fun run() {
        val matches = repository.findSkills(query)

        if (jsonOutput) {
            terminal.println(json.encodeToString(matches))
            return
        }

        if (matches.isEmpty()) {
            terminal.println(yellow("No skills found matching '$query'."))
            return
        }

        terminal.println(bold("Found ${matches.size} skills matching '$query':\n"))
        for (skill in matches) {
            renderSkillSummary(skill, terminal)
        }
    }
}

class SkillsDescribeCommand(
    private val repository: SkillsRepository = SkillsRepository(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "describe",
) {
    val skillId by argument("skill-id", help = "Skill identifier to describe")
    private val jsonOutput by option(
        "--json",
        help = "Output skill manifest as JSON",
    ).flag(default = false)

    private val json = Json { prettyPrint = true }

    override fun run() {
        val manifest = repository.loadSkillManifest(skillId)
        if (manifest == null) {
            terminal.println(red("Error: Skill '$skillId' not found in catalog."))
            throw ProgramResult(1)
        }

        if (jsonOutput) {
            terminal.println(json.encodeToString(manifest))
            return
        }

        val meta = manifest.metadata
        terminal.println(bold("${meta.name} (v${meta.version})"))
        terminal.println(meta.description)
        terminal.println()
        if (meta.tags.isNotEmpty()) {
            terminal.println(cyan("Tags: ") + meta.tags.joinToString(", "))
        }
        if (meta.targets.isNotEmpty()) {
            terminal.println(magenta("Targets: ") + meta.targets.joinToString(", "))
        }
        meta.author?.let { terminal.println(dim("Author: $it")) }
        terminal.println()
        terminal.println(manifest.content)
    }
}

class SkillsAddCommand(
    private val repository: SkillsRepository = SkillsRepository(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "add",
) {
    val skillId by argument("skill-id", help = "Skill identifier to install")
    val isGlobal by option(
        "--global",
        "-g",
        help = "Install globally in user home directory (~/.kmp/skills/)",
    ).flag(default = false)
    val force by option(
        "--force",
        "-f",
        help = "Force overwrite if skill is already installed",
    ).flag(default = false)
    private val jsonOutput by option(
        "--json",
        help = "Output installation result as JSON",
    ).flag(default = false)

    override fun run() {
        val result = repository.installSkill(
            skillId = skillId,
            isGlobal = isGlobal,
            force = force,
        )

        when (result) {
            is InstallResult.Success -> {
                val action = if (result.isUpdate) "Updated" else "Installed"
                terminal.println(green("✓ $action skill '${result.skillId}' into ${result.installedPath}"))
            }
            is InstallResult.AlreadyExists -> {
                terminal.println(yellow("! Skill '${result.skillId}' is already installed at ${result.path}. Use --force to overwrite."))
            }
            is InstallResult.NotFound -> {
                terminal.println(red("✗ Skill '${result.skillId}' was not found in the catalog."))
                throw ProgramResult(1)
            }
            is InstallResult.Error -> {
                terminal.println(red("✗ Error installing skill: ${result.message}"))
                throw ProgramResult(1)
            }
        }
    }
}

private fun renderSkillSummary(skill: SkillCatalogEntry, terminal: Terminal) {
    terminal.println("  ${bold(skill.name)} ${dim("(v${skill.version})")}")
    terminal.println("  ${skill.description}")
    val tags = if (skill.tags.isNotEmpty()) cyan("Tags: ") + skill.tags.joinToString(", ") else ""
    val targets = if (skill.targets.isNotEmpty()) magenta("Targets: ") + skill.targets.joinToString(", ") else ""
    val meta = listOf(tags, targets).filter { it.isNotBlank() }.joinToString("  |  ")
    if (meta.isNotBlank()) {
        terminal.println("  $meta")
    }
    terminal.println()
}
