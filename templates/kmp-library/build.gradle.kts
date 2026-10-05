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
