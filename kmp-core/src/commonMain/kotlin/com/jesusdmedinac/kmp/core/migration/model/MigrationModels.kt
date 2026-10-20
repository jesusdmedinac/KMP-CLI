package com.jesusdmedinac.kmp.core.migration.model

import kotlinx.serialization.Serializable

@Serializable
enum class LegacyFramework {
    REACT_NATIVE,
    FLUTTER,
    UNKNOWN,
}

@Serializable
enum class ArchitectureType {
    OLD_BRIDGE,
    NEW_ARCHITECTURE_TURBOMODULES,
    FLUTTER_PLATFORM_CHANNELS,
    STANDARD,
}

@Serializable
enum class ModernizationPathway {
    DUAL_NATIVE,
    NATIVE_UI_SHARED_CORE,
    COMPOSE_MULTIPLATFORM,
}

@Serializable
data class DependencyMapping(
    val legacyName: String,
    val legacyVersion: String? = null,
    val category: String,
    val kmpEquivalent: String,
    val recommendationReason: String,
    val migrationEffort: String = "MEDIUM",
)

@Serializable
data class MigrationAuditReport(
    val framework: LegacyFramework,
    val frameworkVersion: String? = null,
    val architecture: ArchitectureType,
    val projectPath: String,
    val platformTargets: List<String> = emptyList(),
    val dependencies: List<DependencyMapping> = emptyList(),
    val recommendedPathway: ModernizationPathway,
    val pathwayRationale: String,
    val compatibilityScore: Int,
    val riskFactors: List<String> = emptyList(),
)

@Serializable
data class MigrationPhase(
    val phaseNumber: Int,
    val name: String,
    val objectives: List<String>,
    val deliverables: List<String>,
    val recommendedSkills: List<String> = emptyList(),
)

@Serializable
data class MigrationPlan(
    val framework: LegacyFramework,
    val pathway: ModernizationPathway,
    val projectPath: String,
    val phases: List<MigrationPhase>,
)

@Serializable
data class LegacyDetectionResult(
    val detected: Boolean,
    val framework: LegacyFramework,
    val signatureFile: String,
    val suggestedCommand: String = "kmp migrate audit",
    val message: String,
)
