plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    listOf(macosArm64(), macosX64(), linuxX64()).forEach { target ->
        target.binaries {
            executable {
                baseName = "kmp"
                entryPoint = "com.jesusdmedinac.kmp.cli.main"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmp-core"))
            implementation(libs.clikt)
            implementation(libs.mordant)
            implementation(libs.mordant.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.register<Exec>("assembleReleaseExecutableMacos") {
    dependsOn("linkReleaseExecutableMacosX64", "linkReleaseExecutableMacosArm64")
    val outputDir = layout.buildDirectory.dir("bin/macosUniversal/releaseExecutable").get().asFile
    doFirst {
        outputDir.mkdirs()
    }
    commandLine(
        "lipo",
        "-create",
        "-output",
        "${outputDir.absolutePath}/kmp",
        layout.buildDirectory.file("bin/macosX64/releaseExecutable/kmp.kexe").get().asFile.absolutePath,
        layout.buildDirectory.file("bin/macosArm64/releaseExecutable/kmp.kexe").get().asFile.absolutePath
    )
    group = "Build"
    description = "Builds universal macOS binary (x86_64 + arm64)"
}
