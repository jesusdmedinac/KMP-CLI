package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions

class SduiStarterGenerator : TemplateGenerator {
    override fun generate(options: ScaffoldingOptions): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val packagePath = options.packageName.replace('.', '/')

        files[".gitignore"] = """
            *.iml
            .gradle
            /build
            !/gradle/wrapper/gradle-wrapper.jar
            .idea
            local.properties
            .DS_Store
            /composeApp/build
            .kotlin
        """.trimIndent()

        files["settings.gradle.kts"] = """
            rootProject.name = "${options.name}"
            enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

            pluginManagement {
                repositories {
                    google {
                        mavenContent {
                            includeGroupAndSubgroups("androidx")
                            includeGroupAndSubgroups("com.android")
                            includeGroupAndSubgroups("com.google")
                        }
                    }
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            dependencyResolutionManagement {
                repositories {
                    google {
                        mavenContent {
                            includeGroupAndSubgroups("androidx")
                            includeGroupAndSubgroups("com.android")
                            includeGroupAndSubgroups("com.google")
                        }
                    }
                    mavenCentral()
                }
            }

            include(":composeApp")
        """.trimIndent()

        files["gradle/libs.versions.toml"] = """
            [versions]
            agp = "8.9.0"
            kotlin = "2.2.0"
            compose-multiplatform = "1.8.0"
            json-to-compose = "0.1.0"
            ktor = "3.1.1"

            [libraries]
            json-to-compose-core = { module = "com.jesusdmedinac:json-to-compose", version.ref = "json-to-compose" }
            ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
            ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }

            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
            composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
        """.trimIndent()

        files["build.gradle.kts"] = """
            plugins {
                alias(libs.plugins.androidApplication) apply false
                alias(libs.plugins.composeMultiplatform) apply false
                alias(libs.plugins.composeCompiler) apply false
                alias(libs.plugins.kotlinMultiplatform) apply false
            }
        """.trimIndent()

        files["composeApp/build.gradle.kts"] = """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.androidApplication)
                alias(libs.plugins.composeMultiplatform)
                alias(libs.plugins.composeCompiler)
            }

            kotlin {
                androidTarget()
                listOf(
                    iosX64(),
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = "ComposeApp"
                        isStatic = true
                    }
                }
                jvm("desktop")
                @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                    binaries.executable()
                }

                sourceSets {
                    commonMain.dependencies {
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.ui)
                        implementation(libs.json.to.compose.core)
                        implementation(libs.ktor.client.core)
                    }
                }
            }

            android {
                namespace = "${options.packageName}"
                compileSdk = 35
                defaultConfig {
                    applicationId = "${options.packageName}"
                    minSdk = 24
                    targetSdk = 35
                }
            }
        """.trimIndent()

        files["composeApp/src/commonMain/kotlin/$packagePath/SduiApp.kt"] = """
            package ${options.packageName}

            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            @Composable
            fun SduiApp() {
                MaterialTheme {
                    Text("Server-Driven UI Ready Application")
                }
            }
        """.trimIndent()

        return files
    }
}
