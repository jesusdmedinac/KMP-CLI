package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions

class ToolchainAppGenerator : TemplateGenerator {
    override fun generate(options: ScaffoldingOptions): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val packagePath = options.packageName.replace('.', '/')
        val platforms = options.targets.map { it.lowercase().trim() }

        files[".gitignore"] = """
            .idea
            .kotlin
            /build
            /app/build
            .DS_Store
        """.trimIndent()

        files["project.yaml"] = """
            modules:
              - app
            settings:
              kotlin:
                version: 2.2.0
        """.trimIndent()

        val platformsYaml = platforms.joinToString("\n") { "    - $it" }

        files["app/module.yaml"] = """
            product:
              type: app
              platforms:
            $platformsYaml
            dependencies:
              - org.jetbrains.compose.runtime:runtime:1.8.0
              - org.jetbrains.compose.foundation:foundation:1.8.0
              - org.jetbrains.compose.material3:material3:1.8.0
        """.trimIndent()

        files["app/src/commonMain/kotlin/$packagePath/Main.kt"] = """
            package ${options.packageName}

            fun main() {
                println("Hello from Declarative Kotlin Toolchain App!")
            }
        """.trimIndent()

        return files
    }
}
