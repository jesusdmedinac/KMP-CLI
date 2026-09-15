package com.jesusdmedinac.kmp.core.skill.model

import kotlinx.serialization.Serializable

@Serializable
data class SkillMetadata(
    val name: String,
    val description: String,
    val version: String = "1.0.0",
    val author: String? = null,
    val tags: List<String> = emptyList(),
    val targets: List<String> = emptyList(),
    val compatibility: SkillCompatibility = SkillCompatibility(),
    val triggers: List<String> = emptyList(),
)
