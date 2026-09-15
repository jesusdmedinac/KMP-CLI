plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

tasks.register("installLocal") {
    group = "Install"
    description = "Installs release binary copy into ~/.local/bin/kmp (survives ./gradlew clean)"
    dependsOn(":kmp-cli:installLocal")
}

tasks.register("installDebugLocal") {
    group = "Install"
    description = "Installs a live development symlink to the debug executable in ~/.local/bin/kmp"
    dependsOn(":kmp-cli:installDebugLocal")
}

