package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions
import com.jesusdmedinac.kmp.core.scaffold.template.TemplateContext
import com.jesusdmedinac.kmp.core.scaffold.template.TemplateProcessor
import com.jesusdmedinac.kmp.core.scaffold.template.TemplateSource

class KmpLibraryGenerator(
    private val templateSource: TemplateSource,
) : TemplateGenerator {
    override fun generate(options: ScaffoldingOptions): Map<String, String> {
        val rawFiles = templateSource.loadRawTemplateFiles(ProjectTemplate.KMP_LIBRARY)
        val packagePath = options.packageName.replace('.', '/')

        val targets = options.targets.map { it.lowercase().trim() }.toSet()
        val normalizedTargets = mutableSetOf<String>()
        if (targets.contains("android")) normalizedTargets.add("android")
        if (targets.any { it.startsWith("ios") }) normalizedTargets.add("ios")
        if (targets.any { it == "desktop" || it == "jvm" }) normalizedTargets.add("desktop")
        if (targets.any { it.startsWith("wasm") }) normalizedTargets.add("wasm")

        val context = TemplateContext(
            variables = mapOf(
                "PROJECT_NAME" to options.name,
                "PACKAGE_NAME" to options.packageName,
                "PACKAGE_PATH" to packagePath,
            ),
            activeTargets = normalizedTargets,
        )

        val processed = TemplateProcessor.process(rawFiles, context).toMutableMap()

        if (!normalizedTargets.contains("desktop")) {
            processed.remove("src/jvmMain/kotlin/$packagePath/Platform.jvm.kt")
        }
        if (!normalizedTargets.contains("ios")) {
            processed.remove("src/iosMain/kotlin/$packagePath/Platform.ios.kt")
        }

        return processed
    }
}
