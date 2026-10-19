package com.jesusdmedinac.kmp.core.skill

import com.jesusdmedinac.kmp.core.skill.model.SkillCatalog
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry
import com.jesusdmedinac.kmp.core.skill.model.SkillManifest
import com.jesusdmedinac.kmp.core.skill.model.SkillMetadata
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
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
                    skill.aliases.any { it.lowercase().contains(q) } ||
                    skill.description.lowercase().contains(q) ||
                    skill.tags.any { it.lowercase().contains(q) } ||
                    skill.targets.any { it.lowercase().contains(q) } ||
                    (skill.category?.lowercase()?.contains(q) == true)
        }
    }

    fun getSkill(id: String): SkillCatalogEntry? {
        return getCatalog().skills.find {
            it.id.equals(id, ignoreCase = true) || it.aliases.any { alias -> alias.equals(id, ignoreCase = true) }
        }
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

    fun resolveTargetDirectory(explicitDir: String? = null, isGlobal: Boolean = false): String {
        if (explicitDir != null) return explicitDir
        if (isGlobal) {
            val home = systemEnvironment.getEnv("HOME") ?: "~"
            return "$home/.kmp/skills"
        }
        if (systemEnvironment.fileExists(".claude/skills")) {
            return ".claude/skills"
        }
        return ".agents/skills"
    }

    fun installSkill(
        skillId: String,
        targetDirectory: String? = null,
        isGlobal: Boolean = false,
        force: Boolean = false,
    ): InstallResult {
        val entry = getSkill(skillId) ?: return InstallResult.NotFound(skillId)
        val manifest = loadSkillManifest(skillId) ?: return InstallResult.NotFound(skillId)

        val baseDir = resolveTargetDirectory(targetDirectory, isGlobal)
        val destinationDir = "$baseDir/${entry.id}"

        val targetFile = "$destinationDir/SKILL.md"
        val exists = systemEnvironment.fileExists(targetFile)

        if (exists && !force) {
            return InstallResult.AlreadyExists(entry.id, targetFile)
        }

        val serialized = SkillParser.serialize(manifest)
        val written = systemEnvironment.writeFileText(targetFile, serialized)
        if (!written) {
            return InstallResult.Error(entry.id, "Failed to write skill file to $targetFile")
        }

        return InstallResult.Success(
            skillId = entry.id,
            installedPath = targetFile,
            isUpdate = exists,
        )
    }

    fun installRemoteSkill(
        coordinate: String,
        targetDirectory: String? = null,
        isGlobal: Boolean = false,
        force: Boolean = false,
        isUpdate: Boolean = false,
    ): RemoteInstallResult {
        val spec = RemoteSkillSpec.parse(coordinate)
            ?: return RemoteInstallResult.Error(coordinate, "Invalid repository coordinate: '$coordinate'")

        val baseTargetDir = resolveTargetDirectory(targetDirectory, isGlobal)
        val cloneTempDir = ".kmp-cache/remote/${spec.owner}-${spec.repo}"

        val hasExistingCache = isUpdate || systemEnvironment.fileExists("$cloneTempDir/.git")

        val cloneResult = if (hasExistingCache) {
            val pullCmd = listOf("git", "-C", cloneTempDir, "pull")
            val pullRes = systemEnvironment.execute(pullCmd)
            if (pullRes.exitCode == 0) {
                pullRes
            } else {
                val cloneCmd = listOf("git", "clone", "--depth", "1", "https://github.com/${spec.owner}/${spec.repo}.git", cloneTempDir)
                val cloneRes = systemEnvironment.execute(cloneCmd)
                if (cloneRes.exitCode != 0) {
                    systemEnvironment.execute(listOf("rm", "-rf", cloneTempDir))
                    systemEnvironment.execute(cloneCmd)
                } else {
                    cloneRes
                }
            }
        } else {
            val cloneCmd = listOf("git", "clone", "--depth", "1", "https://github.com/${spec.owner}/${spec.repo}.git", cloneTempDir)
            systemEnvironment.execute(cloneCmd)
        }

        if (cloneResult.exitCode != 0 && !systemEnvironment.fileExists("$cloneTempDir/SKILL.md") && !systemEnvironment.fileExists("$cloneTempDir/skills")) {
            val errorMsg = cloneResult.stderr.ifBlank { cloneResult.stdout }.ifBlank { "git clone exited with code ${cloneResult.exitCode}" }
            return RemoteInstallResult.Error(coordinate, errorMsg)
        }

        val candidateFiles = systemEnvironment.listFilesRecursively(cloneTempDir)
            .filter { it.endsWith("SKILL.md") }

        if (candidateFiles.isEmpty()) {
            return RemoteInstallResult.Error(coordinate, "No SKILL.md files found in repository '${spec.owner}/${spec.repo}'")
        }

        val matchingFiles = if (spec.subSkill != null) {
            candidateFiles.filter { relPath ->
                relPath.contains("/${spec.subSkill}/") ||
                        relPath.startsWith("${spec.subSkill}/") ||
                        relPath == "${spec.subSkill}/SKILL.md" ||
                        relPath == "skills/${spec.subSkill}/SKILL.md"
            }
        } else {
            candidateFiles
        }

        if (matchingFiles.isEmpty()) {
            return RemoteInstallResult.Error(coordinate, "Skill '${spec.subSkill}' not found in repository '${spec.owner}/${spec.repo}'")
        }

        val installedSkillNames = mutableListOf<String>()

        for (relPath in matchingFiles) {
            val fullSourcePath = "$cloneTempDir/$relPath"
            val content = systemEnvironment.readFileText(fullSourcePath) ?: continue

            val manifest = try {
                SkillParser.parse(content)
            } catch (e: Exception) {
                null
            }

            val skillFolder = manifest?.metadata?.name
                ?: relPath.split("/").dropLast(1).lastOrNull()?.ifBlank { null }
                ?: spec.repo

            val destFile = "$baseTargetDir/$skillFolder/SKILL.md"
            systemEnvironment.writeFileText(destFile, content)
            installedSkillNames.add(skillFolder)
        }

        recordRemoteInstalledSource(baseTargetDir, coordinate)

        return RemoteInstallResult.Success(
            source = coordinate,
            installedSkills = installedSkillNames,
            targetDirectory = baseTargetDir,
        )
    }

    private fun recordRemoteInstalledSource(baseTargetDir: String, coordinate: String) {
        val registryFile = "$baseTargetDir/.kmp-registry.json"
        val existing = systemEnvironment.readFileText(registryFile)
        val sources = mutableSetOf<String>()
        if (!existing.isNullOrBlank()) {
            try {
                sources.addAll(json.decodeFromString<List<String>>(existing))
            } catch (_: Exception) {}
        }
        sources.add(coordinate)
        systemEnvironment.writeFileText(registryFile, json.encodeToString(sources.toList()))
    }

    fun updateRemoteSkills(
        targetDirectory: String? = null,
        isGlobal: Boolean = false,
    ): List<RemoteInstallResult> {
        val baseTargetDir = resolveTargetDirectory(targetDirectory, isGlobal)
        val registryFile = "$baseTargetDir/.kmp-registry.json"
        val content = systemEnvironment.readFileText(registryFile) ?: return emptyList()

        val sources = try {
            json.decodeFromString<List<String>>(content)
        } catch (_: Exception) {
            return emptyList()
        }

        return sources.map { coord ->
            installRemoteSkill(
                coordinate = coord,
                targetDirectory = baseTargetDir,
                isGlobal = isGlobal,
                force = true,
                isUpdate = true,
            )
        }
    }
}

@Serializable
sealed interface RemoteInstallResult {
    @Serializable
    data class Success(
        val source: String,
        val installedSkills: List<String>,
        val targetDirectory: String,
    ) : RemoteInstallResult

    @Serializable
    data class Error(val source: String, val message: String) : RemoteInstallResult
}

data class RemoteSkillSpec(
    val owner: String,
    val repo: String,
    val subSkill: String? = null,
) {
    companion object {
        fun parse(coordinate: String): RemoteSkillSpec? {
            val clean = coordinate
                .removePrefix("https://github.com/")
                .removePrefix("http://github.com/")
                .removeSuffix(".git")
                .trim()

            val colonParts = clean.split(":")
            val repoPath = colonParts[0]
            val subSkill = colonParts.getOrNull(1)?.removePrefix("skills/")?.trim()

            val slashParts = repoPath.split("/")
            if (slashParts.size != 2 || slashParts[0].isBlank() || slashParts[1].isBlank()) {
                return null
            }

            return RemoteSkillSpec(
                owner = slashParts[0],
                repo = slashParts[1],
                subSkill = subSkill?.ifBlank { null }
            )
        }
    }
}
