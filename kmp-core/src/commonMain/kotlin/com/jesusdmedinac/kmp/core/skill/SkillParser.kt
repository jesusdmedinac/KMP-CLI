package com.jesusdmedinac.kmp.core.skill

import com.jesusdmedinac.kmp.core.skill.model.SkillCompatibility
import com.jesusdmedinac.kmp.core.skill.model.SkillManifest
import com.jesusdmedinac.kmp.core.skill.model.SkillMetadata

object SkillParser {

    fun parse(rawDocument: String): SkillManifest {
        val trimmed = rawDocument.trimStart()
        if (!trimmed.startsWith("---")) {
            throw IllegalArgumentException("Missing YAML frontmatter delimiters in document")
        }

        val afterFirstDelimiter = trimmed.removePrefix("---").trimStart('\r', '\n')
        val closingIndex = afterFirstDelimiter.indexOf("\n---")
        if (closingIndex == -1) {
            throw IllegalArgumentException("Missing closing '---' delimiter in document")
        }

        val frontmatter = afterFirstDelimiter.substring(0, closingIndex).trim()
        val content = afterFirstDelimiter.substring(closingIndex + 4).trimStart('\r', '\n')

        var currentKey: String? = null
        var currentNestedSection: String? = null

        val map = mutableMapOf<String, String>()
        val listMap = mutableMapOf<String, MutableList<String>>()
        val nestedMap = mutableMapOf<String, MutableMap<String, String>>()

        for (line in frontmatter.lines()) {
            val rawLine = line.trimEnd()
            if (rawLine.isBlank() || rawLine.trimStart().startsWith("#")) continue

            val isIndented = rawLine.startsWith("  ") || rawLine.startsWith("\t")
            val trimmedLine = rawLine.trim()

            if (isIndented && trimmedLine.startsWith("-")) {
                val item = cleanYamlValue(trimmedLine.removePrefix("-").trim())
                currentKey?.let { listMap.getOrPut(it) { mutableListOf() }.add(item) }
                continue
            }

            if (isIndented && currentNestedSection != null && trimmedLine.contains(":")) {
                val colonIndex = trimmedLine.indexOf(":")
                val subKey = trimmedLine.substring(0, colonIndex).trim()
                val subValue = cleanYamlValue(trimmedLine.substring(colonIndex + 1).trim())
                nestedMap.getOrPut(currentNestedSection) { mutableMapOf() }[subKey] = subValue
                continue
            }

            if (trimmedLine.contains(":")) {
                val colonIndex = trimmedLine.indexOf(":")
                val key = trimmedLine.substring(0, colonIndex).trim()
                val valuePart = trimmedLine.substring(colonIndex + 1).trim()

                if (valuePart.isEmpty()) {
                    currentKey = key
                    currentNestedSection = key
                } else if (valuePart.startsWith("[") && valuePart.endsWith("]")) {
                    currentKey = key
                    currentNestedSection = null
                    val items = valuePart.removeSurrounding("[", "]")
                        .split(",")
                        .map { cleanYamlValue(it.trim()) }
                        .filter { it.isNotEmpty() }
                    listMap[key] = items.toMutableList()
                } else {
                    currentKey = key
                    currentNestedSection = null
                    map[key] = cleanYamlValue(valuePart)
                }
            }
        }

        val name = map["name"] ?: throw IllegalArgumentException("Missing required 'name' in skill frontmatter")
        val description = map["description"] ?: throw IllegalArgumentException("Missing required 'description' in skill frontmatter")
        val version = map["version"] ?: "1.0.0"
        val author = map["author"]
        val tags = listMap["tags"] ?: emptyList()
        val targets = listMap["targets"] ?: emptyList()
        val triggers = listMap["triggers"] ?: emptyList()

        val compatMap = nestedMap["compatibility"] ?: emptyMap()
        val compatibility = SkillCompatibility(
            kotlin = compatMap["kotlin"],
            compose = compatMap["compose"],
            gradle = compatMap["gradle"],
            toolchain = compatMap["toolchain"],
        )

        return SkillManifest(
            metadata = SkillMetadata(
                name = name,
                description = description,
                version = version,
                author = author,
                tags = tags,
                targets = targets,
                compatibility = compatibility,
                triggers = triggers,
            ),
            content = content
        )
    }

    fun serialize(manifest: SkillManifest): String {
        val sb = StringBuilder()
        sb.appendLine("---")
        sb.appendLine("name: ${manifest.metadata.name}")
        sb.appendLine("description: ${manifest.metadata.description}")
        sb.appendLine("version: ${manifest.metadata.version}")
        manifest.metadata.author?.let { sb.appendLine("author: $it") }

        if (manifest.metadata.tags.isNotEmpty()) {
            sb.appendLine("tags:")
            manifest.metadata.tags.forEach { sb.appendLine("  - $it") }
        }

        if (manifest.metadata.targets.isNotEmpty()) {
            sb.appendLine("targets:")
            manifest.metadata.targets.forEach { sb.appendLine("  - $it") }
        }

        if (manifest.metadata.triggers.isNotEmpty()) {
            sb.appendLine("triggers:")
            manifest.metadata.triggers.forEach { sb.appendLine("  - $it") }
        }

        val c = manifest.metadata.compatibility
        if (c.kotlin != null || c.compose != null || c.gradle != null || c.toolchain != null) {
            sb.appendLine("compatibility:")
            c.kotlin?.let { sb.appendLine("  kotlin: \"$it\"") }
            c.compose?.let { sb.appendLine("  compose: \"$it\"") }
            c.gradle?.let { sb.appendLine("  gradle: \"$it\"") }
            c.toolchain?.let { sb.appendLine("  toolchain: \"$it\"") }
        }

        sb.appendLine("---")
        sb.appendLine()
        sb.append(manifest.content.trim())
        sb.appendLine()
        return sb.toString()
    }

    private fun cleanYamlValue(value: String): String {
        var v = value.trim()
        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
            v = v.substring(1, v.length - 1)
        }
        return v
    }
}
