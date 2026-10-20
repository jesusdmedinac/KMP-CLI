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
            "kotlin-kmp-router",
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
            "kmp-router",
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
    fun `kotlin-kmp-router skill encapsulates progressive routing and senior staff directives`() {
        val skill = DefaultSkillsCatalog.catalog.skills.find { it.id == "kotlin-kmp-router" }
        assertTrue(skill != null)
        assertEquals("core", skill.category)
        assertTrue(skill.tags.contains("router"))
        assertTrue(skill.aliases.contains("kmp-router"))
    }

    @Test
    fun `default catalog has valid schemaVersion`() {
        assertEquals("1.0.0", DefaultSkillsCatalog.catalog.schemaVersion)
    }

    @Test
    fun `default catalog registers all official Kotlin Foundation Grant Winner skills`() {
        val catalog = DefaultSkillsCatalog.catalog

        val grantWinnerSkills = listOf(
            Triple("kotlin-media-coil", "kmp-coil", "media"),
            Triple("kotlin-data-multiplatform-settings", "kmp-settings", "persistence"),
            Triple("kotlin-data-store5", "kmp-store5", "persistence"),
            Triple("kotlin-architecture-orbit-mvi", "kmp-orbit-mvi", "architecture"),
            Triple("kotlin-ui-compose-rich-editor", "kmp-rich-editor", "ui"),
            Triple("kotlin-interop-swift-coroutines", "kmp-swift-coroutines", "interop"),
            Triple("kotlin-web-kobweb", "kmp-kobweb", "web"),
            Triple("kotlin-cloud-firebase", "kmp-firebase", "cloud")
        )

        for ((canonicalId, legacyAlias, expectedCategory) in grantWinnerSkills) {
            val skill = catalog.skills.find { it.id == canonicalId }
            assertTrue(skill != null, "Missing grant winner skill: $canonicalId")
            assertEquals(expectedCategory, skill.category, "Category mismatch for $canonicalId")
            assertTrue(skill.tags.contains("grant-winner"), "Skill $canonicalId must be tagged with 'grant-winner'")
            assertTrue(skill.aliases.contains(legacyAlias), "Skill $canonicalId must have alias $legacyAlias")
            assertTrue(skill.targets.isNotEmpty(), "Skill $canonicalId must declare targets")
            assertFalse(skill.path.isNullOrBlank(), "Skill $canonicalId must have valid path")
        }
    }
}
