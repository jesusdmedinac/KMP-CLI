package com.jesusdmedinac.kmp.core.scaffold.template

import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.system.SystemEnvironment

class TemplateSource(
    private val systemEnvironment: SystemEnvironment,
    private val customTemplatePath: String? = null,
) {
    fun loadRawTemplateFiles(template: ProjectTemplate): Map<String, String> {
        // 1. Check custom path if provided
        if (!customTemplatePath.isNullOrBlank()) {
            val fromCustom = loadFromDisk("$customTemplatePath/${template.id}")
            if (fromCustom.isNotEmpty()) return fromCustom
            for (alias in template.aliases) {
                val fromAlias = loadFromDisk("$customTemplatePath/$alias")
                if (fromAlias.isNotEmpty()) return fromAlias
            }
        }

        // 2. Check local repository templates/ directory
        val fromLocal = loadFromDisk("templates/${template.id}")
        if (fromLocal.isNotEmpty()) return fromLocal
        for (alias in template.aliases) {
            val fromAlias = loadFromDisk("templates/$alias")
            if (fromAlias.isNotEmpty()) return fromAlias
        }

        // 3. Check global user directory ~/.kmp/templates/
        val home = systemEnvironment.getEnv("HOME")
        if (!home.isNullOrBlank()) {
            val fromGlobal = loadFromDisk("$home/.kmp/templates/${template.id}")
            if (fromGlobal.isNotEmpty()) return fromGlobal
            for (alias in template.aliases) {
                val fromAlias = loadFromDisk("$home/.kmp/templates/$alias")
                if (fromAlias.isNotEmpty()) return fromAlias
            }
        }

        // 4. Return embedded snapshot
        return EmbeddedTemplates.get(template)
    }

    private fun loadFromDisk(baseDir: String): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val foundFiles = systemEnvironment.listFilesRecursively(baseDir)
        if (foundFiles.isNotEmpty()) {
            for (relPath in foundFiles) {
                if (isBinaryFile(relPath)) continue
                val fullPath = "$baseDir/$relPath"
                val content = systemEnvironment.readFileText(fullPath)
                if (content != null) {
                    files[relPath] = content
                }
            }
            return files
        }

        for (relPath in knownTemplateFiles) {
            val fullPath = "$baseDir/$relPath"
            if (systemEnvironment.fileExists(fullPath)) {
                val content = systemEnvironment.readFileText(fullPath)
                if (content != null) {
                    files[relPath] = content
                }
            }
        }
        return files
    }

    companion object {
        fun isBinaryFile(path: String): Boolean {
            val ext = path.substringAfterLast('.', "").lowercase()
            return ext in setOf("jar", "png", "jpg", "jpeg", "ico", "webp", "gif", "keystore", "dylib", "so", "a", "klib")
        }

        val knownTemplateFiles: List<String> = listOf(
            ".gitignore",
            ".run/desktopApp.run.xml",
            ".run/wasmJs.run.xml",
            ".run/server.run.xml",
            "settings.gradle.kts",
            "build.gradle.kts",
            "project.yaml",
            "gradle/libs.versions.toml",
            "app/module.yaml",
            "app/src/commonMain/kotlin/{{PACKAGE_PATH}}/Main.kt",
            "composeApp/build.gradle.kts",
            "composeApp/src/commonMain/kotlin/{{PACKAGE_PATH}}/App.kt",
            "shared/build.gradle.kts",
            "shared/src/commonMain/kotlin/{{PACKAGE_PATH}}/shared/Message.kt",
            "server/build.gradle.kts",
            "server/src/main/kotlin/{{PACKAGE_PATH}}/server/Application.kt",
            "src/commonMain/kotlin/{{PACKAGE_PATH}}/Platform.kt",
            "src/jvmMain/kotlin/{{PACKAGE_PATH}}/Platform.jvm.kt",
            "src/iosMain/kotlin/{{PACKAGE_PATH}}/Platform.ios.kt",
            "src/commonTest/kotlin/{{PACKAGE_PATH}}/GreetingTest.kt"
        )
    }
}
