package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.jesusdmedinac.kmp.core.skill.SkillsRepository
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

class SkillsCommandTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val repository = SkillsRepository(fakeEnv)
    private val recorder = TerminalRecorder(width = 80)
    private val terminal = Terminal(terminalInterface = recorder)

    @Test
    fun `skills list displays formatted catalog with seed skills`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("list")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kotlin-ui-compose-adaptive"))
        assertTrue(output.contains("kmp-compose-adaptive")) // Displayed as alias
        assertTrue(output.contains("Responsive and adaptive UI patterns"))
        assertTrue(output.contains("kotlin-networking-ktor"))
        assertTrue(output.contains("kotlin-data-room-kmp"))
    }

    @Test
    fun `skills list with json outputs parseable JSON array`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("list", "--json")) { it.run() }

        val output = recorder.output().trim()
        val skills = Json.decodeFromString<List<SkillCatalogEntry>>(output)

        assertTrue(skills.size >= 6)
        assertTrue(skills.any { it.id == "kotlin-ui-compose-adaptive" })
        assertTrue(skills.any { it.id == "kotlin-data-room-kmp" })
    }

    @Test
    fun `skills find filters skills by keyword`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("find", "compose")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kotlin-ui-compose-adaptive"))
        assertTrue(!output.contains("kotlin-networking-ktor"))
    }

    @Test
    fun `skills describe displays skill details and usage instructions`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("describe", "kotlin-networking-ktor")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kotlin-networking-ktor"))
        assertTrue(output.contains("Multiplatform HTTP client architecture"))
    }

    @Test
    fun `skills describe resolves legacy alias`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("describe", "kmp-ktor-networking")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kotlin-networking-ktor"))
        assertTrue(output.contains("Multiplatform HTTP client architecture"))
    }

    @Test
    fun `skills describe displays progressive router directives`() {
        fakeEnv.writeFileText(
            "skills/kotlin-kmp-router/SKILL.md",
            "---\nname: kotlin-kmp-router\ndescription: Lightweight progressive routing tree\n---\n# Progressive Router\n\nValidate-Before-Recall"
        )
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("describe", "kotlin-kmp-router")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kotlin-kmp-router"))
        assertTrue(output.contains("Lightweight progressive routing tree"))
        assertTrue(output.contains("Validate-Before-Recall"))
    }

    @Test
    fun `skills add installs skill into agents directory`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("add", "kotlin-data-sqldelight-database")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Installed skill"))
        assertTrue(fakeEnv.fileExists(".agents/skills/kotlin-data-sqldelight-database/SKILL.md"))
    }

    @Test
    fun `skills add with legacy alias resolves and installs canonical skill`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("add", "kmp-toolchain-migration")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kotlin-tooling-gradle-to-kotlin-toolchain"))
        assertTrue(output.contains("resolved from 'kmp-toolchain-migration'"))
        assertTrue(fakeEnv.fileExists(".agents/skills/kotlin-tooling-gradle-to-kotlin-toolchain/SKILL.md"))
    }

    @Test
    fun `skills add with remote repository coordinate installs remote skills`() {
        val cloneDir = ".kmp-cache/remote/Kotlin-kotlin-agent-skills"
        fakeEnv.whenCommand(
            listOf("git", "clone", "--depth", "1", "https://github.com/Kotlin/kotlin-agent-skills.git", cloneDir),
            com.jesusdmedinac.kmp.core.system.ProcessResult(0, "Cloned", "")
        )
        fakeEnv.writeFileText(
            "$cloneDir/skills/kotlin-tooling-kotlin-toolchain/SKILL.md",
            "---\nname: kotlin-tooling-kotlin-toolchain\ndescription: Toolchain\n---\n# Toolchain Content"
        )

        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("add", "Kotlin/kotlin-agent-skills")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Installed 1 skills from 'Kotlin/kotlin-agent-skills'"))
        assertTrue(fakeEnv.fileExists(".agents/skills/kotlin-tooling-kotlin-toolchain/SKILL.md"))
    }

    @Test
    fun `skills update refreshes registered remote skills`() {
        val cloneDir = ".kmp-cache/remote/Kotlin-kotlin-agent-skills"
        fakeEnv.whenCommand(
            listOf("git", "clone", "--depth", "1", "https://github.com/Kotlin/kotlin-agent-skills.git", cloneDir),
            com.jesusdmedinac.kmp.core.system.ProcessResult(0, "Cloned", "")
        )
        fakeEnv.writeFileText(
            "$cloneDir/skills/kotlin-tooling-kotlin-toolchain/SKILL.md",
            "---\nname: kotlin-tooling-kotlin-toolchain\ndescription: Toolchain\n---\n# Toolchain Content"
        )

        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("add", "Kotlin/kotlin-agent-skills")) { it.run() }

        CommandLineParser.parseAndRun(command, listOf("update")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Updated 1 skills from 'Kotlin/kotlin-agent-skills'"))
    }
}
