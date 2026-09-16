package com.jesusdmedinac.kmp.core.project.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectModule(
    val name: String,
    val path: String,
    val targets: List<String> = emptyList(),
)
