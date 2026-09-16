import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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

val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()
val isMac = osName.contains("mac")
val isLinux = osName.contains("linux")
val isArm64 = osArch == "aarch64" || osArch == "arm64"

val hostDebugTaskName: String? = when {
    isMac && isArm64 -> "linkDebugExecutableMacosArm64"
    isMac -> "linkDebugExecutableMacosX64"
    isLinux -> "linkDebugExecutableLinuxX64"
    else -> null
}

val hostDebugBinaryFile: Provider<RegularFile>? = when {
    isMac && isArm64 -> layout.buildDirectory.file("bin/macosArm64/debugExecutable/kmp.kexe")
    isMac -> layout.buildDirectory.file("bin/macosX64/debugExecutable/kmp.kexe")
    isLinux -> layout.buildDirectory.file("bin/linuxX64/debugExecutable/kmp.kexe")
    else -> null
}

val hostReleaseTaskName: String? = when {
    isMac -> "assembleReleaseExecutableMacos"
    isLinux -> "linkReleaseExecutableLinuxX64"
    else -> null
}

val hostReleaseBinaryFile: Provider<RegularFile>? = when {
    isMac -> layout.buildDirectory.file("bin/macosUniversal/releaseExecutable/kmp")
    isLinux -> layout.buildDirectory.file("bin/linuxX64/releaseExecutable/kmp.kexe")
    else -> null
}

tasks.register("installDebugLocal") {
    group = "Install"
    description = "Installs a live development symlink to the debug executable in ~/.local/bin/kmp"
    if (hostDebugTaskName != null && hostDebugBinaryFile != null) {
        dependsOn(hostDebugTaskName)
        doLast {
            val localBin = File(System.getProperty("user.home"), ".local/bin")
            if (!localBin.exists()) {
                localBin.mkdirs()
            }
            val target = File(localBin, "kmp")
            val binaryFile = hostDebugBinaryFile.get().asFile
            if (!binaryFile.exists()) {
                throw GradleException("Debug binary not found at ${binaryFile.absolutePath}")
            }
            Files.deleteIfExists(target.toPath())
            Files.createSymbolicLink(target.toPath(), binaryFile.toPath())
            target.setExecutable(true, false)
            println("✓ Symlinked debug executable to ${target.absolutePath} -> ${binaryFile.absolutePath}")

            val pathEnv = System.getenv("PATH") ?: ""
            val localBinCanonical = try { localBin.canonicalPath } catch (e: Exception) { localBin.absolutePath }
            val inPath = pathEnv.split(File.pathSeparator).any { entry ->
                val entryCanonical = try { File(entry).canonicalPath } catch (e: Exception) { entry }
                entryCanonical == localBinCanonical
            }
            if (!inPath) {
                println()
                println("⚠️  Notice: ${localBin.absolutePath} is not currently in your \$PATH.")
                println("   To invoke 'kmp' from anywhere, add it to your shell configuration:")
                println("     echo 'export PATH=\"\$HOME/.local/bin:\$PATH\"' >> ~/.zshrc   # or ~/.bashrc")
                println("     source ~/.zshrc")
                println()
            }
        }
    } else {
        doLast {
            throw GradleException("Unsupported host operating system for local installation: $osName ($osArch)")
        }
    }
}

tasks.register("installLocal") {
    group = "Install"
    description = "Installs a release binary copy into ~/.local/bin/kmp (survives ./gradlew clean)"
    if (hostReleaseTaskName != null && hostReleaseBinaryFile != null) {
        dependsOn(hostReleaseTaskName)
        doLast {
            val localBin = File(System.getProperty("user.home"), ".local/bin")
            if (!localBin.exists()) {
                localBin.mkdirs()
            }
            val target = File(localBin, "kmp")
            val binaryFile = hostReleaseBinaryFile.get().asFile
            if (!binaryFile.exists()) {
                throw GradleException("Release binary not found at ${binaryFile.absolutePath}")
            }
            Files.deleteIfExists(target.toPath())
            Files.copy(
                binaryFile.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
            )
            target.setExecutable(true, false)
            println("✓ Installed release binary copy to ${target.absolutePath} (survives ./gradlew clean)")

            val pathEnv = System.getenv("PATH") ?: ""
            val localBinCanonical = try { localBin.canonicalPath } catch (e: Exception) { localBin.absolutePath }
            val inPath = pathEnv.split(File.pathSeparator).any { entry ->
                val entryCanonical = try { File(entry).canonicalPath } catch (e: Exception) { entry }
                entryCanonical == localBinCanonical
            }
            if (!inPath) {
                println()
                println("⚠️  Notice: ${localBin.absolutePath} is not currently in your \$PATH.")
                println("   To invoke 'kmp' from anywhere, add it to your shell configuration:")
                println("     echo 'export PATH=\"\$HOME/.local/bin:\$PATH\"' >> ~/.zshrc   # or ~/.bashrc")
                println("     source ~/.zshrc")
                println()
            }
        }
    } else {
        doLast {
            throw GradleException("Unsupported host operating system for local installation: $osName ($osArch)")
        }
    }
}

