package com.jesusdmedinac.kmp.core.scaffold.template

import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate

object EmbeddedTemplates {
    fun get(template: ProjectTemplate): Map<String, String> = when (template) {
        ProjectTemplate.SHARED_UI -> composeMultiplatform
        ProjectTemplate.NATIVE_UI -> nativeUi
        ProjectTemplate.MULTIPLATFORM_LIBRARY -> kmpLibrary
        ProjectTemplate.TOOLCHAIN_SHARED_UI -> toolchainApp
        ProjectTemplate.TOOLCHAIN_NATIVE_UI -> toolchainApp
        ProjectTemplate.FULLSTACK -> fullstack
        ProjectTemplate.JSON_TO_COMPOSE_SAMPLE -> jsonToComposeSample
    }

    private val composeMultiplatform = mapOf(
        ".run/desktopApp.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="desktopApp" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":composeApp:desktopRun" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
        ".run/wasmJs.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="wasmJs" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":composeApp:wasmJsBrowserDevelopmentRun" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
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
        "gradle.properties" to """
            kotlin.code.style=official
            kotlin.daemon.jvmargs=-Xmx3072M
            org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
            org.gradle.configuration-cache=true
            org.gradle.caching=true
            android.nonTransitiveRClass=true
            android.useAndroidX=true
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
                @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
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
        """.trimIndent(),
        "kotlin" to """
            #!/bin/sh
            echo "Kotlin Toolchain"
        """.trimIndent(),
        "kotlin.bat" to """
            @echo off
            echo Kotlin Toolchain
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
        ".run/desktopApp.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="desktopApp" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":app:desktopApp:run" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
        ".run/server.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="server" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":server:run" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
        ".run/wasmJs.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="wasmJs" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":app:webApp:wasmJsBrowserDevelopmentRun" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
        ".run/js.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="js" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":app:webApp:jsBrowserDevelopmentRun" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
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
        "gradle.properties" to """
            kotlin.code.style=official
            kotlin.daemon.jvmargs=-Xmx3072M
            org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
            org.gradle.configuration-cache=true
            org.gradle.caching=true
            android.nonTransitiveRClass=true
            android.useAndroidX=true
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

            include(":app:androidApp")
            include(":app:desktopApp")
            include(":app:shared")
            include(":app:webApp")
            include(":core")
            include(":server")
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
            ktor = { id = "io.ktor.plugin", version.ref = "ktor" }
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
                alias(libs.plugins.ktor) apply false
            }
        """.trimIndent(),
        "core/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
            }

            kotlin {
                jvm()
                androidLibrary {
                    namespace = "{{PACKAGE_NAME}}.core"
                    compileSdk = 35
                }
            }
        """.trimIndent(),
        "core/src/commonMain/kotlin/{{PACKAGE_PATH}}/GreetingUtil.kt" to """
            package {{PACKAGE_NAME}}

            fun sayHello(name: String): String = "Hello, ${'$'}name from Core!"
        """.trimIndent(),
        "app/shared/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.composeMultiplatform)
                alias(libs.plugins.composeCompiler)
            }

            kotlin {
                jvm()
                androidLibrary {
                    namespace = "{{PACKAGE_NAME}}.shared"
                    compileSdk = 35
                }
                listOf(
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = "Shared"
                        isStatic = true
                    }
                }
                wasmJs { browser() }

                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":core"))
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.ui)
                    }
                }
            }
        """.trimIndent(),
        "app/shared/src/commonMain/kotlin/{{PACKAGE_PATH}}/App.kt" to """
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
                        Text(sayHello("Fullstack KMP"))
                    }
                }
            }
        """.trimIndent(),
        "app/androidApp/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.androidApplication)
                alias(libs.plugins.composeCompiler)
            }

            android {
                namespace = "{{PACKAGE_NAME}}"
                compileSdk = 35

                defaultConfig {
                    applicationId = "{{PACKAGE_NAME}}"
                    minSdk = 24
                    targetSdk = 35
                    versionCode = 1
                    versionName = "1.0"
                }
            }

            dependencies {
                implementation(project(":app:shared"))
            }
        """.trimIndent(),
        "app/androidApp/src/main/AndroidManifest.xml" to """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application
                    android:allowBackup="true"
                    android:label="{{PROJECT_NAME}}"
                    android:supportsRtl="true"
                    android:theme="@android:style/Theme.Material.Light.NoActionBar">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent(),
        "app/androidApp/src/main/kotlin/{{PACKAGE_PATH}}/MainActivity.kt" to """
            package {{PACKAGE_NAME}}

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent

            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent {
                        App()
                    }
                }
            }
        """.trimIndent(),
        "app/desktopApp/build.gradle.kts" to """
            import org.jetbrains.compose.desktop.application.dsl.TargetFormat

            plugins {
                alias(libs.plugins.kotlinJvm)
                alias(libs.plugins.composeMultiplatform)
                alias(libs.plugins.composeCompiler)
            }

            dependencies {
                implementation(project(":app:shared"))
                implementation(compose.desktop.currentOs)
            }

            compose.desktop {
                application {
                    mainClass = "{{PACKAGE_NAME}}.MainKt"

                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        packageName = "{{PROJECT_NAME}}"
                        packageVersion = "1.0.0"
                    }
                }
            }
        """.trimIndent(),
        "app/desktopApp/src/main/kotlin/{{PACKAGE_PATH}}/main.kt" to """
            package {{PACKAGE_NAME}}

            import androidx.compose.ui.window.Window
            import androidx.compose.ui.window.application

            fun main() = application {
                Window(onCloseRequest = ::exitApplication, title = "{{PROJECT_NAME}}") {
                    App()
                }
            }
        """.trimIndent(),
        "app/webApp/build.gradle.kts" to """
            import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.composeMultiplatform)
                alias(libs.plugins.composeCompiler)
            }

            kotlin {
                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                    binaries.executable()
                }

                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":app:shared"))
                    }
                }
            }
        """.trimIndent(),
        "app/webApp/src/webMain/resources/index.html" to """
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>{{PROJECT_NAME}}</title>
            </head>
            <body>
                <canvas id="ComposeTarget"></canvas>
                <script src="webApp.js"></script>
            </body>
            </html>
        """.trimIndent(),
        "app/webApp/src/webMain/kotlin/{{PACKAGE_PATH}}/main.kt" to """
            package {{PACKAGE_NAME}}

            import androidx.compose.ui.ExperimentalComposeUiApi
            import androidx.compose.ui.window.CanvasBasedWindow

            @OptIn(ExperimentalComposeUiApi::class)
            fun main() {
                CanvasBasedWindow("{{PROJECT_NAME}}", canvasElementId = "ComposeTarget") {
                    App()
                }
            }
        """.trimIndent(),
        "server/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinJvm)
                alias(libs.plugins.ktor)
            }

            application {
                mainClass.set("{{PACKAGE_NAME}}.server.ApplicationKt")
            }

            dependencies {
                implementation(project(":core"))
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.logback)
            }
        """.trimIndent(),
        "server/src/main/kotlin/{{PACKAGE_PATH}}/server/Application.kt" to """
            package {{PACKAGE_NAME}}.server

            import {{PACKAGE_NAME}}.sayHello
            import io.ktor.server.application.*
            import io.ktor.server.engine.*
            import io.ktor.server.netty.*
            import io.ktor.server.response.*
            import io.ktor.server.routing.*

            fun main() {
                embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
                    routing {
                        get("/") {
                            call.respondText(sayHello("Ktor Server"))
                        }
                    }
                }.start(wait = true)
            }
        """.trimIndent()
    )

    private val jsonToComposeSample = mapOf(
        ".run/desktopApp.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="desktopApp" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":composeApp:desktopRun" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
        ".run/wasmJs.run.xml" to """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="wasmJs" type="GradleRunConfiguration" factoryName="Gradle">
                <ExternalSystemSettings>
                  <option name="executionName" />
                  <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                  <option name="externalSystemIdString" value="GRADLE" />
                  <option name="scriptParameters" value="" />
                  <option name="taskDescriptions">
                    <list />
                  </option>
                  <option name="taskNames">
                    <list>
                      <option value=":composeApp:wasmJsBrowserDevelopmentRun" />
                    </list>
                  </option>
                  <option name="vmOptions" />
                </ExternalSystemSettings>
                <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                <method v="2" />
              </configuration>
            </component>
        """.trimIndent(),
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
        "gradle.properties" to """
            kotlin.code.style=official
            kotlin.daemon.jvmargs=-Xmx3072M
            org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
            org.gradle.configuration-cache=true
            org.gradle.caching=true
            android.nonTransitiveRClass=true
            android.useAndroidX=true
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
                    mavenLocal()
                }
            }

            include(":composeApp")
        """.trimIndent(),
        "gradle/libs.versions.toml" to """
            [versions]
            agp = "8.9.0"
            kotlin = "2.2.0"
            compose-multiplatform = "1.8.0"
            json-to-compose = "1.1.0"
            androidx-activity = "1.10.1"
            ktor = "3.1.1"

            [libraries]
            androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity" }
            json-to-compose = { module = "com.jesusdmedinac:json-to-compose", version.ref = "json-to-compose" }
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
            import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
                @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
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
                        implementation(libs.json.to.compose)
                        implementation(libs.ktor.client.core)
                    }
                    // {{#TARGET:android}}
                    androidMain.dependencies {
                        implementation(libs.androidx.activity.compose)
                    }
                    // {{/TARGET:android}}
                    // {{#TARGET:desktop}}
                    val desktopMain by getting {
                        dependencies {
                            implementation(compose.desktop.currentOs)
                        }
                    }
                    // {{/TARGET:desktop}}
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

            // {{#TARGET:desktop}}
            compose.desktop {
                application {
                    mainClass = "{{PACKAGE_NAME}}.MainKt"

                    nativeDistributions {
                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                        packageName = "{{PACKAGE_NAME}}"
                        packageVersion = "1.0.0"
                    }
                }
            }
            // {{/TARGET:desktop}}
        """.trimIndent(),
        "composeApp/src/desktopMain/kotlin/{{PACKAGE_PATH}}/main.kt" to """
            package {{PACKAGE_NAME}}

            import androidx.compose.ui.window.Window
            import androidx.compose.ui.window.application

            fun main() = application {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = "{{PROJECT_NAME}}",
                ) {
                    SduiApp()
                }
            }
        """.trimIndent(),
        "composeApp/src/iosMain/kotlin/{{PACKAGE_PATH}}/MainViewController.kt" to """
            package {{PACKAGE_NAME}}

            import androidx.compose.ui.window.ComposeUIViewController

            fun MainViewController() = ComposeUIViewController { SduiApp() }
        """.trimIndent(),
        "composeApp/src/commonMain/kotlin/{{PACKAGE_PATH}}/SduiApp.kt" to """
            package {{PACKAGE_NAME}}

            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.Surface
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.CompositionLocalProvider
            import androidx.compose.runtime.getValue
            import androidx.compose.runtime.mutableIntStateOf
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.setValue
            import androidx.compose.ui.Modifier
            import com.jesusdmedinac.jsontocompose.LocalBehavior
            import com.jesusdmedinac.jsontocompose.LocalStateHost
            import com.jesusdmedinac.jsontocompose.ToCompose
            import com.jesusdmedinac.jsontocompose.behavior.Behavior
            import com.jesusdmedinac.jsontocompose.state.MutableStateHost
            import com.jesusdmedinac.jsontocompose.state.StateHost

            @Composable
            fun SduiApp() {
                val nameStateHost = remember { MutableStateHost("") }
                val greetingStateHost = remember { MutableStateHost("Welcome to Server-Driven UI!") }
                var clickCount by remember { mutableIntStateOf(0) }

                val stateHosts = remember {
                    mapOf<String, StateHost<*>>(
                        "name_input" to nameStateHost,
                        "greeting_text" to greetingStateHost,
                    )
                }

                val behaviors = remember {
                    mapOf<String, Behavior>(
                        "on_greet_click" to object : Behavior {
                            override fun invoke() {
                                clickCount++
                                val inputName = nameStateHost.state.trim()
                                if (inputName.isNotEmpty()) {
                                    greetingStateHost.onStateChange("Hello, ${'$'}inputName! 👋 (Click #${'$'}clickCount)")
                                } else {
                                    greetingStateHost.onStateChange("Server-Driven UI in action! 🚀 (Click #${'$'}clickCount)")
                                }
                            }
                        }
                    )
                }

                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        CompositionLocalProvider(
                            LocalStateHost provides stateHosts,
                            LocalBehavior provides behaviors,
                        ) {
                            SAMPLE_SDUI_JSON.ToCompose()
                        }
                    }
                }
            }

            private val SAMPLE_SDUI_JSON = ""${'"'}
            {
              "type": "Column",
              "composeModifier": {
                "operations": [
                  { "type": "FillMaxSize" },
                  { "type": "Padding", "value": 24 }
                ]
              },
              "properties": {
                "type": "ColumnProps",
                "horizontalAlignment": "CenterHorizontally",
                "children": [
                  {
                    "type": "Text",
                    "properties": {
                      "type": "TextProps",
                      "text": "{{PROJECT_NAME}}",
                      "fontSize": 26.0,
                      "fontWeight": "Bold"
                    }
                  },
                  {
                    "type": "Spacer",
                    "composeModifier": {
                      "operations": [
                        { "type": "Height", "value": 8 }
                      ]
                    },
                    "properties": {
                      "type": "SpacerProps"
                    }
                  },
                  {
                    "type": "Text",
                    "properties": {
                      "type": "TextProps",
                      "text": "Powered by json-to-compose",
                      "fontSize": 14.0
                    }
                  },
                  {
                    "type": "Spacer",
                    "composeModifier": {
                      "operations": [
                        { "type": "Height", "value": 24 }
                      ]
                    },
                    "properties": {
                      "type": "SpacerProps"
                    }
                  },
                  {
                    "type": "Card",
                    "composeModifier": {
                      "operations": [
                        { "type": "FillMaxWidth" },
                        { "type": "Padding", "value": 8 }
                      ]
                    },
                    "properties": {
                      "type": "CardProps",
                      "child": {
                        "type": "Column",
                        "composeModifier": {
                          "operations": [
                            { "type": "Padding", "value": 20 }
                          ]
                        },
                        "properties": {
                          "type": "ColumnProps",
                          "horizontalAlignment": "CenterHorizontally",
                          "children": [
                            {
                              "type": "Text",
                              "properties": {
                                "type": "TextProps",
                                "textStateHostName": "greeting_text",
                                "fontSize": 16.0,
                                "fontWeight": "Medium"
                              }
                            },
                            {
                              "type": "Spacer",
                              "composeModifier": {
                                "operations": [
                                  { "type": "Height", "value": 16 }
                                ]
                              },
                              "properties": {
                                "type": "SpacerProps"
                              }
                            },
                            {
                              "type": "TextField",
                              "composeModifier": {
                                "operations": [
                                  { "type": "FillMaxWidth" }
                                ]
                              },
                              "properties": {
                                "type": "TextFieldProps",
                                "valueStateHostName": "name_input",
                                "placeholder": {
                                  "type": "Text",
                                  "properties": {
                                    "type": "TextProps",
                                    "text": "Type your name here..."
                                  }
                                }
                              }
                            },
                            {
                              "type": "Spacer",
                              "composeModifier": {
                                "operations": [
                                  { "type": "Height", "value": 16 }
                                ]
                              },
                              "properties": {
                                "type": "SpacerProps"
                              }
                            },
                            {
                              "type": "Button",
                              "properties": {
                                "type": "ButtonProps",
                                "onClickEventName": "on_greet_click",
                                "child": {
                                  "type": "Text",
                                  "properties": {
                                    "type": "TextProps",
                                    "text": "Send Greeting"
                                  }
                                }
                              }
                            }
                          ]
                        }
                      }
                    }
                  }
                ]
              }
            }
            ""${'"'}.trimIndent()
        """.trimIndent()
    )

    private val nativeUi = mapOf(
        ".gitignore" to """
            *.iml
            .gradle
            /build
            !/gradle/wrapper/gradle-wrapper.jar
            .idea
            local.properties
            .DS_Store
            .kotlin
            node_modules
            dist
        """.trimIndent(),
        "gradle.properties" to """
            kotlin.code.style=official
            kotlin.daemon.jvmargs=-Xmx3072M
            org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
            org.gradle.configuration-cache=true
            org.gradle.caching=true
            android.nonTransitiveRClass=true
            android.useAndroidX=true
        """.trimIndent(),
        "gradlew" to """
            #!/bin/sh
            exec gradle "$@"
        """.trimIndent(),
        "gradlew.bat" to """
            @echo off
            gradle %*
        """.trimIndent(),
        "package.json" to """
            {
              "name": "{{PROJECT_NAME}}",
              "private": true,
              "workspaces": [
                "webApp"
              ]
            }
        """.trimIndent(),
        "settings.gradle.kts" to """
            rootProject.name = "{{PROJECT_NAME}}"
            enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

            include(":shared")
            include(":sharedUI")
            include(":androidApp")
            include(":desktopApp")
            include(":server")
        """.trimIndent(),
        "build.gradle.kts" to """
            plugins {
                alias(libs.plugins.androidApplication) apply false
                alias(libs.plugins.androidMultiplatformLibrary) apply false
                alias(libs.plugins.composeCompiler) apply false
                alias(libs.plugins.composeMultiplatform) apply false
                alias(libs.plugins.kotlinJvm) apply false
                alias(libs.plugins.kotlinMultiplatform) apply false
                alias(libs.plugins.kotlinxSerialization) apply false
            }
        """.trimIndent(),
        "gradle/libs.versions.toml" to """
            [versions]
            agp = "8.9.0"
            kotlin = "2.2.0"
            compose-multiplatform = "1.8.0"
            ktor = "3.1.0"
            logback = "1.5.16"

            [libraries]
            ktor-server-core = { module = "io.ktor:ktor-server-core", version.ref = "ktor" }
            ktor-server-netty = { module = "io.ktor:ktor-server-netty", version.ref = "ktor" }
            logback = { module = "ch.qos.logback:logback-classic", version.ref = "logback" }

            [plugins]
            androidApplication = { id = "com.android.application", version.ref = "agp" }
            androidMultiplatformLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
            composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
            composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
            kotlinJvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
            kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
            kotlinxSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
        """.trimIndent(),
        "shared/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinMultiplatform)
                alias(libs.plugins.androidMultiplatformLibrary)
                alias(libs.plugins.kotlinxSerialization)
            }

            kotlin {
                androidLibrary {
                    namespace = "{{PACKAGE_NAME}}.shared"
                    compileSdk = 35
                    minSdk = 24
                }

                listOf(
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = "Shared"
                        isStatic = true
                    }
                }

                jvm()

                js {
                    outputModuleName = "shared"
                    browser()
                    binaries.library()
                    generateTypeScriptDefinitions()
                }

                sourceSets {
                    commonMain.dependencies {
                    }
                }
            }
        """.trimIndent(),
        "shared/src/commonMain/kotlin/{{PACKAGE_PATH}}/Platform.kt" to """
            package {{PACKAGE_NAME}}.shared

            expect fun getPlatformName(): String
        """.trimIndent(),
        "webApp/package.json" to """
            {
              "name": "webApp",
              "private": true,
              "version": "0.0.0",
              "type": "module",
              "scripts": {
                "dev": "vite",
                "build": "tsc && vite build",
                "preview": "vite preview"
              },
              "dependencies": {
                "react": "^18.2.0",
                "react-dom": "^18.2.0"
              },
              "devDependencies": {
                "@types/react": "^18.2.0",
                "@types/react-dom": "^18.2.0",
                "@vitejs/plugin-react": "^4.2.0",
                "typescript": "^5.2.0",
                "vite": "^5.1.0"
              }
            }
        """.trimIndent(),
        "webApp/vite.config.ts" to """
            import { defineConfig } from 'vite'
            import react from '@vitejs/plugin-react'

            export default defineConfig({
              plugins: [react()],
            })
        """.trimIndent(),
        "webApp/index.html" to """
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <title>{{PROJECT_NAME}}</title>
              </head>
              <body>
                <div id="root"></div>
                <script type="module" src="/src/main.tsx"></script>
              </body>
            </html>
        """.trimIndent(),
        "iosApp/ContentView.swift" to """
            import SwiftUI

            struct ContentView: View {
                var body: some View {
                    Text("Hello from SwiftUI!")
                        .padding()
                }
            }
        """.trimIndent(),
        "server/build.gradle.kts" to """
            plugins {
                alias(libs.plugins.kotlinJvm)
                application
            }

            application {
                mainClass.set("{{PACKAGE_NAME}}.server.ApplicationKt")
            }

            dependencies {
                implementation(project(":shared"))
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.logback)
            }
        """.trimIndent(),
        "server/src/main/kotlin/{{PACKAGE_PATH}}/server/Application.kt" to """
            package {{PACKAGE_NAME}}.server

            import io.ktor.server.application.*
            import io.ktor.server.engine.*
            import io.ktor.server.netty.*
            import io.ktor.server.response.*
            import io.ktor.server.routing.*

            fun main() {
                embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
                    routing {
                        get("/") {
                            call.respondText("Hello from Ktor Server!")
                        }
                    }
                }.start(wait = true)
            }
        """.trimIndent()
    )
}
