package com.jesusdmedinac.kmp.core.project

import com.jesusdmedinac.kmp.core.project.model.BuildSystem
import com.jesusdmedinac.kmp.core.project.parser.GradleProjectParser
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GradleProjectParserTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val parser = GradleProjectParser(fakeEnv)

    @Test
    fun `parses standard multi-module Gradle KMP project with root name and targets`() {
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """
            rootProject.name = "MyAwesomeApp"
            include(":shared")
            include(":composeApp")
            """.trimIndent()
        )

        fakeEnv.writeFileText(
            "shared/build.gradle.kts",
            """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.androidLibrary)
            }

            kotlin {
                androidTarget {
                    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
                }
                iosX64()
                iosArm64()
                iosSimulatorArm64()
                jvm("desktop")
                wasmJs {
                    browser()
                }
            }
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

        val descriptor = parser.parse(".")
        assertNotNull(descriptor)
        assertEquals("MyAwesomeApp", descriptor.name)
        assertEquals(BuildSystem.GRADLE, descriptor.buildSystem)
        assertEquals("2.2.20", descriptor.kotlinVersion)

        val targets = descriptor.targets
        assertTrue(targets.contains("android"), "Expected target android in $targets")
        assertTrue(targets.contains("iosX64"), "Expected target iosX64 in $targets")
        assertTrue(targets.contains("iosArm64"), "Expected target iosArm64 in $targets")
        assertTrue(targets.contains("jvm"), "Expected target jvm in $targets")
        assertTrue(targets.contains("wasmJs"), "Expected target wasmJs in $targets")

        assertEquals(2, descriptor.modules.size)
        assertTrue(descriptor.modules.any { it.name == "shared" && it.path == ":shared" })
        assertTrue(descriptor.modules.any { it.name == "composeApp" && it.path == ":composeApp" })
    }

    @Test
    fun `parses single module Gradle KMP project when settings has only root name`() {
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """rootProject.name = "SingleKmpLib""""
        )

        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            kotlin {
                jvm()
                linuxX64()
            }
            """.trimIndent()
        )

        val descriptor = parser.parse(".")
        assertNotNull(descriptor)
        assertEquals("SingleKmpLib", descriptor.name)
        assertEquals(BuildSystem.GRADLE, descriptor.buildSystem)
        assertTrue(descriptor.targets.contains("jvm"))
        assertTrue(descriptor.targets.contains("linuxX64"))
    }
}
