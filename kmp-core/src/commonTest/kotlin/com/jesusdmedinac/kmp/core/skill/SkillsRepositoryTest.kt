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
        assertTrue(catalog.skills.size >= 5)
        assertNotNull(catalog.skills.find { it.id == "kmp-compose-adaptive" })
    }

    @Test
    fun `findSkills filters by keyword across id name tags and targets`() {
        val composeSkills = repository.findSkills("compose")
        assertEquals(1, composeSkills.size)
        assertEquals("kmp-compose-adaptive", composeSkills.first().id)

        val networkSkills = repository.findSkills("networking")
        assertEquals(1, networkSkills.size)
        assertEquals("kmp-ktor-networking", networkSkills.first().id)

        val unknownSkills = repository.findSkills("nonexistent-tool")
        assertTrue(unknownSkills.isEmpty())
    }

    @Test
    fun `getSkill returns entry case-insensitively`() {
        val skill = repository.getSkill("KMP-KTOR-NETWORKING")
        assertNotNull(skill)
        assertEquals("kmp-ktor-networking", skill.id)

        assertNull(repository.getSkill("unknown-skill"))
    }

    @Test
    fun `installSkill installs to local agents directory by default`() {
        val result = repository.installSkill("kmp-compose-adaptive")

        assertTrue(result is InstallResult.Success)
        assertEquals(".agents/skills/kmp-compose-adaptive/SKILL.md", result.installedPath)
        assertEquals(false, result.isUpdate)

        assertTrue(fakeEnv.fileExists(".agents/skills/kmp-compose-adaptive/SKILL.md"))
        val installedContent = fakeEnv.readFileText(".agents/skills/kmp-compose-adaptive/SKILL.md")
        assertNotNull(installedContent)
        assertTrue(installedContent.contains("name: kmp-compose-adaptive"))
    }

    @Test
    fun `installSkill with isGlobal installs to home directory`() {
        fakeEnv.setEnv("HOME", "/Users/testdeveloper")

        val result = repository.installSkill("kmp-sqldelight-database", isGlobal = true)

        assertTrue(result is InstallResult.Success)
        assertEquals("/Users/testdeveloper/.kmp/skills/kmp-sqldelight-database/SKILL.md", result.installedPath)
        assertTrue(fakeEnv.fileExists("/Users/testdeveloper/.kmp/skills/kmp-sqldelight-database/SKILL.md"))
    }

    @Test
    fun `installSkill returns AlreadyExists when skill is already installed and force is false`() {
        val firstResult = repository.installSkill("kmp-compose-adaptive")
        assertTrue(firstResult is InstallResult.Success)

        val secondResult = repository.installSkill("kmp-compose-adaptive", force = false)
        assertTrue(secondResult is InstallResult.AlreadyExists)
        assertEquals("kmp-compose-adaptive", secondResult.skillId)
    }

    @Test
    fun `installSkill overwrites and reports update when force is true`() {
        repository.installSkill("kmp-compose-adaptive")

        val updateResult = repository.installSkill("kmp-compose-adaptive", force = true)
        assertTrue(updateResult is InstallResult.Success)
        assertEquals(true, updateResult.isUpdate)
    }

    @Test
    fun `installSkill returns NotFound when skill is missing from catalog`() {
        val result = repository.installSkill("imaginary-missing-skill")
        assertTrue(result is InstallResult.NotFound)
    }
}
