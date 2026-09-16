package com.jesusdmedinac.kmp.core.project

import com.jesusdmedinac.kmp.core.project.model.BuildSystem
import com.jesusdmedinac.kmp.core.project.parser.KotlinToolchainParser
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KotlinToolchainParserTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val parser = KotlinToolchainParser(fakeEnv)

    @Test
    fun `parses multi-module Kotlin Toolchain project with project yaml and module yaml`() {
        fakeEnv.writeFileText(
            "project.yaml",
            """
            modules:
              - shared
              - app

            settings:
              kotlin:
                version: 2.1.10
            """.trimIndent()
        )

        fakeEnv.writeFileText(
            "shared/module.yaml",
            """
            product:
              type: lib
              platforms:
                - jvm
                - android
                - iosArm64
                - wasm
            """.trimIndent()
        )

        fakeEnv.writeFileText(
            "app/module.yaml",
            """
            product:
              type: app
              platforms:
                - android
                - iosArm64
            """.trimIndent()
        )

        val descriptor = parser.parse(".")
        assertNotNull(descriptor)
        assertEquals(BuildSystem.KOTLIN_TOOLCHAIN, descriptor.buildSystem)
        assertEquals("2.1.10", descriptor.kotlinVersion)

        val targets = descriptor.targets
        assertTrue(targets.contains("jvm"), "Expected target jvm in $targets")
        assertTrue(targets.contains("android"), "Expected target android in $targets")
        assertTrue(targets.contains("iosArm64"), "Expected target iosArm64 in $targets")
        assertTrue(targets.contains("wasm"), "Expected target wasm in $targets")

        assertEquals(2, descriptor.modules.size)
        assertTrue(descriptor.modules.any { it.name == "shared" })
        assertTrue(descriptor.modules.any { it.name == "app" })
    }

    @Test
    fun `parses standalone single module yaml project`() {
        fakeEnv.writeFileText(
            "module.yaml",
            """
            product:
              type: lib
              platforms:
                - jvm
                - linuxX64
            """.trimIndent()
        )

        val descriptor = parser.parse(".")
        assertNotNull(descriptor)
        assertEquals(BuildSystem.KOTLIN_TOOLCHAIN, descriptor.buildSystem)
        assertTrue(descriptor.targets.contains("jvm"))
        assertTrue(descriptor.targets.contains("linuxX64"))
    }
}
