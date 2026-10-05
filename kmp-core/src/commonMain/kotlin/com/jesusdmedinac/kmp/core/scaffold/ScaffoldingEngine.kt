package com.jesusdmedinac.kmp.core.scaffold

import com.jesusdmedinac.kmp.core.scaffold.generator.ComposeMultiplatformGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.FullstackGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.KmpLibraryGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.SduiStarterGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.TemplateGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.ToolchainAppGenerator
import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingResult
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class ScaffoldingEngine(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    fun scaffold(options: ScaffoldingOptions): ScaffoldingResult {
        if (options.name.isBlank()) {
            return ScaffoldingResult(
                status = "FAILED",
                template = options.template.id,
                projectPath = options.outputDir,
                errorMessage = "Project name cannot be empty.",
            )
        }

        val outDir = options.outputDir.trim()
        val generator: TemplateGenerator = when (options.template) {
            ProjectTemplate.COMPOSE_MULTIPLATFORM -> ComposeMultiplatformGenerator()
            ProjectTemplate.TOOLCHAIN_APP -> ToolchainAppGenerator()
            ProjectTemplate.KMP_LIBRARY -> KmpLibraryGenerator()
            ProjectTemplate.FULLSTACK -> FullstackGenerator()
            ProjectTemplate.SDUI_STARTER -> SduiStarterGenerator()
        }

        val generatedFiles = generator.generate(options)
        val writtenFiles = mutableListOf<String>()

        for ((relPath, content) in generatedFiles) {
            val fullPath = if (outDir == "." || outDir.isEmpty()) relPath else "$outDir/$relPath"
            val success = systemEnvironment.writeFileText(fullPath, content)
            if (!success) {
                return ScaffoldingResult(
                    status = "FAILED",
                    template = options.template.id,
                    projectPath = outDir,
                    createdFiles = writtenFiles,
                    errorMessage = "Failed to write file '$fullPath'.",
                )
            }
            writtenFiles.add(fullPath)
        }

        return ScaffoldingResult(
            status = "SUCCESS",
            template = options.template.id,
            projectPath = outDir,
            createdFiles = writtenFiles,
        )
    }
}
