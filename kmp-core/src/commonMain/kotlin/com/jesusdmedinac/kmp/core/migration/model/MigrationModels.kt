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

@Serializable
enum class MigrationStrategy {
    AUTONOMOUS,
    GUIDED,
}

@Serializable
data class MigrationLayerFile(
    val source: String,
    val target: String,
    val category: String,
    val status: String = "PENDING",
)

@Serializable
data class MigrationLayer(
    val layerNumber: Int,
    val name: String,
    val files: List<MigrationLayerFile> = emptyList(),
)

@Serializable
data class MigrationManifest(
    val sourceFramework: LegacyFramework,
    val sourcePath: String,
    val outputPath: String,
    val pathway: ModernizationPathway,
    val strategy: MigrationStrategy,
    val layers: List<MigrationLayer> = emptyList(),
)

@Serializable
data class MigrationExecuteOptions(
    val sourcePath: String,
    val outputDir: String,
    val packageName: String? = null,
    val pathway: ModernizationPathway? = null,
    val strategy: MigrationStrategy = MigrationStrategy.AUTONOMOUS,
)

@Serializable
data class MigrationExecuteResult(
    val status: String,
    val sourcePath: String,
    val outputPath: String,
    val framework: LegacyFramework,
    val strategy: MigrationStrategy,
    val pathway: ModernizationPathway,
    val synthesizedDependencies: List<String> = emptyList(),
    val migratedAssetsCount: Int = 0,
    val manifestPath: String? = null,
    val errorMessage: String? = null,
)
