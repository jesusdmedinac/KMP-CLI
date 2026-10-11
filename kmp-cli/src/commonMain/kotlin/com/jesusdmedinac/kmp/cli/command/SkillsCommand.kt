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
            SkillsUpdateCommand(repository, terminal),
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

    private val json = Json { prettyPrint = true }

    override fun run() {
        if (skillId.contains("/")) {
            val remoteResult = repository.installRemoteSkill(
                coordinate = skillId,
                isGlobal = isGlobal,
                force = force,
            )

            when (remoteResult) {
                is com.jesusdmedinac.kmp.core.skill.RemoteInstallResult.Success -> {
                    if (jsonOutput) {
                        terminal.println(json.encodeToString(remoteResult))
                        return
                    }
                    terminal.println(green("✓ Installed ${remoteResult.installedSkills.size} skills from '${remoteResult.source}' into ${remoteResult.targetDirectory}"))
                    for (name in remoteResult.installedSkills) {
                        terminal.println("  • $name")
                    }
                }
                is com.jesusdmedinac.kmp.core.skill.RemoteInstallResult.Error -> {
                    terminal.println(red("✗ Error installing remote skill from '${remoteResult.source}': ${remoteResult.message}"))
                    throw ProgramResult(1)
                }
            }
            return
        }

        val result = repository.installSkill(
            skillId = skillId,
            isGlobal = isGlobal,
            force = force,
        )

        when (result) {
            is InstallResult.Success -> {
                val action = if (result.isUpdate) "Updated" else "Installed"
                val aliasNote = if (!skillId.equals(result.skillId, ignoreCase = true)) " (resolved from '$skillId')" else ""
                terminal.println(green("✓ $action skill '${result.skillId}'$aliasNote into ${result.installedPath}"))
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

class SkillsUpdateCommand(
    private val repository: SkillsRepository = SkillsRepository(),
    private val terminal: Terminal = Terminal(),
) : CliktCommand(
    name = "update",
) {
    val isGlobal by option(
        "--global",
        "-g",
        help = "Update global skills (~/.kmp/skills/)",
    ).flag(default = false)

    override fun run() {
        val results = repository.updateRemoteSkills(isGlobal = isGlobal)
        if (results.isEmpty()) {
            terminal.println(yellow("No remote skills registered for update."))
            return
        }

        terminal.println(bold("Updating remote skills:\n"))
        for (result in results) {
            when (result) {
                is com.jesusdmedinac.kmp.core.skill.RemoteInstallResult.Success -> {
                    terminal.println(green("✓ Updated ${result.installedSkills.size} skills from '${result.source}'"))
                }
                is com.jesusdmedinac.kmp.core.skill.RemoteInstallResult.Error -> {
                    terminal.println(red("✗ Failed to update '${result.source}': ${result.message}"))
                }
            }
        }
    }
}

private fun renderSkillSummary(skill: SkillCatalogEntry, terminal: Terminal) {
    val categoryBadge = skill.category?.let { yellow("[$it] ") } ?: ""
    terminal.println("  $categoryBadge${bold(skill.name)} ${dim("(v${skill.version})")}")
    terminal.println("  ${skill.description}")
    val tags = if (skill.tags.isNotEmpty()) cyan("Tags: ") + skill.tags.joinToString(", ") else ""
    val targets = if (skill.targets.isNotEmpty()) magenta("Targets: ") + skill.targets.joinToString(", ") else ""
    val aliases = if (skill.aliases.isNotEmpty()) dim("Aliases: ") + skill.aliases.joinToString(", ") else ""
    val meta = listOf(tags, targets, aliases).filter { it.isNotBlank() }.joinToString("  |  ")
    if (meta.isNotBlank()) {
        terminal.println("  $meta")
    }
    terminal.println()
}
