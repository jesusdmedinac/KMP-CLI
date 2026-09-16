package com.jesusdmedinac.kmp.core.project

import com.jesusdmedinac.kmp.core.project.model.BuildSystem
import com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UnifiedProjectParserTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val parser = UnifiedProjectParser(fakeEnv)

    @Test
    fun `detects and parses Gradle project with version catalog`() {
        fakeEnv.writeFileText("settings.gradle.kts", """rootProject.name = "MyGradleKmp"""")
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            kotlin {
                androidTarget()
                iosArm64()
                jvm()
            }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "gradle/libs.versions.toml",
            """
            [versions]
            kotlin = "2.2.0"
            [libraries]
            ktor = "io.ktor:ktor-client:3.0.0"
            """.trimIndent()
        )

        val project = parser.parse(".")
        assertNotNull(project)
        assertEquals("MyGradleKmp", project.name)
        assertEquals(BuildSystem.GRADLE, project.buildSystem)
        assertTrue(project.targets.contains("android"))
        assertTrue(project.targets.contains("iosArm64"))
        assertTrue(project.targets.contains("jvm"))
        assertNotNull(project.dependencies)
        assertEquals("2.2.0", project.dependencies?.versions?.get("kotlin"))
    }

    @Test
    fun `detects and parses Kotlin Toolchain project`() {
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
            """.trimIndent()
        )

        val project = parser.parse(".")
        assertNotNull(project)
        assertEquals(BuildSystem.KOTLIN_TOOLCHAIN, project.buildSystem)
        assertEquals("2.1.0", project.kotlinVersion)
        assertTrue(project.targets.contains("android"))
        assertTrue(project.targets.contains("ios"))
    }
}
