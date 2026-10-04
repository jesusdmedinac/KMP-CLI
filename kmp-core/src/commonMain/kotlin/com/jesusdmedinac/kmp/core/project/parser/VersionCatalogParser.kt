package com.jesusdmedinac.kmp.core.project.parser

import com.jesusdmedinac.kmp.core.project.model.CatalogLibrary
import com.jesusdmedinac.kmp.core.project.model.CatalogPlugin
import com.jesusdmedinac.kmp.core.project.model.ProjectDependencies
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

/**
 * Parses Gradle TOML version catalogs (`gradle/libs.versions.toml`).
 *
 * Extracts versions, declared libraries, and plugins, automatically resolving
 * `version.ref` references against the parsed `[versions]` table.
 */
class VersionCatalogParser(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    /**
     * Parses the TOML version catalog at [path].
     *
     * @param path Relative or absolute path to the TOML file. Defaults to `gradle/libs.versions.toml`.
     * @return A [ProjectDependencies] descriptor if the file exists and is valid, or `null` otherwise.
     */
    fun parse(path: String = "gradle/libs.versions.toml"): ProjectDependencies? {
        if (!systemEnvironment.fileExists(path)) return null
        val content = systemEnvironment.readFileText(path) ?: return null

        val versions = mutableMapOf<String, String>()
        val libraries = mutableListOf<CatalogLibrary>()
        val plugins = mutableListOf<CatalogPlugin>()

        var currentSection = ""

        for (rawLine in content.lines()) {
            val line = rawLine.substringBefore("#").trim()
            if (line.isEmpty()) continue

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.removeSurrounding("[", "]").trim().lowercase()
                continue
            }

            if (!line.contains("=")) continue
            val key = line.substringBefore("=").trim()
            val value = line.substringAfter("=").trim()

            when (currentSection) {
                "versions" -> {
                    val cleanVersion = value.removeSurrounding("\"").removeSurrounding("'")
                    versions[key] = cleanVersion
                }

                "libraries" -> {
                    val lib = parseLibraryEntry(key, value, versions)
                    if (lib != null) libraries.add(lib)
                }

                "plugins" -> {
                    val plugin = parsePluginEntry(key, value, versions)
                    if (plugin != null) plugins.add(plugin)
                }
            }
        }

        return ProjectDependencies(
            libraries = libraries,
            plugins = plugins,
            versions = versions,
        )
    }

    private fun parseLibraryEntry(
        alias: String,
        value: String,
        versions: Map<String, String>,
    ): CatalogLibrary? {
        if (value.startsWith("{") && value.endsWith("}")) {
            val inner = value.removeSurrounding("{", "}").trim()
            val pairs = parseKeyValuePairs(inner)

            val module = pairs["module"]
            val group = pairs["group"]
            val name = pairs["name"]
            val versionRef = pairs["version.ref"]
            val directVersion = pairs["version"]

            val resolvedVersion = directVersion ?: (versionRef?.let { versions[it] })

            return CatalogLibrary(
                alias = alias,
                group = group,
                name = name,
                module = module,
                version = resolvedVersion,
            )
        } else if (value.startsWith("\"") && value.endsWith("\"")) {
            val str = value.removeSurrounding("\"")
            val parts = str.split(":")
            return when (parts.size) {
                3 -> CatalogLibrary(
                    alias = alias,
                    group = parts[0],
                    name = parts[1],
                    version = parts[2],
                )
                2 -> CatalogLibrary(
                    alias = alias,
                    group = parts[0],
                    name = parts[1],
                )
                else -> CatalogLibrary(alias = alias, module = str)
            }
        }
        return null
    }

    private fun parsePluginEntry(
        alias: String,
        value: String,
        versions: Map<String, String>,
    ): CatalogPlugin? {
        if (value.startsWith("{") && value.endsWith("}")) {
            val inner = value.removeSurrounding("{", "}").trim()
            val pairs = parseKeyValuePairs(inner)

            val id = pairs["id"] ?: return null
            val versionRef = pairs["version.ref"]
            val directVersion = pairs["version"]
            val resolvedVersion = directVersion ?: (versionRef?.let { versions[it] })

            return CatalogPlugin(
                alias = alias,
                id = id,
                version = resolvedVersion,
            )
        }
        return null
    }

    private fun parseKeyValuePairs(content: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (match in KEY_VALUE_REGEX.findAll(content)) {
            val k = match.groupValues[1]
            val v = match.groupValues[2].removeSurrounding("\"").removeSurrounding("'")
            map[k] = v
        }
        return map
    }

    companion object {
        private val KEY_VALUE_REGEX = Regex("""([a-zA-Z0-9_.-]+)\s*=\s*("[^"]*"|'[^']*'|[a-zA-Z0-9_.-]+)""")
    }
}
