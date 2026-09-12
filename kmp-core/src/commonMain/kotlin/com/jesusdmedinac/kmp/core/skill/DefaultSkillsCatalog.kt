package com.jesusdmedinac.kmp.core.skill

import com.jesusdmedinac.kmp.core.skill.model.SkillCatalog
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry

object DefaultSkillsCatalog {
    val catalog: SkillCatalog = SkillCatalog(
        schemaVersion = "1.0.0",
        generatedAt = "2026-09-11T20:00:00Z",
        skills = listOf(
            SkillCatalogEntry(
                id = "kmp-compose-adaptive",
                name = "kmp-compose-adaptive",
                description = "Responsive and adaptive UI patterns for Compose Multiplatform across Android, iOS, Desktop, and Web.",
                version = "1.0.0",
                author = "jesusdmedinac",
                tags = listOf("compose", "ui", "adaptive", "layout", "navigation"),
                targets = listOf("android", "ios", "desktop", "wasm"),
                path = "skills/kmp-compose-adaptive/SKILL.md",
            ),
            SkillCatalogEntry(
                id = "kmp-ktor-networking",
                name = "kmp-ktor-networking",
                description = "Multiplatform HTTP client architecture with Ktor, native engine selection, and JSON serialization.",
                version = "1.0.0",
                author = "jesusdmedinac",
                tags = listOf("ktor", "networking", "http", "serialization"),
                targets = listOf("android", "ios", "desktop", "wasm"),
                path = "skills/kmp-ktor-networking/SKILL.md",
            ),
            SkillCatalogEntry(
                id = "kmp-sqldelight-database",
                name = "kmp-sqldelight-database",
                description = "Multiplatform SQL persistence, reactive Flow queries, and target-specific driver setup using SQLDelight 2+.",
                version = "1.0.0",
                author = "jesusdmedinac",
                tags = listOf("database", "sql", "sqlite", "persistence", "sqldelight"),
                targets = listOf("android", "ios", "desktop", "wasm"),
                path = "skills/kmp-sqldelight-database/SKILL.md",
            ),
            SkillCatalogEntry(
                id = "kmp-toolchain-migration",
                name = "kmp-toolchain-migration",
                description = "Step-by-step procedural guide to migrate from legacy build.gradle.kts to declarative Kotlin Toolchain 0.12+ module.yaml.",
                version = "1.0.0",
                author = "jesusdmedinac",
                tags = listOf("toolchain", "amper", "gradle-migration", "module-yaml"),
                targets = listOf("android", "ios", "desktop", "wasm"),
                path = "skills/kmp-toolchain-migration/SKILL.md",
            ),
            SkillCatalogEntry(
                id = "kmp-coroutines-concurrency",
                name = "kmp-coroutines-concurrency",
                description = "Best practices for structured concurrency, background dispatchers, and StateFlow management across KMP targets.",
                version = "1.0.0",
                author = "jesusdmedinac",
                tags = listOf("coroutines", "concurrency", "flows", "async"),
                targets = listOf("android", "ios", "desktop", "wasm"),
                path = "skills/kmp-coroutines-concurrency/SKILL.md",
            ),
            SkillCatalogEntry(
                id = "kmp-cli",
                name = "kmp-cli",
                description = "Procedural guide for AI agents and human developers on using the KMP CLI for environment diagnostics, skills management, and project analysis.",
                version = "1.0.0",
                author = "jesusdmedinac",
                tags = listOf("cli", "tooling", "doctor", "skills", "diagnostics"),
                targets = listOf("android", "ios", "desktop", "wasm"),
                path = "skills/kmp-cli/SKILL.md",
            ),
        )
    )
}
