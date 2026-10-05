package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.jesusdmedinac.kmp.core.project.model.BuildSystem
import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DescribeCommandTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val parser = UnifiedProjectParser(fakeEnv)
    private val recorder = TerminalRecorder(width = 80)
    private val terminal = Terminal(terminalInterface = recorder)

    @Test
    fun `describe with json outputs valid ProjectDescriptor JSON for Gradle project`() {
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """
            rootProject.name = "MyAwesomeApp"
            include(":shared")
            include(":composeApp")
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform") version "2.2.20" apply false
            }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "shared/build.gradle.kts",
            """
            kotlin {
                androidTarget()
                iosArm64()
                iosX64()
                jvm("desktop")
            }
            """.trimIndent()
        )

        val command = DescribeCommand(parser = parser, terminal = terminal)
        CommandLineParser.parseAndRun(command, listOf("--json")) { it.run() }

        val output = recorder.output().trim()
        val descriptor = Json.decodeFromString<ProjectDescriptor>(output)

        assertNotNull(descriptor)
        assertEquals("MyAwesomeApp", descriptor.name)
        assertEquals(BuildSystem.GRADLE, descriptor.buildSystem)
        assertEquals("2.2.20", descriptor.kotlinVersion)
        assertTrue(descriptor.targets.contains("android"))
        assertTrue(descriptor.targets.contains("iosArm64"))
        assertTrue(descriptor.targets.contains("iosX64"))
        assertTrue(descriptor.targets.contains("jvm"))
        assertEquals(2, descriptor.modules.size)
    }

    @Test
    fun `describe with json outputs valid ProjectDescriptor JSON for Kotlin Toolchain project`() {
        fakeEnv.writeFileText(
            "project.yaml",
            """
            modules:
              - shared
            settings:
              kotlin:
                version: 2.1.0
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "shared/module.yaml",
            """
            product:
              type: lib
              platforms:
                - android
                - ios
                - wasm
            """.trimIndent()
        )

        val command = DescribeCommand(parser = parser, terminal = terminal)
        CommandLineParser.parseAndRun(command, listOf("--json")) { it.run() }

        val output = recorder.output().trim()
        val descriptor = Json.decodeFromString<ProjectDescriptor>(output)

        assertNotNull(descriptor)
        assertEquals(BuildSystem.KOTLIN_TOOLCHAIN, descriptor.buildSystem)
        assertEquals("2.1.0", descriptor.kotlinVersion)
        assertTrue(descriptor.targets.contains("android"))
        assertTrue(descriptor.targets.contains("ios"))
        assertTrue(descriptor.targets.contains("wasm"))
    }

    @Test
    fun `describe human output renders project name build system targets and modules`() {
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """
            rootProject.name = "MyAwesomeApp"
            include(":shared")
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            kotlin {
                jvm()
                androidTarget()
            }
            """.trimIndent()
        )

        val command = DescribeCommand(parser = parser, terminal = terminal)
        CommandLineParser.parseAndRun(command, emptyList()) { it.run() }

        val output = recorder.output()
        assertTrue(output.contains("MyAwesomeApp"))
        assertTrue(output.contains("gradle") || output.contains("Gradle"))
        assertTrue(output.contains("android"))
        assertTrue(output.contains("jvm"))
        assertTrue(output.contains("shared"))
    }

    @Test
    fun `describe fails with exit code 1 when no project found`() {
        val command = DescribeCommand(parser = parser, terminal = terminal)
        val exception = assertFailsWith<ProgramResult> {
            CommandLineParser.parseAndRun(command, emptyList()) { it.run() }
        }
        assertEquals(1, exception.statusCode)
        assertTrue(recorder.output().contains("No Kotlin Multiplatform project found"))
    }
}
