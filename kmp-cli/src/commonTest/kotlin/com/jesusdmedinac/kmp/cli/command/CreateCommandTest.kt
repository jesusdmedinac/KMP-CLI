package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.jesusdmedinac.kmp.core.scaffold.ScaffoldingEngine
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingResult
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CreateCommandTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val engine = ScaffoldingEngine(fakeEnv)
    private val recorder = TerminalRecorder(width = 80)
    private val terminal = Terminal(terminalInterface = recorder)

    @Test
    fun `create with name and json flag outputs valid ScaffoldingResult JSON`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "MyJsonApp", "--template", "shared-ui", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertNotNull(result)
        assertEquals("SUCCESS", result.status)
        assertEquals("shared-ui", result.template)
        assertEquals("MyJsonApp", result.projectPath)
        assertTrue(result.createdFiles.isNotEmpty())
        assertTrue(fakeEnv.fileExists("MyJsonApp/settings.gradle.kts"))
    }

    @Test
    fun `create human mode prints friendly summary and next steps`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "MyCliApp", "--template", "multiplatform-library")
        ) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Project created successfully") || output.contains("MyCliApp"))
        assertTrue(output.contains("multiplatform-library"))
        assertTrue(fakeEnv.fileExists("MyCliApp/build.gradle.kts"))
    }

    @Test
    fun `create with legacy alias compose-multiplatform maps to shared-ui`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "LegacyApp", "--template", "compose-multiplatform", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("shared-ui", result.template)
    }

    @Test
    fun `create with custom targets and package generates configured project`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf(
                "--name", "CustomApp",
                "--package", "com.custom.app",
                "--targets", "android,ios",
                "--json"
            )
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertTrue(fakeEnv.fileExists("CustomApp/composeApp/src/commonMain/kotlin/com/custom/app/App.kt"))
    }

    @Test
    fun `create using positional argument generates project`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("PositionalApp", "--template", "multiplatform-library", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("PositionalApp", result.projectPath)
        assertTrue(fakeEnv.fileExists("PositionalApp/build.gradle.kts"))
    }

    @Test
    fun `create with format toolchain selects toolchain-shared-ui template`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "ToolchainApp", "--format", "toolchain", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("toolchain-shared-ui", result.template)
        assertTrue(fakeEnv.fileExists("ToolchainApp/project.yaml"))
    }

    @Test
    fun `create with ios-ui swiftui selects native-ui template`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "NativeApp", "--ios-ui", "swiftui", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("native-ui", result.template)
    }

    @Test
    fun `create with fullstack template generates fullstack project`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "FullstackApp", "--template", "fullstack", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("fullstack", result.template)
        assertTrue(fakeEnv.fileExists("FullstackApp/server/build.gradle.kts"))
    }

    @Test
    fun `create with sdui-starter template generates sdui project`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "SduiApp", "--template", "sdui-starter", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("sdui-starter", result.template)
        assertTrue(fakeEnv.fileExists("SduiApp/gradle/libs.versions.toml"))
    }

    @Test
    fun `create with custom output directory specifies custom path`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf("--name", "CustomDirApp", "-o", "custom/destination/path", "--json")
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("custom/destination/path", result.projectPath)
        assertTrue(fakeEnv.fileExists("custom/destination/path/settings.gradle.kts"))
    }

    @Test
    fun `create with invalid template prints error and fails`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        val ex = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(
                command,
                listOf("--name", "FailApp", "--template", "unknown-template")
            ) { it.run() }
        }
        assertEquals(1, ex.statusCode)
        assertTrue(recorder.output().contains("Unknown template"))
    }

    @Test
    fun `create without name fails with exit code 1`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        val ex = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, emptyList()) { it.run() }
        }
        assertEquals(1, ex.statusCode)
        assertTrue(recorder.output().contains("Project name is required"))
    }

    @Test
    fun `create without name with json outputs json error`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        val ex = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, listOf("--json")) { it.run() }
        }
        assertEquals(1, ex.statusCode)
        assertTrue(recorder.output().contains("\"error\""))
    }

    @Test
    fun `create with wizard flag prompts interactively and scaffolds native react server project`() {
        val simulatedInputs = mutableListOf(
            "MyWizardApp",
            "com.example.wizard",
            "1",
            "2",
            "2",
            "y",
            "y",
        )
        val command = CreateCommand(
            engine = engine,
            terminal = terminal,
            readInput = { simulatedInputs.removeFirstOrNull() ?: "" },
        )
        CommandLineParser.parseAndRun(
            command,
            listOf("--wizard")
        ) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Kotlin Multiplatform Project Wizard"))
        assertTrue(output.contains("Project created successfully"))
        assertTrue(fakeEnv.fileExists("MyWizardApp/settings.gradle.kts"))
        assertTrue(fakeEnv.fileExists("MyWizardApp/webApp/package.json"))
        assertTrue(fakeEnv.fileExists("MyWizardApp/webApp/vite.config.ts"))
        assertTrue(fakeEnv.fileExists("MyWizardApp/shared/build.gradle.kts"))
        val sharedBuild = fakeEnv.readFileText("MyWizardApp/shared/build.gradle.kts") ?: ""
        assertTrue(sharedBuild.contains("generateTypeScriptDefinitions"))
        assertTrue(fakeEnv.fileExists("MyWizardApp/server/build.gradle.kts"))
    }

    @Test
    fun `create with wizard flag for Kotlin Toolchain scaffolds toolchain project`() {
        val simulatedInputs = mutableListOf(
            "MyToolchainWizardApp",
            "com.example.toolchain",
            "2",
            "y",
            "n",
        )
        val command = CreateCommand(
            engine = engine,
            terminal = terminal,
            readInput = { simulatedInputs.removeFirstOrNull() ?: "" },
        )
        CommandLineParser.parseAndRun(
            command,
            listOf("--wizard")
        ) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Kotlin Multiplatform Project Wizard"))
        assertTrue(fakeEnv.fileExists("MyToolchainWizardApp/project.yaml"))
        assertTrue(fakeEnv.fileExists("MyToolchainWizardApp/kotlin"))
    }

    @Test
    fun `create with flags selecting gradle native ios react web and server maps to native-ui`() {
        val command = CreateCommand(engine = engine, terminal = terminal)
        CommandLineParser.parseAndRun(
            command,
            listOf(
                "--name", "ConfiguredWizardApp",
                "--build-system", "gradle",
                "--ios-ui", "swiftui",
                "--web-ui", "react",
                "--targets", "android,ios,web,server",
                "--json"
            )
        ) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<ScaffoldingResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("native-ui", result.template)
        assertTrue(fakeEnv.fileExists("ConfiguredWizardApp/webApp/vite.config.ts"))
        assertTrue(fakeEnv.fileExists("ConfiguredWizardApp/server/build.gradle.kts"))
    }
}

