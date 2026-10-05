package com.jesusdmedinac.kmp.core.scaffold.template

import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate

object EmbeddedTemplates {
    fun get(template: ProjectTemplate): Map<String, String> = when (template) {
        ProjectTemplate.COMPOSE_MULTIPLATFORM -> composeMultiplatform
        ProjectTemplate.TOOLCHAIN_APP -> toolchainApp
        ProjectTemplate.KMP_LIBRARY -> kmpLibrary
        ProjectTemplate.FULLSTACK -> fullstack
        ProjectTemplate.SDUI_STARTER -> sduiStarter
    }

    private val composeMultiplatform = mapOf(
        ".gitignore" to """
            *.iml
            .gradle
            /build
            !/gradle/wrapper/gradle-wrapper.jar
            .idea
            local.properties
            .DS_Store
            /composeApp/build
            .kotlin
        """.trimIndent(),
        "settings.gradle.kts" to """
            rootProject.name = "{{PROJECT_NAME}}"
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
        """.trimIndent(),
        "gradle/libs.versions.toml" to """
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
        """.trimIndent(),
        "build.gradle.kts" to """
            plugins {
                alias(libs.plugins.androidApplication) apply false
                alias(libs.plugins.androidLibrary) apply false
                alias(libs.plugins.composeMultiplatform) apply false
                alias(libs.plugins.composeCompiler) apply false
                alias(libs.plugins.kotlinMultiplatform) apply false
            }
        """.trimIndent(),
        "composeApp/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                // {{#TARGET:android}}
                alias(libs.plugins.androidApplication)
                // {{/TARGET:android}}
                alias(libs.plugins.composeMultiplatform)
                alias(libs.plugins.composeCompiler)
            }

            kotlin {
                // {{#TARGET:android}}
                androidTarget {
                    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                    }
                }
                // {{/TARGET:android}}

                // {{#TARGET:ios}}
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
                // {{/TARGET:ios}}

                // {{#TARGET:desktop}}
                jvm("desktop")
                // {{/TARGET:desktop}}

