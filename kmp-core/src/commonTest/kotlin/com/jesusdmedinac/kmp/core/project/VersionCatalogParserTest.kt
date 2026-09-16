package com.jesusdmedinac.kmp.core.project

import com.jesusdmedinac.kmp.core.project.parser.VersionCatalogParser
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VersionCatalogParserTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val parser = VersionCatalogParser(fakeEnv)

    @Test
    fun `parses libs versions toml with versions libraries and plugins`() {
        val tomlContent = """
            [versions]
            kotlin = "2.2.21"
            ktor = "3.3.0"
            coroutines = "1.10.2"

            [libraries]
            kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
            ktor-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
            kotlinx-coroutines = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }

            [plugins]
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
            composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
        """.trimIndent()

        fakeEnv.writeFileText("gradle/libs.versions.toml", tomlContent)

        val catalog = parser.parse("gradle/libs.versions.toml")
        assertNotNull(catalog)

        assertEquals(3, catalog.versions.size)
        assertEquals("2.2.21", catalog.versions["kotlin"])
        assertEquals("3.3.0", catalog.versions["ktor"])
        assertEquals("1.10.2", catalog.versions["coroutines"])

        assertEquals(3, catalog.libraries.size)
        val ktorLib = catalog.libraries.find { it.alias == "ktor-core" }
        assertNotNull(ktorLib)
        assertEquals("io.ktor:ktor-client-core", ktorLib.module)
        assertEquals("3.3.0", ktorLib.version)

        val coroutinesLib = catalog.libraries.find { it.alias == "kotlinx-coroutines" }
        assertNotNull(coroutinesLib)
        assertEquals("org.jetbrains.kotlinx", coroutinesLib.group)
        assertEquals("kotlinx-coroutines-core", coroutinesLib.name)
        assertEquals("1.10.2", coroutinesLib.version)

        assertEquals(2, catalog.plugins.size)
        val kmpPlugin = catalog.plugins.find { it.alias == "kotlinMultiplatform" }
        assertNotNull(kmpPlugin)
        assertEquals("org.jetbrains.kotlin.multiplatform", kmpPlugin.id)
        assertEquals("2.2.21", kmpPlugin.version)
    }
}
