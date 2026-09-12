package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.jesusdmedinac.kmp.core.skill.SkillsRepository
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertTrue(output.contains("kmp-compose-adaptive"))
        assertTrue(output.contains("Responsive and adaptive UI patterns"))
        assertTrue(output.contains("kmp-ktor-networking"))
    }

    @Test
    fun `skills list with json outputs parseable JSON array`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("list", "--json")) { it.run() }

        val output = recorder.output().trim()
        val skills = Json.decodeFromString<List<SkillCatalogEntry>>(output)

        assertTrue(skills.size >= 5)
        assertTrue(skills.any { it.id == "kmp-compose-adaptive" })
    }

    @Test
    fun `skills find filters skills by keyword`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("find", "compose")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kmp-compose-adaptive"))
        assertTrue(!output.contains("kmp-ktor-networking"))
    }

    @Test
    fun `skills describe displays skill details and usage instructions`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("describe", "kmp-ktor-networking")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("kmp-ktor-networking"))
        assertTrue(output.contains("Multiplatform HTTP client architecture"))
    }

    @Test
    fun `skills add installs skill into agents directory`() {
        val command = SkillsCommand(repository, terminal)
        CommandLineParser.parseAndRun(command, listOf("add", "kmp-sqldelight-database")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Installed skill"))
        assertTrue(fakeEnv.fileExists(".agents/skills/kmp-sqldelight-database/SKILL.md"))
    }
}
