package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions
import com.jesusdmedinac.kmp.core.scaffold.template.TemplateContext
import com.jesusdmedinac.kmp.core.scaffold.template.TemplateProcessor
import com.jesusdmedinac.kmp.core.scaffold.template.TemplateSource

class FullstackGenerator(
    private val templateSource: TemplateSource,
) : TemplateGenerator {
    override fun generate(options: ScaffoldingOptions): Map<String, String> {
        val rawFiles = templateSource.loadRawTemplateFiles(ProjectTemplate.FULLSTACK)
        val packagePath = options.packageName.replace('.', '/')

        val context = TemplateContext(
            variables = mapOf(
                "PROJECT_NAME" to options.name,
                "PACKAGE_NAME" to options.packageName,
                "PACKAGE_PATH" to packagePath,
            ),
            activeTargets = options.targets.map { it.lowercase().trim() }.toSet(),
        )

        return TemplateProcessor.process(rawFiles, context)
    }
}
