package com.jesusdmedinac.kmp.core.skill.model

import kotlinx.serialization.Serializable

@Serializable
data class SkillManifest(
    val metadata: SkillMetadata,
    val content: String,
) {
    val id: String get() = metadata.name
    val summary: String get() = metadata.description
}
