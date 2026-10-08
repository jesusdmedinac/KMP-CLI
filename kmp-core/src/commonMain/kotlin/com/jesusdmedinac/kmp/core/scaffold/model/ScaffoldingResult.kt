package com.jesusdmedinac.kmp.core.scaffold.model

import kotlinx.serialization.Serializable

@Serializable
data class ScaffoldingResult(
    val status: String,
    val template: String,
    val projectPath: String,
    val createdFiles: List<String> = emptyList(),
    val errorMessage: String? = null,
)
