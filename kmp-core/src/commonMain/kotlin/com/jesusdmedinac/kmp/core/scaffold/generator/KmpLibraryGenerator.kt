package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions

class KmpLibraryGenerator : TemplateGenerator {
    override fun generate(options: ScaffoldingOptions): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val packagePath = options.packageName.replace('.', '/')
        val targets = options.targets.map { it.lowercase().trim() }

        val hasAndroid = targets.contains("android")
        val hasIos = targets.any { it.startsWith("ios") }
        val hasDesktop = targets.any { it == "desktop" || it == "jvm" }
        val hasWasm = targets.any { it.startsWith("wasm") }

        files[".gitignore"] = """
            *.iml
            .gradle
            /build
            !/gradle/wrapper/gradle-wrapper.jar
            .idea
            local.properties
            .DS_Store
            .kotlin
        """.trimIndent()

        files["settings.gradle.kts"] = """
            rootProject.name = "${options.name}"

            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            dependencyResolutionManagement {
                repositories {
                    google()
                    mavenCentral()
                }
            }
        """.trimIndent()

        files["gradle/libs.versions.toml"] = """
            [versions]
            agp = "8.9.0"
            kotlin = "2.2.0"

            [plugins]
            androidLibrary = { id = "com.android.library", version.ref = "agp" }
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
        """.trimIndent()

        val targetBlocks = mutableListOf<String>()
        if (hasAndroid) {
            targetBlocks.add("""
                androidTarget {
                    publishLibraryVariants("release")
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                    }
                }
            """.trimIndent())
        }
        if (hasDesktop) {
            targetBlocks.add("""
                jvm()
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
                        baseName = "${options.name}"
                    }
                }
            """.trimIndent())
        }
        if (hasWasm) {
            targetBlocks.add("""
                wasmJs {
                    nodejs()
                }
            """.trimIndent())
        }

        val pluginsBlock = buildString {
            appendLine("plugins {")
            appendLine("    alias(libs.plugins.kotlinMultiplatform)")
            if (hasAndroid) {
                appendLine("    alias(libs.plugins.androidLibrary)")
            }
            appendLine("    `maven-publish`")
            appendLine("}")
        }

        val androidBlock = if (hasAndroid) {
            """
            android {
                namespace = "${options.packageName}"
                compileSdk = 35
                defaultConfig {
                    minSdk = 24
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
            """.trimIndent()
        } else ""

        files["build.gradle.kts"] = """
            $pluginsBlock

            group = "${options.packageName}"
            version = "0.1.0"

            kotlin {
            ${targetBlocks.joinToString("\n\n").prependIndent("    ")}

                sourceSets {
                    commonMain.dependencies {
                        // Common multiplatform dependencies
                    }
                    commonTest.dependencies {
                        implementation(kotlin("test"))
                    }
                }
            }

            $androidBlock

            publishing {
                publications.withType<MavenPublication> {
                    pom {
                        name.set("${options.name}")
                        description.set("Kotlin Multiplatform Library ${options.name}")
                    }
                }
            }
        """.trimIndent()

        files["src/commonMain/kotlin/$packagePath/Platform.kt"] = """
            package ${options.packageName}

            expect fun getPlatformName(): String

            class Greeting {
                fun greet(): String = "Hello from ${'$'}{getPlatformName()}!"
            }
        """.trimIndent()

        if (hasDesktop) {
            files["src/jvmMain/kotlin/$packagePath/Platform.jvm.kt"] = """
                package ${options.packageName}

                actual fun getPlatformName(): String = "JVM (${'$'}{System.getProperty("java.version")})"
            """.trimIndent()
        }

        if (hasIos) {
            files["src/iosMain/kotlin/$packagePath/Platform.ios.kt"] = """
                package ${options.packageName}

                import platform.UIKit.UIDevice

                actual fun getPlatformName(): String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
            """.trimIndent()
        }

        files["src/commonTest/kotlin/$packagePath/GreetingTest.kt"] = """
            package ${options.packageName}

            import kotlin.test.Test
            import kotlin.test.assertTrue

            class GreetingTest {
                @Test
                fun testGreeting() {
                    val greeting = Greeting().greet()
                    assertTrue(greeting.startsWith("Hello from"))
                }
            }
        """.trimIndent()

        return files
    }
}
