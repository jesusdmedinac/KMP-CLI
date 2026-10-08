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
            // implementation(libs.json.to.compose.core)
            implementation(libs.ktor.client.core)
        }
        // {{#TARGET:android}}
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        // {{/TARGET:android}}
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
