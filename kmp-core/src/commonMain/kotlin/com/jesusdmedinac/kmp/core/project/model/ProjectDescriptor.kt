package com.jesusdmedinac.kmp.core.project.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectDescriptor(
    val name: String,
    val buildSystem: BuildSystem,
    val kotlinVersion: String? = null,
    val targets: List<String> = emptyList(),
    val modules: List<ProjectModule> = emptyList(),
    val dependencies: ProjectDependencies? = null,
)
