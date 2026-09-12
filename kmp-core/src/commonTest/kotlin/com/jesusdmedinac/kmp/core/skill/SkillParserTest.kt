package com.jesusdmedinac.kmp.core.skill

import com.jesusdmedinac.kmp.core.skill.model.SkillCatalog
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry
import com.jesusdmedinac.kmp.core.skill.model.SkillCompatibility
import com.jesusdmedinac.kmp.core.skill.model.SkillManifest
import com.jesusdmedinac.kmp.core.skill.model.SkillMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SkillParserTest {

    @Test
    fun `parses standard SKILL md with frontmatter and body`() {
        val skillDoc = """
            ---
            name: kmp-compose-adaptive
            description: Responsive and adaptive UI patterns for Compose Multiplatform.
            version: 1.0.0
            author: jesusdmedinac
            tags:
              - compose
              - ui
              - adaptive
            targets:
              - android
              - ios
              - desktop
              - wasm
            triggers:
              - adaptive compose
              - responsive layout
            compatibility:
              kotlin: ">=2.0.0"
              compose: ">=1.6.0"
            ---

            # Adaptive UI in Compose Multiplatform

            Use Navigation3 and window size classes to adapt layouts.
        """.trimIndent()

        val manifest = SkillParser.parse(skillDoc)

        assertEquals("kmp-compose-adaptive", manifest.metadata.name)
        assertEquals("Responsive and adaptive UI patterns for Compose Multiplatform.", manifest.metadata.description)
        assertEquals("1.0.0", manifest.metadata.version)
        assertEquals("jesusdmedinac", manifest.metadata.author)
        assertEquals(listOf("compose", "ui", "adaptive"), manifest.metadata.tags)
        assertEquals(listOf("android", "ios", "desktop", "wasm"), manifest.metadata.targets)
        assertEquals(listOf("adaptive compose", "responsive layout"), manifest.metadata.triggers)
        assertEquals(">=2.0.0", manifest.metadata.compatibility.kotlin)
        assertEquals(">=1.6.0", manifest.metadata.compatibility.compose)

        assertTrue(manifest.content.contains("# Adaptive UI in Compose Multiplatform"))
        assertTrue(manifest.content.contains("Use Navigation3"))
    }

    @Test
    fun `parses inline lists and defaults for optional fields`() {
        val skillDoc = """
            ---
            name: kmp-ktor-networking
            description: Multiplatform HTTP client architecture with Ktor.
            tags: [ktor, networking, http]
            targets: [android, ios]
            ---

            # Ktor Networking Guide
        """.trimIndent()

        val manifest = SkillParser.parse(skillDoc)

        assertEquals("kmp-ktor-networking", manifest.metadata.name)
        assertEquals("Multiplatform HTTP client architecture with Ktor.", manifest.metadata.description)
        assertEquals("1.0.0", manifest.metadata.version)
        assertEquals(null, manifest.metadata.author)
        assertEquals(listOf("ktor", "networking", "http"), manifest.metadata.tags)
        assertEquals(listOf("android", "ios"), manifest.metadata.targets)
        assertTrue(manifest.metadata.triggers.isEmpty())
        assertEquals(null, manifest.metadata.compatibility.kotlin)
    }

    @Test
    fun `throws IllegalArgumentException when frontmatter delimiters are missing`() {
        val rawDoc = "# Pure markdown without frontmatter"
        assertFailsWith<IllegalArgumentException> {
            SkillParser.parse(rawDoc)
        }
    }

    @Test
    fun `serializes SkillManifest to valid frontmatter markdown roundtrip`() {
        val original = SkillManifest(
            metadata = SkillMetadata(
                name = "kmp-sqldelight-database",
                description = "SQL persistence and migrations with SQLDelight.",
                version = "1.2.0",
                author = "kmp-team",
                tags = listOf("database", "sql", "sqlite"),
                targets = listOf("android", "ios", "jvm"),
                triggers = listOf("database", "sqldelight"),
                compatibility = SkillCompatibility(kotlin = ">=2.1.0", gradle = ">=8.10")
            ),
            content = "# SQLDelight in KMP\n\nConfigure SqlDelight drivers per platform."
        )

        val serialized = SkillParser.serialize(original)
        val parsed = SkillParser.parse(serialized)

        assertEquals(original.metadata.name, parsed.metadata.name)
        assertEquals(original.metadata.description, parsed.metadata.description)
        assertEquals(original.metadata.version, parsed.metadata.version)
        assertEquals(original.metadata.tags, parsed.metadata.tags)
        assertEquals(original.metadata.targets, parsed.metadata.targets)
        assertEquals(original.metadata.triggers, parsed.metadata.triggers)
        assertEquals(original.metadata.compatibility.kotlin, parsed.metadata.compatibility.kotlin)
        assertEquals(original.metadata.compatibility.gradle, parsed.metadata.compatibility.gradle)
        assertTrue(parsed.content.contains("# SQLDelight in KMP"))
    }

    @Test
    fun `SkillCatalog serialization and deserialization roundtrip`() {
        val catalog = SkillCatalog(
            schemaVersion = "1.0.0",
            generatedAt = "2026-09-11T20:00:00Z",
            skills = listOf(
                SkillCatalogEntry(
                    id = "kmp-compose-adaptive",
                    name = "kmp-compose-adaptive",
                    description = "Adaptive Compose patterns",
                    version = "1.0.0",
                    author = "jesusdmedinac",
                    tags = listOf("compose", "ui"),
                    targets = listOf("android", "ios", "desktop"),
                    path = "skills/kmp-compose-adaptive/SKILL.md"
                ),
                SkillCatalogEntry(
                    id = "kmp-ktor-networking",
                    name = "kmp-ktor-networking",
                    description = "Ktor HTTP client setup",
                    version = "1.0.0",
                    author = "jesusdmedinac",
                    tags = listOf("ktor", "networking"),
                    targets = listOf("android", "ios", "jvm", "wasm"),
                    path = "skills/kmp-ktor-networking/SKILL.md"
                )
            )
        )

        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(catalog)
        val decoded = json.decodeFromString<SkillCatalog>(jsonString)

        assertEquals("1.0.0", decoded.schemaVersion)
        assertEquals(2, decoded.skills.size)
        assertEquals("kmp-compose-adaptive", decoded.skills[0].id)
        assertEquals("kmp-ktor-networking", decoded.skills[1].id)
    }
}
