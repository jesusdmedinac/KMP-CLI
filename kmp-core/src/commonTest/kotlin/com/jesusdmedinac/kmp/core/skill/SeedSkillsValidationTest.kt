package com.jesusdmedinac.kmp.core.skill

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeedSkillsValidationTest {

    @Test
    fun `default catalog contains curated official seed skills`() {
        val catalog = DefaultSkillsCatalog.catalog
        assertTrue(catalog.skills.size >= 5)

        val expectedIds = listOf(
            "kmp-compose-adaptive",
            "kmp-ktor-networking",
            "kmp-sqldelight-database",
            "kmp-toolchain-migration",
            "kmp-coroutines-concurrency",
            "kmp-cli"
        )

        for (expectedId in expectedIds) {
            val skill = catalog.skills.find { it.id == expectedId }
            assertTrue(skill != null, "Missing expected seed skill: $expectedId")
            assertFalse(skill.name.isBlank())
            assertFalse(skill.description.isBlank())
            assertTrue(skill.tags.isNotEmpty())
            assertTrue(skill.targets.isNotEmpty())
        }
    }

    @Test
    fun `default catalog has valid schemaVersion`() {
        assertEquals("1.0.0", DefaultSkillsCatalog.catalog.schemaVersion)
    }
}
