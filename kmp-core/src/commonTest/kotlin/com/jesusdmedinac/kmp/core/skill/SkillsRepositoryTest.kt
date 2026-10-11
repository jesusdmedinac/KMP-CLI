package com.jesusdmedinac.kmp.core.skill

import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SkillsRepositoryTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val repository = SkillsRepository(fakeEnv)

    @Test
    fun `getCatalog returns fallback catalog when no local catalog file exists`() {
        val catalog = repository.getCatalog()
        assertTrue(catalog.skills.size >= 6)
        assertNotNull(catalog.skills.find { it.id == "kotlin-ui-compose-adaptive" })
        assertNotNull(catalog.skills.find { it.id == "kotlin-data-room-kmp" })
    }

    @Test
    fun `findSkills filters by keyword across id name tags and targets`() {
        val composeSkills = repository.findSkills("compose")
        assertTrue(composeSkills.size >= 2)
        assertTrue(composeSkills.any { it.id == "kotlin-ui-compose-adaptive" })

        val networkSkills = repository.findSkills("networking")
        assertEquals(1, networkSkills.size)
        assertEquals("kotlin-networking-ktor", networkSkills.first().id)

        val unknownSkills = repository.findSkills("nonexistent-tool")
        assertTrue(unknownSkills.isEmpty())
    }

    @Test
    fun `getSkill returns entry case-insensitively and resolves legacy aliases`() {
        val skillByAlias = repository.getSkill("KMP-KTOR-NETWORKING")
        assertNotNull(skillByAlias)
        assertEquals("kotlin-networking-ktor", skillByAlias.id)

        val skillByCanonical = repository.getSkill("kotlin-networking-ktor")
        assertNotNull(skillByCanonical)
        assertEquals("kotlin-networking-ktor", skillByCanonical.id)

        assertNull(repository.getSkill("unknown-skill"))
    }

    @Test
    fun `getSkill resolves kotlin-kmp-router and legacy alias kmp-router`() {
        val canonical = repository.getSkill("kotlin-kmp-router")
        assertNotNull(canonical)
        assertEquals("kotlin-kmp-router", canonical.id)
        assertEquals("core", canonical.category)

        val byAlias = repository.getSkill("kmp-router")
        assertNotNull(byAlias)
        assertEquals("kotlin-kmp-router", byAlias.id)
    }

    @Test
    fun `installSkill installs to local agents directory by default using canonical name`() {
        val result = repository.installSkill("kotlin-ui-compose-adaptive")

        assertTrue(result is InstallResult.Success)
        assertEquals(".agents/skills/kotlin-ui-compose-adaptive/SKILL.md", result.installedPath)
        assertEquals(false, result.isUpdate)

        assertTrue(fakeEnv.fileExists(".agents/skills/kotlin-ui-compose-adaptive/SKILL.md"))
        val installedContent = fakeEnv.readFileText(".agents/skills/kotlin-ui-compose-adaptive/SKILL.md")
        assertNotNull(installedContent)
        assertTrue(installedContent.contains("name: kotlin-ui-compose-adaptive"))
    }

    @Test
    fun `installSkill resolves legacy alias to canonical skill directory`() {
        val result = repository.installSkill("kmp-toolchain-migration")

        assertTrue(result is InstallResult.Success)
        assertEquals(".agents/skills/kotlin-tooling-gradle-to-kotlin-toolchain/SKILL.md", result.installedPath)
        assertEquals("kotlin-tooling-gradle-to-kotlin-toolchain", result.skillId)
    }

    @Test
    fun `installSkill with isGlobal installs to home directory`() {
        fakeEnv.setEnv("HOME", "/Users/testdeveloper")

        val result = repository.installSkill("kotlin-data-sqldelight-database", isGlobal = true)

        assertTrue(result is InstallResult.Success)
        assertEquals("/Users/testdeveloper/.kmp/skills/kotlin-data-sqldelight-database/SKILL.md", result.installedPath)
        assertTrue(fakeEnv.fileExists("/Users/testdeveloper/.kmp/skills/kotlin-data-sqldelight-database/SKILL.md"))
    }

    @Test
    fun `installSkill returns AlreadyExists when skill is already installed and force is false`() {
        val firstResult = repository.installSkill("kotlin-ui-compose-adaptive")
        assertTrue(firstResult is InstallResult.Success)

        val secondResult = repository.installSkill("kmp-compose-adaptive", force = false)
        assertTrue(secondResult is InstallResult.AlreadyExists)
        assertEquals("kotlin-ui-compose-adaptive", secondResult.skillId)
    }

    @Test
    fun `installSkill overwrites and reports update when force is true`() {
        repository.installSkill("kotlin-ui-compose-adaptive")

        val updateResult = repository.installSkill("kotlin-ui-compose-adaptive", force = true)
        assertTrue(updateResult is InstallResult.Success)
        assertEquals(true, updateResult.isUpdate)
    }

    @Test
    fun `installSkill returns NotFound when skill is missing from catalog`() {
        val result = repository.installSkill("imaginary-missing-skill")
        assertTrue(result is InstallResult.NotFound)
    }

    @Test
    fun `resolveTargetDirectory auto-detects claude or agents skills folder`() {
        assertEquals(".agents/skills", repository.resolveTargetDirectory())

        fakeEnv.addFile(".claude/skills")
        assertEquals(".claude/skills", repository.resolveTargetDirectory())
    }

    @Test
    fun `installRemoteSkill clones repository and installs all discovered skills`() {
        val cloneDir = ".kmp-cache/remote/Kotlin-kotlin-agent-skills"
        fakeEnv.whenCommand(
            listOf("git", "clone", "--depth", "1", "https://github.com/Kotlin/kotlin-agent-skills.git", cloneDir),
            com.jesusdmedinac.kmp.core.system.ProcessResult(0, "Cloned", "")
        )
        fakeEnv.writeFileText(
            "$cloneDir/skills/kotlin-tooling-kotlin-toolchain/SKILL.md",
            "---\nname: kotlin-tooling-kotlin-toolchain\ndescription: Toolchain\n---\n# Toolchain Content"
        )

        val result = repository.installRemoteSkill("Kotlin/kotlin-agent-skills")

        assertTrue(result is RemoteInstallResult.Success)
        assertEquals("Kotlin/kotlin-agent-skills", result.source)
        assertEquals(listOf("kotlin-tooling-kotlin-toolchain"), result.installedSkills)
        assertTrue(fakeEnv.fileExists(".agents/skills/kotlin-tooling-kotlin-toolchain/SKILL.md"))
    }

    @Test
    fun `installRemoteSkill with scoped subskill installs only specified skill`() {
        val cloneDir = ".kmp-cache/remote/Meet-Miyani-compose-skill"
        fakeEnv.whenCommand(
            listOf("git", "clone", "--depth", "1", "https://github.com/Meet-Miyani/compose-skill.git", cloneDir),
            com.jesusdmedinac.kmp.core.system.ProcessResult(0, "Cloned", "")
        )
        fakeEnv.writeFileText(
            "$cloneDir/skills/compose-architecture/SKILL.md",
            "---\nname: compose-architecture\ndescription: Arch\n---\n# Arch Content"
        )
        fakeEnv.writeFileText(
            "$cloneDir/skills/compose-ui/SKILL.md",
            "---\nname: compose-ui\ndescription: UI\n---\n# UI Content"
        )

        val result = repository.installRemoteSkill("Meet-Miyani/compose-skill:compose-architecture")

        assertTrue(result is RemoteInstallResult.Success)
        assertEquals(listOf("compose-architecture"), result.installedSkills)
        assertTrue(fakeEnv.fileExists(".agents/skills/compose-architecture/SKILL.md"))
        kotlin.test.assertFalse(fakeEnv.fileExists(".agents/skills/compose-ui/SKILL.md"))
    }

    @Test
    fun `updateRemoteSkills updates previously installed remote repositories`() {
        val cloneDir = ".kmp-cache/remote/Kotlin-kotlin-agent-skills"
        fakeEnv.whenCommand(
            listOf("git", "clone", "--depth", "1", "https://github.com/Kotlin/kotlin-agent-skills.git", cloneDir),
            com.jesusdmedinac.kmp.core.system.ProcessResult(0, "Cloned", "")
        )
        fakeEnv.writeFileText(
            "$cloneDir/skills/kotlin-tooling-kotlin-toolchain/SKILL.md",
            "---\nname: kotlin-tooling-kotlin-toolchain\ndescription: Toolchain\n---\n# Toolchain Content"
        )

        repository.installRemoteSkill("Kotlin/kotlin-agent-skills")

        val updateResults = repository.updateRemoteSkills()
        assertEquals(1, updateResults.size)
        assertTrue(updateResults.first() is RemoteInstallResult.Success)
    }
}
