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

        val expectedCanonicalIds = listOf(
            "kotlin-tooling-gradle-to-kotlin-toolchain",
            "kotlin-ui-compose-adaptive",
            "kotlin-ui-sdui-compose",
            "kotlin-data-room-kmp",
            "kotlin-data-sqldelight-database",
            "kotlin-networking-ktor",
            "kotlin-concurrency-coroutines",
            "kmp-cli"
        )

        for (expectedId in expectedCanonicalIds) {
            val skill = catalog.skills.find { it.id == expectedId }
            assertTrue(skill != null, "Missing expected canonical seed skill: $expectedId")
            assertFalse(skill.name.isBlank())
            assertFalse(skill.description.isBlank())
            assertTrue(skill.tags.isNotEmpty())
            assertTrue(skill.targets.isNotEmpty())
        }

        // Verify backward compatibility aliases
        val expectedAliases = listOf(
            "kmp-compose-adaptive",
            "kmp-ktor-networking",
            "kmp-sqldelight-database",
            "kmp-toolchain-migration",
            "kmp-coroutines-concurrency"
        )
        for (legacyAlias in expectedAliases) {
            val skill = catalog.skills.find { it.aliases.contains(legacyAlias) }
            assertTrue(skill != null, "Missing alias for legacy skill: $legacyAlias")
        }
    }

    @Test
    fun `default catalog has valid schemaVersion`() {
        assertEquals("1.0.0", DefaultSkillsCatalog.catalog.schemaVersion)
    }
}
