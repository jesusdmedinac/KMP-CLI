package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions

class ComposeMultiplatformGenerator : TemplateGenerator {
    override fun generate(options: ScaffoldingOptions): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val packagePath = options.packageName.replace('.', '/')
        val targets = options.targets.map { it.lowercase().trim() }

        val hasAndroid = targets.contains("android")
        val hasIos = targets.any { it.startsWith("ios") }
        val hasDesktop = targets.any { it == "desktop" || it == "jvm" }
        val hasWasm = targets.any { it.startsWith("wasm") }

        // .gitignore
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

        // settings.gradle.kts
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

        // gradle/libs.versions.toml
        files["gradle/libs.versions.toml"] = """
            [versions]
            agp = "8.9.0"
            android-compileSdk = "35"
            android-minSdk = "24"
            android-targetSdk = "35"
            androidx-activityCompose = "1.10.1"
            androidx-lifecycle = "2.8.4"
            compose-multiplatform = "1.8.0"
            kotlin = "2.2.0"

            [libraries]
            androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activityCompose" }
            androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", version.ref = "androidx-lifecycle" }
            androidx-lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle" }

            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            androidLibrary = { id = "com.android.library", version.ref = "agp" }
            composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
            composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
        """.trimIndent()

        // build.gradle.kts (root)
        files["build.gradle.kts"] = """
            plugins {
                alias(libs.plugins.androidApplication) apply false
                alias(libs.plugins.androidLibrary) apply false
                alias(libs.plugins.composeMultiplatform) apply false
                alias(libs.plugins.composeCompiler) apply false
                alias(libs.plugins.kotlinMultiplatform) apply false
            }
        """.trimIndent()

        // composeApp/build.gradle.kts
        val targetBlocks = mutableListOf<String>()
        if (hasAndroid) {
            targetBlocks.add("""
                androidTarget {
                    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                    }
                }
            """.trimIndent())
        }
        if (hasIos) {
            targetBlocks.add("""
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
            """.trimIndent())
        }
        if (hasDesktop) {
            targetBlocks.add("""
                jvm("desktop")
            """.trimIndent())
        }
        if (hasWasm) {
            targetBlocks.add("""
                @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
                wasmJs {
                    moduleName = "composeApp"
                    browser {
                        commonWebpackConfig {
                            outputFileName = "composeApp.js"
                        }
                    }
                    binaries.executable()
                }
            """.trimIndent())
        }

        val pluginsBlock = buildString {
            appendLine("plugins {")
            appendLine("    alias(libs.plugins.kotlinMultiplatform)")
            if (hasAndroid) {
                appendLine("    alias(libs.plugins.androidApplication)")
            }
            appendLine("    alias(libs.plugins.composeMultiplatform)")
            appendLine("    alias(libs.plugins.composeCompiler)")
            appendLine("}")
        }

        val androidBlock = if (hasAndroid) {
            """
            android {
                namespace = "${options.packageName}"
                compileSdk = libs.versions.android.compileSdk.get().toInt()

                defaultConfig {
                    applicationId = "${options.packageName}"
                    minSdk = libs.versions.android.minSdk.get().toInt()
                    targetSdk = libs.versions.android.targetSdk.get().toInt()
                    versionCode = 1
                    versionName = "1.0"
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
            """.trimIndent()
        } else ""

        files["composeApp/build.gradle.kts"] = """
            $pluginsBlock

            kotlin {
            ${targetBlocks.joinToString("\n\n").prependIndent("    ")}

                sourceSets {
                    commonMain.dependencies {
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.ui)
                        implementation(compose.components.resources)
                        implementation(compose.components.uiToolingPreview)
                        implementation(libs.androidx.lifecycle.viewmodel)
                        implementation(libs.androidx.lifecycle.runtime.compose)
                    }
                    ${if (hasAndroid) """
                    androidMain.dependencies {
                        implementation(compose.preview)
                        implementation(libs.androidx.activity.compose)
                    }
                    """.trimIndent().prependIndent("        ") else ""}
                }
            }

            $androidBlock
        """.trimIndent()

        // App.kt
        files["composeApp/src/commonMain/kotlin/$packagePath/App.kt"] = """
            package ${options.packageName}

            import androidx.compose.animation.AnimatedVisibility
            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.foundation.layout.padding
            import androidx.compose.material3.Button
            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.getValue
            import androidx.compose.runtime.mutableStateOf
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.setValue
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.unit.dp

            @Composable
            fun App() {
                MaterialTheme {
                    var showGreeting by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(onClick = { showGreeting = !showGreeting }) {
                            Text("Click me!")
                        }
                        AnimatedVisibility(showGreeting) {
                            Text(
                                text = "Hello from Compose Multiplatform!",
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        """.trimIndent()

        return files
    }
}
