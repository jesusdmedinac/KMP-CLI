package com.jesusdmedinac.kmp.core.skill.model

import kotlinx.serialization.Serializable

@Serializable
data class SkillCatalogEntry(
    val id: String,
    val name: String,
    val description: String,
    val version: String = "1.0.0",
    val author: String? = null,
    val tags: List<String> = emptyList(),
    val targets: List<String> = emptyList(),
    val path: String? = null,
    val url: String? = null,
)

@Serializable
data class SkillCatalog(
    val schemaVersion: String = "1.0.0",
    val generatedAt: String? = null,
    val skills: List<SkillCatalogEntry> = emptyList(),
)
