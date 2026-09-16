package com.jesusdmedinac.kmp.core.skill

import com.jesusdmedinac.kmp.core.skill.model.SkillCatalog
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry
import com.jesusdmedinac.kmp.core.skill.model.SkillManifest
import com.jesusdmedinac.kmp.core.skill.model.SkillMetadata
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment
import kotlinx.serialization.json.Json

sealed interface InstallResult {
    data class Success(val skillId: String, val installedPath: String, val isUpdate: Boolean) : InstallResult
    data class AlreadyExists(val skillId: String, val path: String) : InstallResult
    data class NotFound(val skillId: String) : InstallResult
    data class Error(val skillId: String, val message: String) : InstallResult
}

class SkillsRepository(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
    private val fallbackCatalog: SkillCatalog = DefaultSkillsCatalog.catalog,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getCatalog(): SkillCatalog {
        if (systemEnvironment.fileExists("skills/catalog.json")) {
            val content = systemEnvironment.readFileText("skills/catalog.json")
            if (!content.isNullOrBlank()) {
                return try {
                    json.decodeFromString<SkillCatalog>(content)
                } catch (e: Exception) {
                    fallbackCatalog
                }
            }
        }
        return fallbackCatalog
    }

    fun findSkills(query: String): List<SkillCatalogEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return getCatalog().skills

        return getCatalog().skills.filter { skill ->
            skill.id.lowercase().contains(q) ||
                    skill.name.lowercase().contains(q) ||
                    skill.description.lowercase().contains(q) ||
                    skill.tags.any { it.lowercase().contains(q) } ||
                    skill.targets.any { it.lowercase().contains(q) }
        }
    }

    fun getSkill(id: String): SkillCatalogEntry? {
        return getCatalog().skills.find { it.id.equals(id, ignoreCase = true) }
    }

    fun loadSkillManifest(id: String): SkillManifest? {
        val entry = getSkill(id) ?: return null
        entry.path?.let { p ->
            if (systemEnvironment.fileExists(p)) {
                val text = systemEnvironment.readFileText(p)
                if (text != null) {
                    return try {
                        SkillParser.parse(text)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

        return SkillManifest(
            metadata = SkillMetadata(
                name = entry.name,
                description = entry.description,
                version = entry.version,
                author = entry.author,
                tags = entry.tags,
                targets = entry.targets,
            ),
            content = "# ${entry.name}\n\n${entry.description}\n"
        )
    }

    fun installSkill(
        skillId: String,
        targetDirectory: String? = null,
        isGlobal: Boolean = false,
        force: Boolean = false,
    ): InstallResult {
        val entry = getSkill(skillId) ?: return InstallResult.NotFound(skillId)
        val manifest = loadSkillManifest(skillId) ?: return InstallResult.NotFound(skillId)

        val destinationDir = when {
            targetDirectory != null -> targetDirectory
            isGlobal -> {
                val home = systemEnvironment.getEnv("HOME") ?: "~"
                "$home/.kmp/skills/${entry.id}"
            }
            else -> ".agents/skills/${entry.id}"
        }

        val targetFile = "$destinationDir/SKILL.md"
        val exists = systemEnvironment.fileExists(targetFile)

        if (exists && !force) {
            return InstallResult.AlreadyExists(skillId, targetFile)
        }

        val serialized = SkillParser.serialize(manifest)
        val written = systemEnvironment.writeFileText(targetFile, serialized)
        if (!written) {
            return InstallResult.Error(skillId, "Failed to write skill file to $targetFile")
        }

        return InstallResult.Success(
            skillId = skillId,
            installedPath = targetFile,
            isUpdate = exists,
        )
    }
}
