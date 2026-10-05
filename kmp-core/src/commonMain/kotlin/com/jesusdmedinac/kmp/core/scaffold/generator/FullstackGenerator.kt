package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions

class FullstackGenerator : TemplateGenerator {
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
            /server/build
            /shared/build
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

            include(":shared")
            include(":server")
            include(":composeApp")
        """.trimIndent()

        files["gradle/libs.versions.toml"] = """
            [versions]
            agp = "8.9.0"
            kotlin = "2.2.0"
            ktor = "3.1.1"
            compose-multiplatform = "1.8.0"
            kotlinx-serialization = "1.8.0"
            logback = "1.5.16"

            [libraries]
            ktor-server-core = { module = "io.ktor:ktor-server-core", version.ref = "ktor" }
            ktor-server-netty = { module = "io.ktor:ktor-server-netty", version.ref = "ktor" }
            ktor-server-content-negotiation = { module = "io.ktor:ktor-server-content-negotiation", version.ref = "ktor" }
            ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
            logback = { module = "ch.qos.logback:logback-classic", version.ref = "logback" }
            kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            androidLibrary = { id = "com.android.library", version.ref = "agp" }
            composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
            composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
            kotlinJvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
        """.trimIndent()

        files["build.gradle.kts"] = """
            plugins {
                alias(libs.plugins.androidApplication) apply false
                alias(libs.plugins.androidLibrary) apply false
                alias(libs.plugins.composeMultiplatform) apply false
                alias(libs.plugins.composeCompiler) apply false
                alias(libs.plugins.kotlinMultiplatform) apply false
                alias(libs.plugins.kotlinJvm) apply false
                alias(libs.plugins.kotlinSerialization) apply false
            }
        """.trimIndent()

        // shared
        files["shared/build.gradle.kts"] = """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.kotlinSerialization)
            }

            kotlin {
                jvm()
                iosArm64()
                iosX64()
                iosSimulatorArm64()

                sourceSets {
                    commonMain.dependencies {
                        implementation(libs.kotlinx.serialization.json)
                    }
                }
            }
        """.trimIndent()

        files["shared/src/commonMain/kotlin/$packagePath/shared/Message.kt"] = """
            package ${options.packageName}.shared

            import kotlinx.serialization.Serializable

            @Serializable
            data class Message(
                val id: String,
                val text: String,
                val timestamp: Long,
            )
        """.trimIndent()

        // server
        files["server/build.gradle.kts"] = """
            plugins {
                alias(libs.plugins.kotlinJvm)
                alias(libs.plugins.kotlinSerialization)
                application
            }

            application {
                mainClass.set("${options.packageName}.server.ApplicationKt")
            }

            dependencies {
                implementation(project(":shared"))
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.logback)
            }
        """.trimIndent()

        files["server/src/main/kotlin/$packagePath/server/Application.kt"] = """
            package ${options.packageName}.server

            import ${options.packageName}.shared.Message
            import io.ktor.server.application.*
            import io.ktor.server.engine.*
            import io.ktor.server.netty.*
            import io.ktor.server.plugins.contentnegotiation.*
            import io.ktor.server.response.*
            import io.ktor.server.routing.*
            import io.ktor.serialization.kotlinx.json.*

            fun main() {
                embeddedServer(Netty, port = 8080) {
                    install(ContentNegotiation) {
                        json()
                    }
                    routing {
                        get("/api/hello") {
                            call.respond(Message(id = "1", text = "Hello from Ktor Server!", timestamp = System.currentTimeMillis()))
                        }
                    }
                }.start(wait = true)
            }
        """.trimIndent()

        // composeApp
        files["composeApp/build.gradle.kts"] = """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.composeMultiplatform)
                alias(libs.plugins.composeCompiler)
            }

            kotlin {
                jvm("desktop")

                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":shared"))
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.ui)
                    }
                }
            }
        """.trimIndent()

        files["composeApp/src/commonMain/kotlin/$packagePath/App.kt"] = """
            package ${options.packageName}

            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.foundation.layout.padding
            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.unit.dp

            @Composable
            fun App() {
                MaterialTheme {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Fullstack KMP Client")
                    }
                }
            }
        """.trimIndent()

        return files
    }
}
