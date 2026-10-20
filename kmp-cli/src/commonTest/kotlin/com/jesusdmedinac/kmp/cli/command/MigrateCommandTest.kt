package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.jesusdmedinac.kmp.core.migration.MigrationEngine
import com.jesusdmedinac.kmp.core.migration.model.LegacyFramework
import com.jesusdmedinac.kmp.core.migration.model.MigrationAuditReport
import com.jesusdmedinac.kmp.core.migration.model.MigrationExecuteResult
import com.jesusdmedinac.kmp.core.migration.model.MigrationPlan
import com.jesusdmedinac.kmp.core.migration.model.ModernizationPathway
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MigrateCommandTest {
    private val json = Json { ignoreUnknownKeys = true }

    private data class TestFixture(
        val fakeEnv: FakeSystemEnvironment,
        val command: MigrateCommand,
        val recorder: TerminalRecorder,
    )

    private fun createFixture(): TestFixture {
        val fakeEnv = FakeSystemEnvironment()
        val migrationEngine = MigrationEngine(fakeEnv)
        val recorder = TerminalRecorder(width = 100)
        val terminal = Terminal(terminalInterface = recorder)
        val command = MigrateCommand(migrationEngine = migrationEngine, terminal = terminal)
        return TestFixture(fakeEnv, command, recorder)
    }

    @Test
    fun `migrate audit with json outputs structured MigrationAuditReport for React Native`() {
        val fixture = createFixture()
        val packageJson = """
            {
              "name": "LegacyRNApp",
              "version": "1.0.0",
              "dependencies": {
                "react": "18.2.0",
                "react-native": "0.72.6",
                "axios": "^1.6.0",
                "@react-native-async-storage/async-storage": "^1.21.0"
              }
            }
        """.trimIndent()
        fixture.fakeEnv.writeFileText("package.json", packageJson)

        CommandLineParser.parseAndRun(fixture.command, listOf("audit", "--json")) { it.run() }

        val output = fixture.recorder.output().trim()
        val report = json.decodeFromString<MigrationAuditReport>(output)

        assertNotNull(report)
        assertEquals(LegacyFramework.REACT_NATIVE, report.framework)
        assertEquals("0.72.6", report.frameworkVersion)
        assertEquals(2, report.dependencies.size)
        assertTrue(report.dependencies.any { it.legacyName == "axios" && it.kmpEquivalent.contains("ktor", ignoreCase = true) })
        assertTrue(report.dependencies.any { it.legacyName.contains("async-storage") && it.kmpEquivalent.contains("multiplatform-settings", ignoreCase = true) })
    }

    @Test
    fun `migrate audit human output renders rich TUI table with replacement matrix`() {
        val fixture = createFixture()
        val packageJson = """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.76.0",
                "react-native-reanimated": "^3.0.0"
              }
            }
        """.trimIndent()
        fixture.fakeEnv.writeFileText("package.json", packageJson)

        CommandLineParser.parseAndRun(fixture.command, listOf("audit")) { it.run() }

        val output = fixture.recorder.output()
        assertTrue(output.contains("Cross-Platform Modernization Audit: React Native"))
        assertTrue(output.contains("Framework Version:"))
        assertTrue(output.contains("0.76.0"))
        assertTrue(output.contains("TurboModules"))
        assertTrue(output.contains("Dependency Replacement Matrix"))
        assertTrue(output.contains("Recommended Modernization Pathway:"))
    }

    @Test
    fun `migrate audit evaluates Flutter dependencies correctly`() {
        val fixture = createFixture()
        val pubspecYaml = """
            name: legacy_flutter_app
            environment:
              sdk: '>=3.2.0 <4.0.0'
              flutter: '>=3.16.0'
            dependencies:
              flutter:
                sdk: flutter
              dio: ^5.4.0
              sqflite: ^2.3.0
        """.trimIndent()
        fixture.fakeEnv.writeFileText("pubspec.yaml", pubspecYaml)

        CommandLineParser.parseAndRun(fixture.command, listOf("audit", "--json")) { it.run() }

        val output = fixture.recorder.output().trim()
        val report = json.decodeFromString<MigrationAuditReport>(output)

        assertEquals(LegacyFramework.FLUTTER, report.framework)
        assertTrue(report.dependencies.any { it.legacyName == "dio" && it.kmpEquivalent.contains("ktor", ignoreCase = true) })
        assertTrue(report.dependencies.any { it.legacyName == "sqflite" && it.kmpEquivalent.contains("sqldelight", ignoreCase = true) })
    }

    @Test
    fun `migrate plan with json outputs 4-phase Strangler Fig roadmap`() {
        val fixture = createFixture()
        val packageJson = """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.72.0",
                "axios": "^1.0.0"
              }
            }
        """.trimIndent()
        fixture.fakeEnv.writeFileText("package.json", packageJson)

        CommandLineParser.parseAndRun(fixture.command, listOf("plan", "--json")) { it.run() }

        val output = fixture.recorder.output().trim()
        val plan = json.decodeFromString<MigrationPlan>(output)

        assertNotNull(plan)
        assertEquals(LegacyFramework.REACT_NATIVE, plan.framework)
        assertEquals(ModernizationPathway.COMPOSE_MULTIPLATFORM, plan.pathway)
        assertEquals(4, plan.phases.size)
        assertEquals(1, plan.phases[0].phaseNumber)
        assertTrue(plan.phases[0].name.contains("Foundation"))
        assertEquals(4, plan.phases[3].phaseNumber)
        assertTrue(plan.phases[3].name.contains("Legacy Shell Deletion"))
    }

    @Test
    fun `migrate plan supports overriding pathway via option`() {
        val fixture = createFixture()
        val packageJson = """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.72.0"
              }
            }
        """.trimIndent()
        fixture.fakeEnv.writeFileText("package.json", packageJson)

        CommandLineParser.parseAndRun(fixture.command, listOf("plan", "--pathway", "native-ui-shared-core", "--json")) { it.run() }

        val output = fixture.recorder.output().trim()
        val plan = json.decodeFromString<MigrationPlan>(output)

        assertEquals(ModernizationPathway.NATIVE_UI_SHARED_CORE, plan.pathway)
    }

    @Test
    fun `migrate plan human output renders all 4 phases and terminal checklist`() {
        val fixture = createFixture()
        val packageJson = """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.72.0"
              }
            }
        """.trimIndent()
        fixture.fakeEnv.writeFileText("package.json", packageJson)

        CommandLineParser.parseAndRun(fixture.command, listOf("plan")) { it.run() }

        val output = fixture.recorder.output()
        assertTrue(output.contains("Strangler Fig Modernization Roadmap"))
        assertTrue(output.contains("Phase 1:"))
        assertTrue(output.contains("Foundation & Core Bridge"))
        assertTrue(output.contains("Phase 2:"))
        assertTrue(output.contains("Domain & Data Strangler"))
        assertTrue(output.contains("Phase 3:"))
        assertTrue(output.contains("UI & Screen Incremental"))
        assertTrue(output.contains("Phase 4:"))
        assertTrue(output.contains("Legacy Shell Deletion"))
        assertTrue(output.contains("kmp-shared-core-bridge"))
    }

    @Test
    fun `migrate execute creates KMP project with human output`() {
        val fixture = createFixture()
        val packageJson = """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.72.0",
                "axios": "^1.6.0"
              }
            }
        """.trimIndent()
        fixture.fakeEnv.writeFileText("package.json", packageJson)

        CommandLineParser.parseAndRun(
            fixture.command,
            listOf("execute", ".", "--output", "MigratedApp", "--package", "com.example.testmigrated"),
        ) { it.run() }

        val output = fixture.recorder.output()
        assertTrue(output.contains("KMP Modernization Execution Succeeded!"))
        assertTrue(output.contains("MigratedApp"))
        assertTrue(output.contains("kotlin-networking-ktor"))
        assertTrue(fixture.fakeEnv.fileExists("MigratedApp/gradle/libs.versions.toml"))
    }

    @Test
    fun `migrate execute with json returns structured MigrationExecuteResult`() {
        val fixture = createFixture()
        val packageJson = """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.72.0",
                "axios": "^1.6.0"
              }
            }
        """.trimIndent()
        fixture.fakeEnv.writeFileText("package.json", packageJson)

        CommandLineParser.parseAndRun(
            fixture.command,
            listOf("execute", ".", "--output", "MigratedAppJson", "--json"),
        ) { it.run() }

        val output = fixture.recorder.output().trim()
        val result = json.decodeFromString<MigrationExecuteResult>(output)

        assertEquals("SUCCESS", result.status)
        assertEquals("MigratedAppJson", result.outputPath)
        assertEquals(LegacyFramework.REACT_NATIVE, result.framework)
        assertTrue(result.synthesizedDependencies.contains("kotlin-networking-ktor"))
    }
}
