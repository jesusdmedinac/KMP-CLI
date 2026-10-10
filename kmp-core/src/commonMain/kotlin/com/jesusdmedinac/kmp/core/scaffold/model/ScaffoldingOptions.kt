package com.jesusdmedinac.kmp.core.scaffold.model

import kotlinx.serialization.Serializable

@Serializable
data class ScaffoldingOptions(
    val name: String,
    val packageName: String = "com.example.${name.lowercase().replace("-", "").replace("_", "")}",
    val template: ProjectTemplate = ProjectTemplate.SHARED_UI,
    val targets: List<String> = listOf("android", "ios", "desktop", "wasm"),
    val outputDir: String = name,
)