                // {{#TARGET:wasm}}
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
                // {{/TARGET:wasm}}

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
                    // {{#TARGET:android}}
                    androidMain.dependencies {
                        implementation(compose.preview)
                        implementation(libs.androidx.activity.compose)
                    }
                    // {{/TARGET:android}}
                }
            }

            // {{#TARGET:android}}
            android {
                namespace = "{{PACKAGE_NAME}}"
                compileSdk = libs.versions.android.compileSdk.get().toInt()

                defaultConfig {
                    applicationId = "{{PACKAGE_NAME}}"
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
            // {{/TARGET:android}}
        """.trimIndent(),
        "composeApp/src/commonMain/kotlin/{{PACKAGE_PATH}}/App.kt" to """
            package {{PACKAGE_NAME}}

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
    )

    private val toolchainApp = mapOf(
        ".gitignore" to """
            .idea
            .kotlin
            /build
            /app/build
            .DS_Store
        """.trimIndent(),
        "project.yaml" to """
            modules:
              - app
            settings:
              kotlin:
                version: 2.2.0
        """.trimIndent(),
        "app/module.yaml" to """
            product:
              type: app
              platforms:
                # {{#TARGET:android}}
                - android
                # {{/TARGET:android}}
                # {{#TARGET:ios}}
                - ios
                # {{/TARGET:ios}}
                # {{#TARGET:desktop}}
                - desktop
                # {{/TARGET:desktop}}
                # {{#TARGET:wasm}}
                - wasm
                # {{/TARGET:wasm}}
            dependencies:
              - org.jetbrains.compose.runtime:runtime:1.8.0
              - org.jetbrains.compose.foundation:foundation:1.8.0
              - org.jetbrains.compose.material3:material3:1.8.0
        """.trimIndent(),
        "app/src/commonMain/kotlin/{{PACKAGE_PATH}}/Main.kt" to """
            package {{PACKAGE_NAME}}

            fun main() {
                println("Hello from Declarative Kotlin Toolchain App!")
            }
        """.trimIndent()
    )

    private val kmpLibrary = mapOf(
        ".gitignore" to """
            *.iml
            .gradle
            /build
            !/gradle/wrapper/gradle-wrapper.jar
            .idea
            local.properties
            .DS_Store
            .kotlin
        """.trimIndent(),
        "settings.gradle.kts" to """
            rootProject.name = "{{PROJECT_NAME}}"

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
        """.trimIndent(),
        "gradle/libs.versions.toml" to """
            [versions]
            agp = "8.9.0"
            kotlin = "2.2.0"

            [plugins]
            androidLibrary = { id = "com.android.library", version.ref = "agp" }
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
        """.trimIndent(),
        "build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                // {{#TARGET:android}}
                alias(libs.plugins.androidLibrary)
                // {{/TARGET:android}}
                `maven-publish`
            }

            group = "{{PACKAGE_NAME}}"
            version = "0.1.0"

            kotlin {
                // {{#TARGET:android}}
                androidTarget {
                    publishLibraryVariants("release")
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                    }
                }
                // {{/TARGET:android}}

                // {{#TARGET:desktop}}
                jvm()
                // {{/TARGET:desktop}}

                // {{#TARGET:ios}}
                listOf(
                    iosX64(),
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = "{{PROJECT_NAME}}"
                    }
                }
                // {{/TARGET:ios}}

                // {{#TARGET:wasm}}
                wasmJs {
                    nodejs()
                }
                // {{/TARGET:wasm}}

                sourceSets {
                    commonMain.dependencies {
                        // Common multiplatform dependencies
                    }
                    commonTest.dependencies {
                        implementation(kotlin("test"))
                    }
                }
            }

            // {{#TARGET:android}}
            android {
                namespace = "{{PACKAGE_NAME}}"
                compileSdk = 35
                defaultConfig {
                    minSdk = 24
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
            // {{/TARGET:android}}

            publishing {
                publications.withType<MavenPublication> {
                    pom {
                        name.set("{{PROJECT_NAME}}")
                        description.set("Kotlin Multiplatform Library {{PROJECT_NAME}}")
                    }
                }
            }
        """.trimIndent(),
        "src/commonMain/kotlin/{{PACKAGE_PATH}}/Platform.kt" to """
            package {{PACKAGE_NAME}}

            expect fun getPlatformName(): String

            class Greeting {
                fun greet(): String = "Hello from ${'$'}{getPlatformName()}!"
            }
        """.trimIndent(),
        "src/jvmMain/kotlin/{{PACKAGE_PATH}}/Platform.jvm.kt" to """
            package {{PACKAGE_NAME}}

            actual fun getPlatformName(): String = "JVM (${'$'}{System.getProperty("java.version")})"
        """.trimIndent(),
        "src/iosMain/kotlin/{{PACKAGE_PATH}}/Platform.ios.kt" to """
            package {{PACKAGE_NAME}}

            import platform.UIKit.UIDevice

            actual fun getPlatformName(): String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
        """.trimIndent(),
        "src/commonTest/kotlin/{{PACKAGE_PATH}}/GreetingTest.kt" to """
            package {{PACKAGE_NAME}}

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
    )

    private val fullstack = mapOf(
        ".gitignore" to """
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
        """.trimIndent(),
        "settings.gradle.kts" to """
            rootProject.name = "{{PROJECT_NAME}}"
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
        """.trimIndent(),
        "gradle/libs.versions.toml" to """
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
        """.trimIndent(),
        "build.gradle.kts" to """
            plugins {
                alias(libs.plugins.androidApplication) apply false
                alias(libs.plugins.androidLibrary) apply false
                alias(libs.plugins.composeMultiplatform) apply false
                alias(libs.plugins.composeCompiler) apply false
                alias(libs.plugins.kotlinMultiplatform) apply false
                alias(libs.plugins.kotlinJvm) apply false
                alias(libs.plugins.kotlinSerialization) apply false
            }
        """.trimIndent(),
        "shared/build.gradle.kts" to """
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
        """.trimIndent(),
        "shared/src/commonMain/kotlin/{{PACKAGE_PATH}}/shared/Message.kt" to """
            package {{PACKAGE_NAME}}.shared

            import kotlinx.serialization.Serializable

            @Serializable
            data class Message(
                val id: String,
                val text: String,
                val timestamp: Long,
            )
        """.trimIndent(),
        "server/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinJvm)
                alias(libs.plugins.kotlinSerialization)
                application
            }

            application {
                mainClass.set("{{PACKAGE_NAME}}.server.ApplicationKt")
            }

            dependencies {
                implementation(project(":shared"))
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.logback)
            }
        """.trimIndent(),
        "server/src/main/kotlin/{{PACKAGE_PATH}}/server/Application.kt" to """
            package {{PACKAGE_NAME}}.server

            import {{PACKAGE_NAME}}.shared.Message
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
        """.trimIndent(),
        "composeApp/build.gradle.kts" to """
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
        """.trimIndent(),
        "composeApp/src/commonMain/kotlin/{{PACKAGE_PATH}}/App.kt" to """
            package {{PACKAGE_NAME}}

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
    )

    private val sduiStarter = mapOf(
        ".gitignore" to """
            *.iml
            .gradle
            /build
            !/gradle/wrapper/gradle-wrapper.jar
            .idea
            local.properties
            .DS_Store
            /composeApp/build
            .kotlin
        """.trimIndent(),
        "settings.gradle.kts" to """
            rootProject.name = "{{PROJECT_NAME}}"
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
        """.trimIndent(),
        "gradle/libs.versions.toml" to """
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
        """.trimIndent(),
        "build.gradle.kts" to """
            plugins {
                alias(libs.plugins.androidApplication) apply false
                alias(libs.plugins.composeMultiplatform) apply false
                alias(libs.plugins.composeCompiler) apply false
                alias(libs.plugins.kotlinMultiplatform) apply false
            }
        """.trimIndent(),
        "composeApp/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.androidApplication)
                alias(libs.plugins.composeMultiplatform)
                alias(libs.plugins.composeCompiler)
            }

            kotlin {
                // {{#TARGET:android}}
                androidTarget()
                // {{/TARGET:android}}

                // {{#TARGET:ios}}
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
                // {{/TARGET:ios}}

                // {{#TARGET:desktop}}
                jvm("desktop")
                // {{/TARGET:desktop}}

                // {{#TARGET:wasm}}
                @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                    binaries.executable()
                }
                // {{/TARGET:wasm}}

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

            // {{#TARGET:android}}
            android {
                namespace = "{{PACKAGE_NAME}}"
                compileSdk = 35
                defaultConfig {
                    applicationId = "{{PACKAGE_NAME}}"
                    minSdk = 24
                    targetSdk = 35
                }
            }
            // {{/TARGET:android}}
        """.trimIndent(),
        "composeApp/src/commonMain/kotlin/{{PACKAGE_PATH}}/SduiApp.kt" to """
            package {{PACKAGE_NAME}}

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
    )
}
