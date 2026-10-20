package com.jesusdmedinac.kmp.core.klibs.model

import kotlinx.serialization.Serializable

@Serializable
data class KlibsLibraryInfo(
    val coordinate: String,
    val name: String,
    val version: String,
    val description: String,
    val status: String,
    val supportedTargets: List<String>,
    val category: String,
    val docUrl: String? = null,
)

@Serializable
data class VerifiedLibrary(
    val coordinate: String,
    val name: String,
    val version: String,
    val status: String,
    val supportedTargets: List<String>,
    val compatibleWithProject: Boolean,
    val missingTargets: List<String> = emptyList(),
    val category: String,
    val description: String,
    val docUrl: String? = null,
)

@Serializable
data class LibraryRecommendationResult(
    val query: String,
    val projectTargets: List<String>,
    val libraries: List<VerifiedLibrary>,
)
