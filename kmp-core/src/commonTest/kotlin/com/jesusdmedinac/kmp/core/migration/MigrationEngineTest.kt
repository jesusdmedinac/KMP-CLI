package com.jesusdmedinac.kmp.core.migration

import com.jesusdmedinac.kmp.core.migration.model.ArchitectureType
import com.jesusdmedinac.kmp.core.migration.model.LegacyFramework
import com.jesusdmedinac.kmp.core.migration.model.ModernizationPathway
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MigrationEngineTest {

    private val fakeEnv = FakeSystemEnvironment()
    private val migrationEngine = MigrationEngine(fakeEnv)

    @Test
    fun `detectLegacyFramework detects React Native repository`() {
        fakeEnv.writeFileText(
            "package.json",
            """
            {
              "name": "LegacyApp",
              "version": "1.0.0",
              "dependencies": {
                "react": "18.2.0",
                "react-native": "0.72.6"
              }
            }
            """.trimIndent(),
        )

        val result = migrationEngine.detectLegacyFramework(".")
        assertTrue(result.detected)
        assertEquals(LegacyFramework.REACT_NATIVE, result.framework)
        assertEquals("kmp migrate audit", result.suggestedCommand)
        assertTrue(result.signatureFile.endsWith("package.json"))
    }

    @Test
    fun `detectLegacyFramework detects Flutter repository`() {
        fakeEnv.writeFileText(
            "pubspec.yaml",
            """
            name: flutter_app
            description: A sample Flutter project
            environment:
              sdk: '>=3.0.0 <4.0.0'
            dependencies:
              flutter:
                sdk: flutter
            """.trimIndent(),
        )

        val result = migrationEngine.detectLegacyFramework(".")
        assertTrue(result.detected)
        assertEquals(LegacyFramework.FLUTTER, result.framework)
        assertEquals("kmp migrate audit", result.suggestedCommand)
        assertTrue(result.signatureFile.endsWith("pubspec.yaml"))
    }

    @Test
    fun `audit on React Native maps dependencies and identifies architecture`() {
        fakeEnv.writeFileText(
            "package.json",
            """
            {
              "name": "LegacyRNApp",
              "dependencies": {
                "react-native": "0.72.6",
                "@react-native-async-storage/async-storage": "^1.19.0",
                "axios": "^1.4.0",
                "redux": "^4.2.1",
                "@react-navigation/native": "^6.1.7",
                "react-native-fast-image": "^8.6.3",
                "@tanstack/react-query": "^4.29.0"
              }
            }
            """.trimIndent(),
        )
        fakeEnv.addFile("android")
        fakeEnv.addFile("ios")

        val report = migrationEngine.audit(".")
        assertEquals(LegacyFramework.REACT_NATIVE, report.framework)
        assertEquals("0.72.6", report.frameworkVersion)
        assertEquals(ArchitectureType.OLD_BRIDGE, report.architecture)
        assertTrue(report.platformTargets.contains("android"))
        assertTrue(report.platformTargets.contains("ios"))

        // Verify dependency replacements
        val mappedEquivalents = report.dependencies.map { it.kmpEquivalent }
        assertTrue(mappedEquivalents.contains("kotlin-data-multiplatform-settings"))
        assertTrue(mappedEquivalents.contains("kotlin-networking-ktor"))
        assertTrue(mappedEquivalents.contains("kotlin-architecture-orbit-mvi"))
        assertTrue(mappedEquivalents.contains("kotlin-ui-compose-adaptive"))
        assertTrue(mappedEquivalents.contains("kotlin-media-coil"))
        assertTrue(mappedEquivalents.contains("kotlin-data-store5"))

        assertTrue(report.compatibilityScore in 60..95)
        assertFalse(report.pathwayRationale.isBlank())
    }

    @Test
    fun `audit on React Native detects TurboModules New Architecture`() {
        fakeEnv.writeFileText(
            "package.json",
            """
            {
              "name": "NewArchApp",
              "dependencies": {
                "react-native": "0.76.1"
              }
            }
            """.trimIndent(),
        )
        fakeEnv.writeFileText("android/gradle.properties", "newArchEnabled=true")

        val report = migrationEngine.audit(".")
        assertEquals(ArchitectureType.NEW_ARCHITECTURE_TURBOMODULES, report.architecture)
    }

    @Test
    fun `audit on Flutter maps pub dependencies and channels`() {
        fakeEnv.writeFileText(
            "pubspec.yaml",
            """
            name: legacy_flutter
            environment:
              sdk: '>=3.2.0 <4.0.0'
            dependencies:
              flutter:
                sdk: flutter
              sqflite: ^2.3.0
              shared_preferences: ^2.2.0
              dio: ^5.3.0
              flutter_bloc: ^8.1.3
              cached_network_image: ^3.3.0
            """.trimIndent(),
        )
        fakeEnv.addFile("android")
        fakeEnv.addFile("ios")

        val report = migrationEngine.audit(".")
        assertEquals(LegacyFramework.FLUTTER, report.framework)
        assertTrue(report.frameworkVersion?.contains("3.2.0") == true)

        val mappedEquivalents = report.dependencies.map { it.kmpEquivalent }
        assertTrue(mappedEquivalents.contains("kotlin-data-sqldelight-database"))
        assertTrue(mappedEquivalents.contains("kotlin-data-multiplatform-settings"))
        assertTrue(mappedEquivalents.contains("kotlin-networking-ktor"))
        assertTrue(mappedEquivalents.contains("kotlin-architecture-orbit-mvi"))
        assertTrue(mappedEquivalents.contains("kotlin-media-coil"))
    }

    @Test
    fun `plan generates phased Strangler Fig migration roadmap`() {
        fakeEnv.writeFileText(
            "package.json",
            """
            {
              "name": "MigratingApp",
              "dependencies": {
                "react-native": "0.73.0",
                "axios": "^1.4.0"
              }
            }
            """.trimIndent(),
        )

        val plan = migrationEngine.plan(".")
        assertEquals(LegacyFramework.REACT_NATIVE, plan.framework)
        assertEquals(4, plan.phases.size)

        assertEquals("Foundation & Core Bridge", plan.phases[0].name)
        assertTrue(plan.phases[0].recommendedSkills.contains("kmp-shared-core-bridge"))

        assertEquals("Domain & Data Strangler", plan.phases[1].name)
        assertTrue(plan.phases[1].recommendedSkills.contains("kotlin-data-store5"))

        assertEquals("UI & Screen Incremental", plan.phases[2].name)
        assertTrue(plan.phases[2].recommendedSkills.contains("kotlin-ui-compose-adaptive"))

        assertEquals("Legacy Shell Deletion", plan.phases[3].name)
        assertTrue(plan.phases[3].recommendedSkills.contains("kotlin-tooling-gradle-to-kotlin-toolchain"))
    }
}
