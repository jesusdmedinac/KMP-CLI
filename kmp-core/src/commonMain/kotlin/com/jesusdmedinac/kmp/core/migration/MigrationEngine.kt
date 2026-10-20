package com.jesusdmedinac.kmp.core.migration

import com.jesusdmedinac.kmp.core.migration.model.ArchitectureType
import com.jesusdmedinac.kmp.core.migration.model.DependencyMapping
import com.jesusdmedinac.kmp.core.migration.model.LegacyDetectionResult
import com.jesusdmedinac.kmp.core.migration.model.LegacyFramework
import com.jesusdmedinac.kmp.core.migration.model.MigrationAuditReport
import com.jesusdmedinac.kmp.core.migration.model.MigrationExecuteOptions
import com.jesusdmedinac.kmp.core.migration.model.MigrationExecuteResult
import com.jesusdmedinac.kmp.core.migration.model.MigrationLayer
import com.jesusdmedinac.kmp.core.migration.model.MigrationLayerFile
import com.jesusdmedinac.kmp.core.migration.model.MigrationManifest
import com.jesusdmedinac.kmp.core.migration.model.MigrationPhase
import com.jesusdmedinac.kmp.core.migration.model.MigrationPlan
import com.jesusdmedinac.kmp.core.migration.model.MigrationStrategy
import com.jesusdmedinac.kmp.core.migration.model.ModernizationPathway
import com.jesusdmedinac.kmp.core.scaffold.ScaffoldingEngine
import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MigrationEngine(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun detectLegacyFramework(path: String = "."): LegacyDetectionResult {
        val root = normalizePath(path)
        val packageJsonPath = "${root}package.json"
        val pubspecPath = "${root}pubspec.yaml"

        if (systemEnvironment.fileExists(packageJsonPath)) {
            val content = systemEnvironment.readFileText(packageJsonPath) ?: ""
            if (content.contains("\"react-native\"")) {
                return LegacyDetectionResult(
                    detected = true,
                    framework = LegacyFramework.REACT_NATIVE,
                    signatureFile = packageJsonPath,
                    suggestedCommand = "kmp migrate audit",
                    message = "Detected React Native codebase signature in '$packageJsonPath'. Recommend running 'kmp migrate audit' to evaluate KMP migration.",
                )
            }
        }

        if (systemEnvironment.fileExists(pubspecPath)) {
            val content = systemEnvironment.readFileText(pubspecPath) ?: ""
            if (content.contains("flutter:") || content.contains("sdk: flutter")) {
                return LegacyDetectionResult(
                    detected = true,
                    framework = LegacyFramework.FLUTTER,
                    signatureFile = pubspecPath,
                    suggestedCommand = "kmp migrate audit",
                    message = "Detected Flutter codebase signature in '$pubspecPath'. Recommend running 'kmp migrate audit' to evaluate KMP migration.",
                )
            }
        }

        return LegacyDetectionResult(
            detected = false,
            framework = LegacyFramework.UNKNOWN,
            signatureFile = "",
            suggestedCommand = "",
            message = "No legacy cross-platform framework signature detected.",
        )
    }

    fun audit(path: String = "."): MigrationAuditReport {
        val root = normalizePath(path)
        val detection = detectLegacyFramework(root)

        return when (detection.framework) {
            LegacyFramework.REACT_NATIVE -> auditReactNative(root)
            LegacyFramework.FLUTTER -> auditFlutter(root)
            LegacyFramework.UNKNOWN -> {
                MigrationAuditReport(
                    framework = LegacyFramework.UNKNOWN,
                    frameworkVersion = null,
                    architecture = ArchitectureType.STANDARD,
                    projectPath = path,
                    platformTargets = emptyList(),
                    dependencies = emptyList(),
                    recommendedPathway = ModernizationPathway.COMPOSE_MULTIPLATFORM,
                    pathwayRationale = "No legacy cross-platform signatures found. Target can proceed directly with standard KMP.",
                    compatibilityScore = 100,
                    riskFactors = listOf("No package.json or pubspec.yaml found in target path."),
                )
            }
        }
    }

    private fun auditReactNative(root: String): MigrationAuditReport {
        val packageJsonContent = systemEnvironment.readFileText("${root}package.json") ?: "{}"
        val parsedJson = try {
            json.parseToJsonElement(packageJsonContent).jsonObject
        } catch (_: Exception) {
            JsonObject(emptyMap())
        }

        val dependencies = parsedJson["dependencies"]?.let {
            try { it.jsonObject } catch (_: Exception) { JsonObject(emptyMap()) }
        } ?: JsonObject(emptyMap())

        val devDependencies = parsedJson["devDependencies"]?.let {
            try { it.jsonObject } catch (_: Exception) { JsonObject(emptyMap()) }
        } ?: JsonObject(emptyMap())

        val allDeps = dependencies + devDependencies
        val rnVersion = allDeps["react-native"]?.jsonPrimitive?.content ?: "unknown"

        val gradleProps = systemEnvironment.readFileText("${root}android/gradle.properties") ?: ""
        val appJsonContent = systemEnvironment.readFileText("${root}app.json") ?: ""
        val isNewArch = gradleProps.contains("newArchEnabled=true") ||
                packageJsonContent.contains("\"newArchEnabled\": true") ||
                appJsonContent.contains("\"newArchEnabled\": true") ||
                packageJsonContent.contains("expo-router") ||
                isModernReactNativeVersion(rnVersion)

        val architecture = if (isNewArch) {
            ArchitectureType.NEW_ARCHITECTURE_TURBOMODULES
        } else {
            ArchitectureType.OLD_BRIDGE
        }

        val targets = mutableListOf<String>()
        if (systemEnvironment.fileExists("${root}android")) targets.add("android")
        if (systemEnvironment.fileExists("${root}ios")) targets.add("ios")
        if (systemEnvironment.fileExists("${root}windows")) targets.add("windows")
        if (systemEnvironment.fileExists("${root}macos")) targets.add("macos")
        if (targets.isEmpty()) {
            targets.addAll(listOf("android", "ios"))
        }

        val mappings = mutableListOf<DependencyMapping>()
        val riskFactors = mutableListOf<String>()

        if (architecture == ArchitectureType.OLD_BRIDGE) {
            riskFactors.add("Legacy asynchronous JSON bridge imposes frame-rate bottlenecks under heavy UI data serialization.")
        }

        for ((dep, versionElem) in allDeps) {
            val ver = try { versionElem.jsonPrimitive.content } catch (_: Exception) { null }
            val mapping = mapReactNativeDependency(dep, ver)
            if (mapping != null) {
                mappings.add(mapping)
            }
        }

        val nativeBridgeCount = mappings.count { it.category in listOf("Storage", "Navigation", "Media", "Cloud", "Database") }
        val pathway = if (nativeBridgeCount > 6 || allDeps.containsKey("react-native-camera") || allDeps.containsKey("react-native-vision-camera")) {
            ModernizationPathway.NATIVE_UI_SHARED_CORE
        } else {
            ModernizationPathway.COMPOSE_MULTIPLATFORM
        }

        val rationale = when (pathway) {
            ModernizationPathway.DUAL_NATIVE -> "Heavy hardware and divergent OS-specific design requirements warrant 100% SwiftUI and 100% Jetpack Compose."
            ModernizationPathway.NATIVE_UI_SHARED_CORE -> "Rich platform design and multiple native bridge modules benefit from isolating core business logic, persistence, and networking into a Shared KMP module while keeping native UI shells."
            ModernizationPathway.COMPOSE_MULTIPLATFORM -> "High feature parity and velocity advantage: replace JavaScript shell with a unified 100% Compose Multiplatform codebase across all targets."
        }

        val score = if (mappings.isEmpty()) 90 else {
            val highEffortCount = mappings.count { it.migrationEffort == "HIGH" }
            (95 - (highEffortCount * 5)).coerceIn(60, 95)
        }

        return MigrationAuditReport(
            framework = LegacyFramework.REACT_NATIVE,
            frameworkVersion = rnVersion,
            architecture = architecture,
            projectPath = root,
            platformTargets = targets,
            dependencies = mappings,
            recommendedPathway = pathway,
            pathwayRationale = rationale,
            compatibilityScore = score,
            riskFactors = riskFactors,
        )
    }

    private fun auditFlutter(root: String): MigrationAuditReport {
        val pubspecContent = systemEnvironment.readFileText("${root}pubspec.yaml") ?: ""
        val targets = mutableListOf<String>()
        if (systemEnvironment.fileExists("${root}android")) targets.add("android")
        if (systemEnvironment.fileExists("${root}ios")) targets.add("ios")
        if (systemEnvironment.fileExists("${root}web")) targets.add("web")
        if (systemEnvironment.fileExists("${root}macos")) targets.add("macos")
        if (systemEnvironment.fileExists("${root}linux")) targets.add("linux")
        if (systemEnvironment.fileExists("${root}windows")) targets.add("windows")
        if (targets.isEmpty()) {
            targets.addAll(listOf("android", "ios"))
        }

        val sdkLine = pubspecContent.lines().find { it.trim().startsWith("sdk:") }?.trim() ?: "sdk: >=3.0.0"
        val flutterVersion = sdkLine.substringAfter("sdk:").trim().trim('"', '\'')

        val mappings = mutableListOf<DependencyMapping>()
        val riskFactors = mutableListOf<String>()

        val hasMethodChannels = pubspecContent.contains("pluginClass") ||
                systemEnvironment.listFilesRecursively(root).any { it.endsWith(".dart") && (systemEnvironment.readFileText(it)?.contains("MethodChannel") == true) }

        val architecture = if (hasMethodChannels) {
            riskFactors.add("Platform Channels (MethodChannel/Pigeon) detect bespoke native bridge invocations requiring expect/actual translation.")
            ArchitectureType.FLUTTER_PLATFORM_CHANNELS
        } else {
            ArchitectureType.STANDARD
        }

        var inDependencies = false
        for (line in pubspecContent.lines()) {
            val trimmed = line.trim()
            if (trimmed == "dependencies:") {
                inDependencies = true
                continue
            }
            if (inDependencies && (trimmed.startsWith("dev_dependencies:") || (line.isNotEmpty() && !line.startsWith(" ") && !line.startsWith("\t")))) {
                inDependencies = false
            }
            if (inDependencies && trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val depName = trimmed.substringBefore(":").trim()
                val depVer = trimmed.substringAfter(":", "").trim().takeIf { it.isNotEmpty() }
                val mapping = mapFlutterDependency(depName, depVer)
                if (mapping != null) {
                    mappings.add(mapping)
                }
            }
        }

        val pathway = ModernizationPathway.COMPOSE_MULTIPLATFORM
        val rationale = "Flutter's widget-tree reactive paradigm maps cleanly 1:1 to Compose Multiplatform composables and Coroutines Flows with zero framework impedance mismatch."

        val score = if (mappings.isEmpty()) 90 else {
            val highEffortCount = mappings.count { it.migrationEffort == "HIGH" }
            (95 - (highEffortCount * 5)).coerceIn(65, 95)
        }

        return MigrationAuditReport(
            framework = LegacyFramework.FLUTTER,
            frameworkVersion = flutterVersion,
            architecture = architecture,
            projectPath = root,
            platformTargets = targets,
            dependencies = mappings,
            recommendedPathway = pathway,
            pathwayRationale = rationale,
            compatibilityScore = score,
            riskFactors = riskFactors,
        )
    }

    private fun isModernReactNativeVersion(version: String): Boolean {
        val cleanVersion = version.trimStart('^', '~', 'v')
        val parts = cleanVersion.split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return major >= 1 || (major == 0 && minor >= 76)
    }

    private fun mapReactNativeDependency(name: String, version: String?): DependencyMapping? {
        return when {
            name == "expo-router" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Navigation",
                kmpEquivalent = "kotlin-ui-compose-adaptive",
                recommendationReason = "Replace file-based Expo Router with Compose Multiplatform navigation and adaptive layouts.",
                migrationEffort = "MEDIUM",
            )
            name == "expo-image" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Media",
                kmpEquivalent = "kotlin-media-coil",
                recommendationReason = "Replace expo-image with Coil 3 AsyncImage for unified multiplatform image loading and caching.",
                migrationEffort = "LOW",
            )
            name == "expo-sqlite" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Database",
                kmpEquivalent = "kotlin-data-sqldelight-database",
                recommendationReason = "Replace expo-sqlite with SQLDelight 2.x or Room KMP for reactive multiplatform SQLite persistence.",
                migrationEffort = "MEDIUM",
            )
            name == "expo-secure-store" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Storage",
                kmpEquivalent = "kotlin-data-multiplatform-settings",
                recommendationReason = "Replace expo-secure-store with Multiplatform-Settings keychain/keystore binding.",
                migrationEffort = "LOW",
            )
            name == "react-native-screens" || name == "react-native-safe-area-context" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "UI / Insets",
                kmpEquivalent = "kotlin-ui-compose-adaptive",
                recommendationReason = "Compose Multiplatform handles native window insets, IME, and screens natively without bridge libraries.",
                migrationEffort = "LOW",
            )
            name == "@react-native-async-storage/async-storage" || name == "react-native-async-storage" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Storage",
                kmpEquivalent = "kotlin-data-multiplatform-settings",
                recommendationReason = "Replace AsyncStorage with Multiplatform-Settings for reactive key-value persistence.",
                migrationEffort = "LOW",
            )
            name == "axios" || name == "whatwg-fetch" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Networking",
                kmpEquivalent = "kotlin-networking-ktor",
                recommendationReason = "Replace JS HTTP client with Ktor Client for coroutine-powered asynchronous networking.",
                migrationEffort = "LOW",
            )
            name == "redux" || name == "@reduxjs/toolkit" || name == "zustand" || name == "mobx" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "State",
                kmpEquivalent = "kotlin-architecture-orbit-mvi",
                recommendationReason = "Migrate Redux/Zustand reducers to Orbit MVI ContainerHost with type-safe Coroutines StateFlow.",
                migrationEffort = "MEDIUM",
            )
            name.startsWith("@react-navigation/") || name == "react-native-navigation" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Navigation",
                kmpEquivalent = "kotlin-ui-compose-adaptive",
                recommendationReason = "Replace JavaScript navigation routers with Jetpack Navigation 3 and adaptive Compose backstacks.",
                migrationEffort = "MEDIUM",
            )
            name == "react-native-fast-image" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Media",
                kmpEquivalent = "kotlin-media-coil",
                recommendationReason = "Replace FastImage with Coil 3 AsyncImage for multiplatform memory and disk image caching.",
                migrationEffort = "LOW",
            )
            name.startsWith("@react-native-firebase/") || name == "react-native-firebase" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Cloud",
                kmpEquivalent = "kotlin-cloud-firebase",
                recommendationReason = "Replace native React Native Firebase bridge with GitLive Firebase Kotlin SDK in commonMain.",
                migrationEffort = "LOW",
            )
            name == "realm" || name == "realm-web" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Database",
                kmpEquivalent = "kotlin-data-room-kmp",
                recommendationReason = "Migrate Realm models to AndroidX Room KMP SQLite entities and DAOs.",
                migrationEffort = "HIGH",
            )
            name == "react-native-sqlite-storage" || name == "react-native-quick-sqlite" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Database",
                kmpEquivalent = "kotlin-data-sqldelight-database",
                recommendationReason = "Replace custom bridge SQLite queries with type-safe SQLDelight 2.x schema code generation.",
                migrationEffort = "MEDIUM",
            )
            name == "@tanstack/react-query" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Data Sync",
                kmpEquivalent = "kotlin-data-store5",
                recommendationReason = "Replace React Query cache orchestration with Store5 offline-first single source of truth.",
                migrationEffort = "LOW",
            )
            name == "react-native-reanimated" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Animation",
                kmpEquivalent = "androidx.compose.animation",
                recommendationReason = "Replace Reanimated worklets with declarative Compose Multiplatform transition and spring specs.",
                migrationEffort = "MEDIUM",
            )
            else -> null
        }
    }

    private fun mapFlutterDependency(name: String, version: String?): DependencyMapping? {
        return when {
            name == "sqflite" || name == "floor" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Database",
                kmpEquivalent = "kotlin-data-sqldelight-database",
                recommendationReason = "Replace Dart SQLite binding with SQLDelight 2.x or Room KMP for reactive Flow queries.",
                migrationEffort = "MEDIUM",
            )
            name == "shared_preferences" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Storage",
                kmpEquivalent = "kotlin-data-multiplatform-settings",
                recommendationReason = "Replace Flutter SharedPreferences plugin with Multiplatform-Settings ObservableSettings.",
                migrationEffort = "LOW",
            )
            name == "http" || name == "dio" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Networking",
                kmpEquivalent = "kotlin-networking-ktor",
                recommendationReason = "Migrate Dio/http interceptors and client to Ktor Client with CIO/Darwin engines.",
                migrationEffort = "LOW",
            )
            name == "flutter_bloc" || name == "bloc" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "State",
                kmpEquivalent = "kotlin-architecture-orbit-mvi",
                recommendationReason = "BLoC events and states map 1:1 to Orbit MVI ContainerHost intents and pure state reduction.",
                migrationEffort = "LOW",
            )
            name == "provider" || name == "riverpod" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "State",
                kmpEquivalent = "kotlin-concurrency-coroutines",
                recommendationReason = "Replace Riverpod providers with Coroutines StateFlow and ViewModel lifecycle state holders.",
                migrationEffort = "LOW",
            )
            name == "cached_network_image" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Media",
                kmpEquivalent = "kotlin-media-coil",
                recommendationReason = "Replace CachedNetworkImage widget with Coil 3 AsyncImage for Compose Multiplatform.",
                migrationEffort = "LOW",
            )
            name == "firebase_core" || name == "firebase_auth" || name == "cloud_firestore" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Cloud",
                kmpEquivalent = "kotlin-cloud-firebase",
                recommendationReason = "Replace FlutterFire plugins with GitLive Firebase SDK coroutine flows in commonMain.",
                migrationEffort = "LOW",
            )
            name == "go_router" || name == "auto_route" -> DependencyMapping(
                legacyName = name,
                legacyVersion = version,
                category = "Navigation",
                kmpEquivalent = "kotlin-ui-compose-adaptive",
                recommendationReason = "Migrate GoRouter declarative route hierarchy to Jetpack Navigation 3 backstack.",
                migrationEffort = "MEDIUM",
            )
            else -> null
        }
    }

    fun plan(path: String = ".", overridePathway: ModernizationPathway? = null): MigrationPlan {
        val auditReport = audit(path)
        val selectedPathway = overridePathway ?: auditReport.recommendedPathway

        val phases = listOf(
            MigrationPhase(
                phaseNumber = 1,
                name = "Foundation & Core Bridge",
                objectives = listOf(
                    "Setup shared KMP library skeleton using 'kmp create --template kmp-shared-core-bridge'.",
                    "Export CocoaPods / Swift Package Manager (SPM) framework for iOS and Maven Local / AAR for Android.",
                    "Unify networking (Ktor) and authentication in commonMain; consume from existing legacy shell.",
                ),
                deliverables = listOf(
                    ":shared KMP module with automated target compilation.",
                    "Bridge wrapper exposing suspend functions as Swift AsyncSequence and Android coroutine jobs.",
                ),
                recommendedSkills = listOf("kmp-shared-core-bridge", "kotlin-networking-ktor", "kotlin-concurrency-coroutines"),
            ),
            MigrationPhase(
                phaseNumber = 2,
                name = "Domain & Data Strangler",
                objectives = listOf(
                    "Migrate local persistence (Room KMP or SQLDelight) into shared KMP module.",
                    "Unify offline repository and reactive caching with Store5.",
                    "Strangle legacy mobile state stores (Redux / BLoC) by observing KMP StateFlow.",
                ),
                deliverables = listOf(
                    "Single Source of Truth database running locally on Android and iOS.",
                    "Zero duplicated data models between platforms.",
                ),
                recommendedSkills = listOf("kotlin-data-room-kmp", "kotlin-data-sqldelight-database", "kotlin-data-multiplatform-settings", "kotlin-data-store5"),
            ),
            MigrationPhase(
                phaseNumber = 3,
                name = "UI & Screen Incremental",
                objectives = listOf(
                    "Replace high-friction or performance-critical legacy screens with Compose Multiplatform or native UI.",
                    "Embed ComposeView on Android and ComposeUIViewController on iOS inside the legacy navigation shell.",
                    "Migrate design tokens and theme palettes to shared Compose Multiplatform theme.",
                ),
                deliverables = listOf(
                    "Hybrid screen navigation with shared UI components.",
                    "Elimination of native bridge overhead on modern screens.",
                ),
                recommendedSkills = listOf("kotlin-ui-compose-adaptive", "kotlin-media-coil", "kotlin-architecture-orbit-mvi"),
            ),
            MigrationPhase(
                phaseNumber = 4,
                name = "Legacy Shell Deletion",
                objectives = listOf(
                    "Replace root application entry point with native Kotlin/Compose MainActivity and iOS MainViewController.",
                    "Remove JavaScript / Dart runtime engines, node_modules, and bridge pods.",
                    "Finalize standalone multiplatform binary compilation with sub-second startup times.",
                ),
                deliverables = listOf(
                    "100% native multiplatform application with zero bridge overhead.",
                    "Pruned build pipeline with pure Gradle or Kotlin Toolchain configuration.",
                ),
                recommendedSkills = listOf("kotlin-tooling-gradle-to-kotlin-toolchain", "kmp-cli"),
            )
        )

        return MigrationPlan(
            framework = auditReport.framework,
            pathway = selectedPathway,
            projectPath = auditReport.projectPath,
            phases = phases,
        )
    }

    fun execute(options: MigrationExecuteOptions): MigrationExecuteResult {
        val root = normalizePath(options.sourcePath)
        val detection = detectLegacyFramework(root)
        if (!detection.detected) {
            return MigrationExecuteResult(
                status = "FAILED",
                sourcePath = options.sourcePath,
                outputPath = options.outputDir,
                framework = LegacyFramework.UNKNOWN,
                strategy = options.strategy,
                pathway = options.pathway ?: ModernizationPathway.COMPOSE_MULTIPLATFORM,
                errorMessage = "No legacy React Native or Flutter codebase detected at '${options.sourcePath}'.",
            )
        }

        val auditReport = audit(root)
        val chosenPathway = options.pathway ?: auditReport.recommendedPathway
        val targets = if (auditReport.platformTargets.isNotEmpty()) {
            auditReport.platformTargets
        } else {
            listOf("android", "ios", "desktop", "wasm")
        }

        val cleanProjectName = options.outputDir.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\')
        val pkg = options.packageName ?: "com.example.${cleanProjectName.lowercase().replace("-", "").replace("_", "")}"

        val scaffoldingEngine = ScaffoldingEngine(systemEnvironment)
        val scaffoldOptions = ScaffoldingOptions(
            name = cleanProjectName,
            packageName = pkg,
            template = ProjectTemplate.SHARED_UI,
            targets = targets,
            outputDir = options.outputDir,
        )

        val scaffoldResult = scaffoldingEngine.scaffold(scaffoldOptions)
        if (scaffoldResult.status == "FAILED") {
            return MigrationExecuteResult(
                status = "FAILED",
                sourcePath = options.sourcePath,
                outputPath = options.outputDir,
                framework = detection.framework,
                strategy = options.strategy,
                pathway = chosenPathway,
                errorMessage = "Scaffolding failed: ${scaffoldResult.errorMessage}",
            )
        }

        // Synthesize libs.versions.toml with KMP equivalents from audit
        val synthesizedDeps = mutableListOf<String>()
        val tomlPath = "${options.outputDir}/gradle/libs.versions.toml"
        val existingToml = systemEnvironment.readFileText(tomlPath) ?: ""

        val librariesToAdd = StringBuilder()

        for (dep in auditReport.dependencies) {
            val kmpEq = dep.kmpEquivalent
            if (!synthesizedDeps.contains(kmpEq)) {
                synthesizedDeps.add(kmpEq)
                when (kmpEq) {
                    "kotlin-networking-ktor" -> {
                        if (!existingToml.contains("ktor-client-core")) {
                            librariesToAdd.append("ktor-client-core = { module = \"io.ktor:ktor-client-core\", version = \"3.0.3\" }\n")
                            librariesToAdd.append("ktor-client-content-negotiation = { module = \"io.ktor:ktor-client-content-negotiation\", version = \"3.0.3\" }\n")
                            librariesToAdd.append("ktor-serialization-kotlinx-json = { module = \"io.ktor:ktor-serialization-kotlinx-json\", version = \"3.0.3\" }\n")
                        }
                    }
                    "kotlin-media-coil" -> {
                        if (!existingToml.contains("coil-compose")) {
                            librariesToAdd.append("coil-compose = { module = \"io.coil-kt.coil3:coil-compose\", version = \"3.0.4\" }\n")
                        }
                        if (!existingToml.contains("coil-network-ktor")) {
                            librariesToAdd.append("coil-network-ktor = { module = \"io.coil-kt.coil3:coil-network-ktor3\", version = \"3.0.4\" }\n")
                        }
                    }
                    "kotlin-data-multiplatform-settings" -> {
                        if (!existingToml.contains("multiplatform-settings")) {
                            librariesToAdd.append("multiplatform-settings = { module = \"com.russhwolf:multiplatform-settings\", version = \"1.3.0\" }\n")
                        }
                    }
                    "kotlin-data-room-kmp" -> {
                        if (!existingToml.contains("androidx-room-runtime")) {
                            librariesToAdd.append("androidx-room-runtime = { module = \"androidx.room:room-runtime\", version = \"2.7.0-alpha13\" }\n")
                        }
                    }
                    "kotlin-data-sqldelight-database" -> {
                        if (!existingToml.contains("sqldelight-runtime")) {
                            librariesToAdd.append("sqldelight-runtime = { module = \"app.cash.sqldelight:runtime\", version = \"2.0.2\" }\n")
                        }
                    }
                    "kotlin-architecture-orbit-mvi" -> {
                        if (!existingToml.contains("orbit-core")) {
                            librariesToAdd.append("orbit-core = { module = \"org.orbit-mvi:orbit-core\", version = \"9.0.0\" }\n")
                            librariesToAdd.append("orbit-compose = { module = \"org.orbit-mvi:orbit-compose\", version = \"9.0.0\" }\n")
                        }
                    }
                    "kotlin-data-store5" -> {
                        if (!existingToml.contains("store5")) {
                            librariesToAdd.append("store5 = { module = \"org.mobilenativefoundation.store:store5\", version = \"5.1.0\" }\n")
                        }
                    }
                }
            }
        }

        val updatedToml = if (librariesToAdd.isNotEmpty()) {
            val librariesRegex = Regex("""^\[libraries]\s*$""", RegexOption.MULTILINE)
            if (librariesRegex.containsMatchIn(existingToml)) {
                existingToml.replace(librariesRegex, "[libraries]\n$librariesToAdd".trimEnd())
            } else if (existingToml.contains("[libraries]")) {
                existingToml.replace("[libraries]", "[libraries]\n$librariesToAdd".trimEnd())
            } else {
                val prefix = if (existingToml.endsWith("\n") || existingToml.isEmpty()) existingToml else "$existingToml\n"
                "${prefix}[libraries]\n$librariesToAdd".trimEnd()
            }
        } else {
            existingToml
        }
        val finalToml = if (updatedToml.endsWith("\n")) updatedToml else "$updatedToml\n"
        systemEnvironment.writeFileText(tomlPath, finalToml)

        // Inject synthesized dependencies into build.gradle.kts (check module build script first)
        val buildGradleCandidates = listOf(
            "${options.outputDir}/composeApp/build.gradle.kts",
            "${options.outputDir}/app/build.gradle.kts",
            "${options.outputDir}/shared/build.gradle.kts",
            "${options.outputDir}/build.gradle.kts",
        )
        val targetBuildGradle = buildGradleCandidates.firstOrNull { candidate ->
            systemEnvironment.fileExists(candidate) &&
                (systemEnvironment.readFileText(candidate)?.contains("commonMain.dependencies {") == true)
        } ?: "${options.outputDir}/build.gradle.kts"

        val existingBuildGradle = systemEnvironment.readFileText(targetBuildGradle) ?: ""
        if (existingBuildGradle.contains("commonMain.dependencies {")) {
            val depInjections = StringBuilder()
            if (synthesizedDeps.contains("kotlin-networking-ktor")) {
                if (!existingBuildGradle.contains("libs.ktor.client.core")) {
                    depInjections.append("            implementation(libs.ktor.client.core)\n")
                }
                if (!existingBuildGradle.contains("libs.ktor.client.content.negotiation")) {
                    depInjections.append("            implementation(libs.ktor.client.content.negotiation)\n")
                }
                if (!existingBuildGradle.contains("libs.ktor.serialization.kotlinx.json")) {
                    depInjections.append("            implementation(libs.ktor.serialization.kotlinx.json)\n")
                }
            }
            if (synthesizedDeps.contains("kotlin-media-coil")) {
                if (!existingBuildGradle.contains("libs.coil.compose")) {
                    depInjections.append("            implementation(libs.coil.compose)\n")
                }
                if (!existingBuildGradle.contains("libs.coil.network.ktor")) {
                    depInjections.append("            implementation(libs.coil.network.ktor)\n")
                }
            }
            if (synthesizedDeps.contains("kotlin-data-multiplatform-settings")) {
                if (!existingBuildGradle.contains("libs.multiplatform.settings")) {
                    depInjections.append("            implementation(libs.multiplatform.settings)\n")
                }
            }
            if (synthesizedDeps.contains("kotlin-data-room-kmp")) {
                if (!existingBuildGradle.contains("libs.androidx.room.runtime")) {
                    depInjections.append("            implementation(libs.androidx.room.runtime)\n")
                }
            }
            if (synthesizedDeps.contains("kotlin-data-sqldelight-database")) {
                if (!existingBuildGradle.contains("libs.sqldelight.runtime")) {
                    depInjections.append("            implementation(libs.sqldelight.runtime)\n")
                }
            }
            if (synthesizedDeps.contains("kotlin-architecture-orbit-mvi")) {
                if (!existingBuildGradle.contains("libs.orbit.core")) {
                    depInjections.append("            implementation(libs.orbit.core)\n")
                }
                if (!existingBuildGradle.contains("libs.orbit.compose")) {
                    depInjections.append("            implementation(libs.orbit.compose)\n")
                }
            }
            if (synthesizedDeps.contains("kotlin-data-store5")) {
                if (!existingBuildGradle.contains("libs.store5")) {
                    depInjections.append("            implementation(libs.store5)\n")
                }
            }

            if (depInjections.isNotEmpty()) {
                val updatedBuildGradle = existingBuildGradle.replace(
                    "commonMain.dependencies {",
                    "commonMain.dependencies {\n$depInjections",
                )
                systemEnvironment.writeFileText(targetBuildGradle, updatedBuildGradle)
            }
        }

        // Migrate Assets
        var assetCount = 0
        val drawableDest = "${options.outputDir}/src/commonMain/composeResources/drawable"
        val fontDest = "${options.outputDir}/src/commonMain/composeResources/font"

        val assetSearchDirs = listOf(
            "${root}assets/images",
            "${root}assets/icons",
            "${root}assets/fonts",
            "${root}assets",
            "${root}images",
        )

        val processedAssetNames = mutableSetOf<String>()
        for (dir in assetSearchDirs) {
            val relativeFiles = systemEnvironment.listFilesRecursively(dir)
            val cleanDir = if (dir.endsWith("/")) dir.dropLast(1) else dir
            for (relFile in relativeFiles) {
                val fullPath = "$cleanDir/$relFile"
                val fileName = relFile.substringAfterLast('/').substringAfterLast('\\')
                if (!processedAssetNames.contains(fileName)) {
                    val ext = fileName.substringAfterLast('.', "").lowercase()
                    if (ext in listOf("png", "jpg", "jpeg", "webp", "svg")) {
                        val destPath = "$drawableDest/$fileName"
                        systemEnvironment.copyFile(fullPath, destPath)
                        processedAssetNames.add(fileName)
                        assetCount++
                    } else if (ext in listOf("ttf", "otf")) {
                        val destPath = "$fontDest/$fileName"
                        systemEnvironment.copyFile(fullPath, destPath)
                        processedAssetNames.add(fileName)
                        assetCount++
                    }
                }
            }
        }

        // Build Migration Manifest & Layers
        val layer1Models = mutableListOf<MigrationLayerFile>()
        val layer2Data = mutableListOf<MigrationLayerFile>()
        val layer3State = mutableListOf<MigrationLayerFile>()
        val layer4Ui = mutableListOf<MigrationLayerFile>()

        fun categorizeAndAdd(fullPath: String, relPath: String) {
            val lower = relPath.lowercase()
            val fileName = relPath.substringAfterLast('/').substringAfterLast('\\')
            val baseName = fileName.substringBeforeLast('.')

            when {
                lower.contains("models/") || lower.contains("types/") || lower.contains("entities/") -> {
                    layer1Models.add(
                        MigrationLayerFile(
                            source = relPath,
                            target = "src/commonMain/kotlin/${pkg.replace('.', '/')}/model/$baseName.kt",
                            category = "MODEL",
                        )
                    )
                }
                lower.contains("services/") || lower.contains("api/") || lower.contains("repositories/") || lower.contains("network/") -> {
                    layer2Data.add(
                        MigrationLayerFile(
                            source = relPath,
                            target = "src/commonMain/kotlin/${pkg.replace('.', '/')}/data/$baseName.kt",
                            category = "DATA",
                        )
                    )
                }
                lower.contains("hooks/") || lower.contains("store/") || lower.contains("bloc/") || lower.contains("redux/") || lower.contains("cubit/") -> {
                    layer3State.add(
                        MigrationLayerFile(
                            source = relPath,
                            target = "src/commonMain/kotlin/${pkg.replace('.', '/')}/state/${baseName}ViewModel.kt",
                            category = "STATE",
                        )
                    )
                }
                lower.contains("screens/") || lower.contains("components/") || lower.contains("widgets/") || lower.contains("views/") ||
                lower.startsWith("app/") || lower.endsWith("main.dart") || lower.endsWith("index.tsx") || lower.endsWith("app.tsx") -> {
                    layer4Ui.add(
                        MigrationLayerFile(
                            source = relPath,
                            target = "src/commonMain/kotlin/${pkg.replace('.', '/')}/ui/${baseName}Screen.kt",
                            category = "UI",
                        )
                    )
                }
            }
        }

        val srcDirsToScan = listOf(
            "${root}src",
            "${root}app",
            "${root}lib",
        )
        for (scanDir in srcDirsToScan) {
            val relativeFiles = systemEnvironment.listFilesRecursively(scanDir)
            val cleanDir = if (scanDir.endsWith("/")) scanDir.dropLast(1) else scanDir
            for (relFile in relativeFiles) {
                val fullPath = "$cleanDir/$relFile"
                val sourceRelPath = if (fullPath.startsWith(root)) fullPath.removePrefix(root) else fullPath
                categorizeAndAdd(fullPath, sourceRelPath)
            }
        }

        val manifest = MigrationManifest(
            sourceFramework = detection.framework,
            sourcePath = options.sourcePath,
            outputPath = options.outputDir,
            pathway = chosenPathway,
            strategy = options.strategy,
            layers = listOf(
                MigrationLayer(1, "Domain Models", layer1Models),
                MigrationLayer(2, "Data Sources & Networking", layer2Data),
                MigrationLayer(3, "State Management & ViewModels", layer3State),
                MigrationLayer(4, "UI Screens & Components", layer4Ui),
            ),
        )

        val manifestPath = "${options.outputDir}/migration-manifest.json"
        systemEnvironment.writeFileText(manifestPath, json.encodeToString(manifest))

        val progressMd = StringBuilder()
        progressMd.append("# Migration Progress Roadmap: ${options.outputDir}\n\n")
        progressMd.append("Migrating from **${detection.framework}** (`${options.sourcePath}`) to **Compose Multiplatform**\n\n")
        progressMd.append("Strategy: `${options.strategy.name.lowercase()}` | Pathway: `${chosenPathway.name}`\n\n")

        for (layer in manifest.layers) {
            progressMd.append("## Layer ${layer.layerNumber}: ${layer.name}\n")
            if (layer.files.isEmpty()) {
                progressMd.append("- _No files detected in this layer._\n\n")
            } else {
                for (f in layer.files) {
                    progressMd.append("- [ ] `${f.source}` -> `${f.target}`\n")
                }
                progressMd.append("\n")
            }
        }
        systemEnvironment.writeFileText("${options.outputDir}/MIGRATION_PROGRESS.md", progressMd.toString())

        return MigrationExecuteResult(
            status = "SUCCESS",
            sourcePath = options.sourcePath,
            outputPath = options.outputDir,
            framework = detection.framework,
            strategy = options.strategy,
            pathway = chosenPathway,
            synthesizedDependencies = synthesizedDeps,
            migratedAssetsCount = assetCount,
            manifestPath = manifestPath,
        )
    }

    private fun normalizePath(path: String): String {
        if (path == "." || path.isEmpty()) return ""
        return if (path.endsWith("/")) path else "$path/"
    }
}
