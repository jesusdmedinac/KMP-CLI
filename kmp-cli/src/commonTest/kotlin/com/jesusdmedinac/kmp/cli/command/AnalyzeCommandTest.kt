package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.jesusdmedinac.kmp.core.migration.MigrationEngine
import com.jesusdmedinac.kmp.core.project.model.ProjectDependencies
import com.jesusdmedinac.kmp.core.project.parser.VersionCatalogParser
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AnalyzeCommandTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val parser = VersionCatalogParser(fakeEnv)
    private val recorder = TerminalRecorder(width = 80)
    private val terminal = Terminal(terminalInterface = recorder)

    @Test
    fun `analyze dependencies with json outputs parseable ProjectDependencies JSON`() {
        val tomlContent = """
            [versions]
            kotlin = "2.2.0"
            ktor = "3.0.0"

            [libraries]
            ktor-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
            kotlinx-coroutines = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.9.0" }

            [plugins]
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
        """.trimIndent()

        fakeEnv.writeFileText("gradle/libs.versions.toml", tomlContent)

        val command = AnalyzeCommand(versionCatalogParser = parser, terminal = terminal)
        CommandLineParser.parseAndRun(command, listOf("dependencies", "--json")) { it.run() }

        val output = recorder.output().trim()
        val dependencies = Json.decodeFromString<ProjectDependencies>(output)

        assertNotNull(dependencies)
        assertEquals(2, dependencies.versions.size)
        assertEquals("2.2.0", dependencies.versions["kotlin"])
        assertEquals("3.0.0", dependencies.versions["ktor"])

        assertEquals(2, dependencies.libraries.size)
        assertTrue(dependencies.libraries.any { it.alias == "ktor-core" && it.version == "3.0.0" })
        assertTrue(dependencies.libraries.any { it.alias == "kotlinx-coroutines" && it.version == "1.9.0" })

        assertEquals(1, dependencies.plugins.size)
        assertEquals("kotlinMultiplatform", dependencies.plugins.first().alias)
        assertEquals("org.jetbrains.kotlin.multiplatform", dependencies.plugins.first().id)
        assertEquals("2.2.0", dependencies.plugins.first().version)
    }

    @Test
    fun `analyze dependencies human output renders versions libraries and plugins`() {
        val tomlContent = """
            [versions]
            kotlin = "2.2.0"

            [libraries]
            ktor-core = "io.ktor:ktor-client-core:3.0.0"

            [plugins]
            kmp = { id = "org.jetbrains.kotlin.multiplatform", version = "2.2.0" }
        """.trimIndent()

        fakeEnv.writeFileText("gradle/libs.versions.toml", tomlContent)

        val command = AnalyzeCommand(versionCatalogParser = parser, terminal = terminal)
        CommandLineParser.parseAndRun(command, listOf("dependencies")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Versions"))
        assertTrue(output.contains("kotlin = 2.2.0"))
        assertTrue(output.contains("Libraries"))
        assertTrue(output.contains("ktor-core"))
        assertTrue(output.contains("Plugins"))
        assertTrue(output.contains("org.jetbrains.kotlin.multiplatform"))
    }

    @Test
    fun `analyze dependencies accepts direct toml file path`() {
        val tomlContent = """
            [versions]
            koin = "4.0.0"

            [libraries]
            koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
        """.trimIndent()

        fakeEnv.writeFileText("custom/my-catalog.toml", tomlContent)

        val command = AnalyzeCommand(versionCatalogParser = parser, terminal = terminal)
        CommandLineParser.parseAndRun(command, listOf("dependencies", "custom/my-catalog.toml", "--json")) { it.run() }

        val output = recorder.output().trim()
        val dependencies = Json.decodeFromString<ProjectDependencies>(output)

        assertNotNull(dependencies)
        assertEquals(1, dependencies.versions.size)
        assertEquals("4.0.0", dependencies.versions["koin"])
        assertEquals(1, dependencies.libraries.size)
        assertEquals("koin-core", dependencies.libraries.first().alias)
    }

    @Test
    fun `analyze dependencies fails with exit code 1 when no version catalog found`() {
        val command = AnalyzeCommand(versionCatalogParser = parser, terminal = terminal)
        val exception = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, listOf("dependencies")) { it.run() }
        }
        assertEquals(1, exception.statusCode)
        assertTrue(recorder.output().contains("No version catalog found"))
    }

    @Test
    fun `analyze with rules flag detects discrepancies and exits with code 1`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        fakeEnv.writeFileText(
            "module.yaml",
            """
                product: app
                dependencies:
                  - com.unused.lib:artifact:1.0.0
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "src/App.kt",
            """
                import com.unregistered.external.Client
            """.trimIndent()
        )
        val archChecker = com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker(fakeEnv)
        val command = AnalyzeCommand(versionCatalogParser = parser, architectureChecker = archChecker, terminal = terminal)

        val exception = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, listOf("--rules")) { it.run() }
        }

        assertEquals(1, exception.statusCode)
        val output = recorder.output()
        assertTrue(output.contains("ARCH003_UNUSED_DEPENDENCY"))
        assertTrue(output.contains("ARCH003_UNDECLARED_DEPENDENCY"))
    }

    @Test
    fun `analyze rules subcommand outputs json report`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        fakeEnv.writeFileText(
            "module.yaml",
            """
                product: app
                dependencies:
                  - com.unused.lib:artifact:1.0.0
            """.trimIndent()
        )
        val archChecker = com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker(fakeEnv)
        val command = AnalyzeCommand(versionCatalogParser = parser, architectureChecker = archChecker, terminal = terminal)

        CommandLineParser.parseAndRun(command, listOf("rules", "--json")) { it.run() }

        val output = recorder.output().trim()
        val report = Json.decodeFromString<com.jesusdmedinac.kmp.core.architecture.model.ArchitectureReport>(output)
        assertEquals(1, report.violations.size)
        assertEquals("ARCH003_UNUSED_DEPENDENCY", report.violations.first().ruleId)
    }

    @Test
    fun `analyze libraries with json outputs verified library recommendations with project targets and flags pre-stable status`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """
            rootProject.name = "MyAwesomeApp"
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            kotlin {
                androidTarget()
                jvm("desktop")
                wasmJs()
            }
            """.trimIndent()
        )

        val projectParser = com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser(fakeEnv)
        val command = AnalyzeCommand(
            versionCatalogParser = parser,
            projectParser = projectParser,
            terminal = terminal,
        )

        CommandLineParser.parseAndRun(command, listOf("libraries", "room", "--json")) { it.run() }

        val output = recorder.output().trim()
        val result = Json.decodeFromString<com.jesusdmedinac.kmp.core.klibs.model.LibraryRecommendationResult>(output)

        assertEquals("room", result.query)
        assertTrue(result.projectTargets.contains("android"))
        assertTrue(result.projectTargets.contains("wasmJs"))
        assertEquals(1, result.libraries.size)

        val room = result.libraries.first()
        assertEquals("androidx.room:room-runtime", room.coordinate)
        assertEquals("Pre-stable", room.status)
        assertEquals(false, room.compatibleWithProject)
        assertEquals(listOf("wasmJs"), room.missingTargets)
    }

    @Test
    fun `analyze libraries human output renders compatibility status for project targets`() {
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """
            rootProject.name = "KtorApp"
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            kotlin {
                androidTarget()
                jvm("desktop")
            }
            """.trimIndent()
        )

        val projectParser = com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser(fakeEnv)
        val command = AnalyzeCommand(
            versionCatalogParser = parser,
            projectParser = projectParser,
            terminal = terminal,
        )

        CommandLineParser.parseAndRun(command, listOf("libraries", "ktor")) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("klibs.io"))
        assertTrue(output.contains("io.ktor:ktor-client-core"))
        assertTrue(output.contains("All project targets supported"))
    }

    @Test
    fun `analyze on legacy React Native codebase redirects to kmp migrate audit`() {
        val testEnv = FakeSystemEnvironment()
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        testEnv.writeFileText(
            "package.json",
            """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.72.0"
              }
            }
            """.trimIndent()
        )

        val migrationEngine = MigrationEngine(testEnv)
        val command = AnalyzeCommand(
            versionCatalogParser = VersionCatalogParser(testEnv),
            migrationEngine = migrationEngine,
            terminal = terminal,
        )

        CommandLineParser.parseAndRun(command, listOf()) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("Legacy cross-platform codebase detected"))
        assertTrue(output.contains("React Native"))
        assertTrue(output.contains("kmp migrate audit"))
    }

    @Test
    fun `analyze with json on legacy Flutter codebase returns LegacyDetectionResult payload`() {
        val testEnv = FakeSystemEnvironment()
        val recorder = TerminalRecorder(width = 80)
        val terminal = Terminal(terminalInterface = recorder)
        testEnv.writeFileText(
            "pubspec.yaml",
            """
            name: flutter_app
            dependencies:
              flutter:
                sdk: flutter
            """.trimIndent()
        )

        val migrationEngine = MigrationEngine(testEnv)
        val command = AnalyzeCommand(
            versionCatalogParser = VersionCatalogParser(testEnv),
            migrationEngine = migrationEngine,
            terminal = terminal,
        )

        CommandLineParser.parseAndRun(command, listOf("--json")) { it.run() }

        val output = recorder.output().trim()
        val legacy = Json.decodeFromString<com.jesusdmedinac.kmp.core.migration.model.LegacyDetectionResult>(output)
        assertTrue(legacy.detected)
        assertEquals(com.jesusdmedinac.kmp.core.migration.model.LegacyFramework.FLUTTER, legacy.framework)
        assertEquals("kmp migrate audit", legacy.suggestedCommand)
    }
}
