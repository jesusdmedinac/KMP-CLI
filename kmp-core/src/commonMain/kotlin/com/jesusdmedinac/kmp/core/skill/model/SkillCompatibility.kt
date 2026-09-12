package com.jesusdmedinac.kmp.core.skill.model

import kotlinx.serialization.Serializable

@Serializable
data class SkillCompatibility(
    val kotlin: String? = null,
    val compose: String? = null,
    val gradle: String? = null,
    val toolchain: String? = null,
)
