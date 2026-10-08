package com.jesusdmedinac.kmp.core.scaffold

import com.jesusdmedinac.kmp.core.scaffold.generator.ComposeMultiplatformGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.FullstackGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.GenericTemplateGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.JsonToComposeSampleGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.KmpLibraryGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.TemplateGenerator
import com.jesusdmedinac.kmp.core.scaffold.generator.ToolchainAppGenerator
import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingResult
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

import com.jesusdmedinac.kmp.core.scaffold.template.TemplateSource

class ScaffoldingEngine(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
    customTemplatePath: String? = null,
) {
    private val templateSource = TemplateSource(systemEnvironment, customTemplatePath)

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
            ProjectTemplate.SHARED_UI -> ComposeMultiplatformGenerator(templateSource)
            ProjectTemplate.NATIVE_UI -> GenericTemplateGenerator(ProjectTemplate.NATIVE_UI, templateSource)
            ProjectTemplate.MULTIPLATFORM_LIBRARY -> KmpLibraryGenerator(templateSource)
            ProjectTemplate.TOOLCHAIN_SHARED_UI -> ToolchainAppGenerator(templateSource)
            ProjectTemplate.TOOLCHAIN_NATIVE_UI -> GenericTemplateGenerator(ProjectTemplate.TOOLCHAIN_NATIVE_UI, templateSource)
            ProjectTemplate.FULLSTACK -> FullstackGenerator(templateSource)
            ProjectTemplate.JSON_TO_COMPOSE_SAMPLE -> JsonToComposeSampleGenerator(templateSource)
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
            if (relPath == "gradlew" || relPath == "kotlin") {
                systemEnvironment.setExecutable(fullPath)
            }
            writtenFiles.add(fullPath)
        }

        // Copy binary files (images, gradle-wrapper.jar, etc.) directly without text corruption
        val templateDirs = listOf(
            "templates/${options.template.id}",
            *options.template.aliases.map { "templates/$it" }.toTypedArray()
        )
        for (dir in templateDirs) {
            val allFiles = systemEnvironment.listFilesRecursively(dir)
            if (allFiles.isNotEmpty()) {
                for (relPath in allFiles) {
                    if (TemplateSource.isBinaryFile(relPath)) {
                        val src = "$dir/$relPath"
                        val dest = if (outDir == "." || outDir.isEmpty()) relPath else "$outDir/$relPath"
                        systemEnvironment.copyFile(src, dest)
                        writtenFiles.add(dest)
                    }
                }
                break
            }
        }

        return ScaffoldingResult(
            status = "SUCCESS",
            template = options.template.id,
            projectPath = outDir,
            createdFiles = writtenFiles,
        )
    }
}
