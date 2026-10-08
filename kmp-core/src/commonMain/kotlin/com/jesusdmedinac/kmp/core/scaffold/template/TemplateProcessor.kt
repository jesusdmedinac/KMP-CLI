package com.jesusdmedinac.kmp.core.scaffold.template

data class TemplateContext(
    val variables: Map<String, String>,
    val activeTargets: Set<String> = emptySet(),
)

object TemplateProcessor {
    private val startTargetRegex = Regex("""(?://\s*|#\s*)?\{\{#TARGET:([a-zA-Z0-9_-]+)\}""")
    private val endTargetRegex = Regex("""(?://\s*|#\s*)?\{\{/TARGET:([a-zA-Z0-9_-]+)\}""")

    fun process(
        rawFiles: Map<String, String>,
        context: TemplateContext,
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((rawPath, rawContent) in rawFiles) {
            val resolvedPath = processPath(rawPath, context.variables)
            val resolvedContent = processContent(rawContent, context)
            result[resolvedPath] = resolvedContent
        }
        return result
    }

    fun processPath(path: String, variables: Map<String, String>): String {
        var resolved = path
        for ((key, value) in variables) {
            resolved = resolved.replace("{{$key}}", value)
        }
        return resolved
    }

    fun processContent(content: String, context: TemplateContext): String {
        val filtered = processConditionals(content, context.activeTargets)
        var resolved = filtered
        for ((key, value) in context.variables) {
            resolved = resolved.replace("{{$key}}", value)
        }
        return resolved
    }

    private fun processConditionals(content: String, activeTargets: Set<String>): String {
        val normalizedTargets = activeTargets.map { it.lowercase().trim() }.toSet()
        val lines = content.lines()
        val result = mutableListOf<String>()
        var skipCurrentBlock = false

        for (line in lines) {
            val trimmed = line.trim()
            val startMatch = startTargetRegex.find(trimmed)
            if (startMatch != null) {
                val targetName = startMatch.groupValues[1].lowercase()
                skipCurrentBlock = !normalizedTargets.contains(targetName)
                continue
            }

            val endMatch = endTargetRegex.find(trimmed)
            if (endMatch != null) {
                skipCurrentBlock = false
                continue
            }

            if (!skipCurrentBlock) {
                result.add(line)
            }
        }
        return result.joinToString("\n")
    }
}
