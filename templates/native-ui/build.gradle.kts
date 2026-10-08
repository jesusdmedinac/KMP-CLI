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

val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val npmExecutable = if (isWindows) "npm.cmd" else "npm"

val webInstall = tasks.register<Exec>("webInstall") {
    group = "application"
    description = "Installs npm dependencies for the web workspace"
    workingDir = projectDir
    dependsOn(":shared:jsBrowserDevelopmentLibraryDistribution")
    commandLine(npmExecutable, "install")
}

tasks.register<Exec>("webRun") {
    group = "application"
    description = "Builds the shared KMP JS library and starts the React web dev server"
    dependsOn(":shared:jsBrowserDevelopmentLibraryDistribution")
    if (!file("node_modules").exists()) {
        dependsOn(webInstall)
    }
    commandLine(npmExecutable, "run", "start", "-w", "webApp")
}