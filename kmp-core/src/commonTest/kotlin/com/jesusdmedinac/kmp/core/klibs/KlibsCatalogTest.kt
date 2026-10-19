package com.jesusdmedinac.kmp.core.klibs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KlibsCatalogTest {
    private val catalog = KlibsCatalog()

    @Test
    fun `query without parameters returns all curated libraries`() {
        val result = catalog.query()
        assertTrue(result.libraries.isNotEmpty())
        assertTrue(result.libraries.any { it.name == "Ktor Client" })
        assertTrue(result.libraries.any { it.name == "Room KMP" })
        assertTrue(result.libraries.any { it.name == "SQLDelight" })
    }

    @Test
    fun `query for room returns verified coordinates and flags pre-stable alpha status`() {
        val projectTargets = listOf("android", "iosArm64", "jvm")
        val result = catalog.query(query = "room", projectTargets = projectTargets)

        assertEquals(1, result.libraries.size)
        val room = result.libraries.first()
        assertEquals("androidx.room:room-runtime", room.coordinate)
        assertEquals("2.7.0-alpha13", room.version)
        assertEquals("Pre-stable", room.status)
        assertTrue(room.compatibleWithProject)
        assertTrue(room.missingTargets.isEmpty())
    }

    @Test
    fun `query for room flags incompatible project target when wasmJs is targeted`() {
        val projectTargets = listOf("android", "wasmJs")
        val result = catalog.query(query = "room", projectTargets = projectTargets)

        assertEquals(1, result.libraries.size)
        val room = result.libraries.first()
        assertFalse(room.compatibleWithProject)
        assertEquals(listOf("wasmJs"), room.missingTargets)
    }

    @Test
    fun `query for ktor client confirms compatibility across android ios desktop and wasm`() {
        val projectTargets = listOf("android", "iosArm64", "jvm", "wasmJs")
        val result = catalog.query(query = "ktor", projectTargets = projectTargets)

        assertEquals(1, result.libraries.size)
        val ktor = result.libraries.first()
        assertEquals("Stable", ktor.status)
        assertTrue(ktor.compatibleWithProject)
        assertTrue(ktor.missingTargets.isEmpty())
    }

    @Test
    fun `isTargetSupported normalizes common target architecture variations`() {
        val supported = listOf("android", "ios", "jvm", "wasm")

        assertTrue(KlibsCatalog.isTargetSupported("android", supported))
        assertTrue(KlibsCatalog.isTargetSupported("iosArm64", supported))
        assertTrue(KlibsCatalog.isTargetSupported("iosSimulatorArm64", supported))
        assertTrue(KlibsCatalog.isTargetSupported("jvm", supported))
        assertTrue(KlibsCatalog.isTargetSupported("desktop", supported))
        assertTrue(KlibsCatalog.isTargetSupported("wasmJs", supported))
        assertFalse(KlibsCatalog.isTargetSupported("linuxX64", supported))
    }
}
