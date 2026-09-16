package com.jesusdmedinac.kmp.core.project.model

import kotlinx.serialization.Serializable

@Serializable
data class CatalogLibrary(
    val alias: String,
    val group: String? = null,
    val name: String? = null,
    val module: String? = null,
    val version: String? = null,
)

@Serializable
data class CatalogPlugin(
    val alias: String,
    val id: String,
    val version: String? = null,
)

@Serializable
data class ProjectDependencies(
    val libraries: List<CatalogLibrary> = emptyList(),
    val plugins: List<CatalogPlugin> = emptyList(),
    val versions: Map<String, String> = emptyMap(),
)
