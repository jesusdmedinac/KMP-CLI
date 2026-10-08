plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kmpNativeCoroutines) apply false
}

tasks.register<Exec>("webRun") {
    group = "application"
    description = "Builds the shared KMP JS library and starts the React web dev server"
    dependsOn(":shared:jsBrowserDevelopmentLibraryDistribution")
    commandLine("npm", "run", "start", "-w", "webApp")
}