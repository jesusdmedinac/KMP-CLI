package com.jesusdmedinac.kmp.core.architecture

import com.jesusdmedinac.kmp.core.architecture.model.ArchitectureReport
import com.jesusdmedinac.kmp.core.architecture.model.RuleViolation
import com.jesusdmedinac.kmp.core.architecture.model.ViolationSeverity
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment

class ArchitectureRuleChecker(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
) {
    fun checkFile(filePath: String, content: String): List<RuleViolation> {
        val violations = mutableListOf<RuleViolation>()
        val lines = content.lines()

        val isPresentationLayer = filePath.contains("/presentation/") ||
                filePath.contains("presentation/") ||
                filePath.contains("/ui/") ||
                content.contains("@Composable")

        val isViewModel = filePath.endsWith("ViewModel.kt") ||
                content.contains("class ") && content.contains("ViewModel")

        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1
            val trimmed = line.trim()

            // Rule ARCH-001: UI-to-Data leak
            if (isPresentationLayer && trimmed.startsWith("import ")) {
                val importPath = trimmed.removePrefix("import ").trim().removeSuffix(";")
                val isDataLeak = importPath.contains(".data.") ||
                        importPath.contains(".dao.") ||
                        importPath.endsWith("Dao") ||
                        importPath.endsWith("RepositoryImpl") ||
                        importPath.startsWith("androidx.room.") ||
                        importPath.startsWith("app.cash.sqldelight.")

                if (isDataLeak) {
                    violations.add(
                        RuleViolation(
                            ruleId = "ARCH001_UI_DATA_LEAK",
                            severity = ViolationSeverity.ERROR,
                            filePath = filePath,
                            lineNumber = lineNumber,
                            message = "Direct data layer or DAO import detected in presentation layer: '$importPath'",
                            remediation = "Inject the domain interface or use case via ViewModel instead of referencing data implementations or DAOs directly.",
                        )
                    )
                }
            }

            // Rule ARCH-002: MutableStateFlow public exposure
            if (isViewModel && trimmed.contains("MutableStateFlow")) {
                val hasValOrVar = trimmed.contains("val ") || trimmed.contains("var ")
                val isPrivate = trimmed.startsWith("private ") || trimmed.contains(" private ")
                if (hasValOrVar && !isPrivate) {
                    violations.add(
                        RuleViolation(
                            ruleId = "ARCH002_MUTABLE_STATE_EXPOSURE",
                            severity = ViolationSeverity.ERROR,
                            filePath = filePath,
                            lineNumber = lineNumber,
                            message = "ViewModel exposes MutableStateFlow directly to the public API.",
                            remediation = "Expose state as read-only StateFlow using 'private val _state = MutableStateFlow(...)' and 'val state: StateFlow<...> = _state.asStateFlow()'.",
                        )
                    )
                }
            }
        }

        return violations
    }

    fun auditModuleYaml(
        moduleYamlPath: String,
        moduleYamlContent: String,
        sourceFilesContent: Map<String, String>,
    ): List<RuleViolation> {
        val violations = mutableListOf<RuleViolation>()
        val lines = moduleYamlContent.lines()

        var inDependencies = false
        val declaredDeps = mutableListOf<Pair<Int, String>>()

        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed == "dependencies:" || trimmed.startsWith("dependencies:")) {
                inDependencies = true
                continue
            }
            if (inDependencies) {
                if (trimmed.startsWith("- ")) {
                    val depCoordinate = trimmed.removePrefix("- ").trim().trim('"', '\'')
                    declaredDeps.add(Pair(index + 1, depCoordinate))
                } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    inDependencies = false
                }
            }
        }

        val allImports = mutableListOf<Pair<String, Pair<Int, String>>>()
        for ((filePath, content) in sourceFilesContent) {
            content.lines().forEachIndexed { lineIdx, line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("import ")) {
                    val imp = trimmed.removePrefix("import ").trim().removeSuffix(";")
                    allImports.add(Pair(filePath, Pair(lineIdx + 1, imp)))
                }
            }
        }

        // Check for unused dependencies declared in module.yaml
        for ((lineNum, dep) in declaredDeps) {
            if (dep.startsWith("$") || dep.startsWith("kotlin")) continue

            // Determine search token from dependency (e.g., group or name)
            val parts = dep.split(":")
            val searchTokens = if (parts.size >= 2) {
                listOf(parts[0], parts[1].replace("-", "."))
            } else {
                listOf(dep)
            }

            val isUsed = allImports.any { (_, importPair) ->
                searchTokens.any { token -> importPair.second.contains(token) }
            } || sourceFilesContent.values.any { content ->
                searchTokens.any { token -> content.contains(token) }
            }

            if (!isUsed) {
                violations.add(
                    RuleViolation(
                        ruleId = "ARCH003_UNUSED_DEPENDENCY",
                        severity = ViolationSeverity.WARNING,
                        filePath = moduleYamlPath,
                        lineNumber = lineNum,
                        message = "Unused dependency coordinate '$dep' declared in module.yaml.",
                        remediation = "Remove '$dep' from module.yaml or implement the planned integration.",
                    )
                )
            }
        }

        // Check for undeclared external library imports
        for ((filePath, importPair) in allImports) {
            val (lineNum, imp) = importPair
            if (imp.startsWith("kotlin.") ||
                imp.startsWith("kotlinx.coroutines.") ||
                imp.startsWith("kotlinx.serialization.") ||
                imp.startsWith("androidx.compose.") ||
                imp.startsWith("java.") ||
                imp.startsWith("android.") ||
                imp.startsWith("com.jesusdmedinac.") ||
                imp.startsWith("org.jetbrains.compose.")
            ) {
                continue
            }

            val isDeclared = declaredDeps.any { (_, dep) ->
                val parts = dep.split(":")
                parts.any { part -> imp.startsWith(part) || imp.contains(part) }
            }

            if (!isDeclared) {
                violations.add(
                    RuleViolation(
                        ruleId = "ARCH003_UNDECLARED_DEPENDENCY",
                        severity = ViolationSeverity.ERROR,
                        filePath = filePath,
                        lineNumber = lineNum,
                        message = "Undeclared external library import '$imp' not declared in module.yaml.",
                        remediation = "Declare dependency coordinate for '$imp' in module.yaml.",
                    )
                )
            }
        }

        return violations
    }

    fun checkProject(projectPath: String = "."): ArchitectureReport {
        val allFiles = systemEnvironment.listFilesRecursively(projectPath)
        val ktFiles = mutableMapOf<String, String>()
        var moduleYamlPath: String? = null
        var moduleYamlContent: String? = null

        val normalizedPrefix = if (projectPath == "." || projectPath.isEmpty()) "" else projectPath.trimEnd('/') + "/"

        for (relPath in allFiles) {
            val fullPath = if (relPath.startsWith("/")) relPath else "$normalizedPrefix$relPath"
            if (relPath.endsWith(".kt")) {
                systemEnvironment.readFileText(fullPath)?.let { ktFiles[fullPath] = it }
            } else if (relPath.endsWith("module.yaml") || fullPath.endsWith("module.yaml")) {
                moduleYamlPath = fullPath
                moduleYamlContent = systemEnvironment.readFileText(fullPath)
            }
        }

        // Check project root directly if not found in list
        if (moduleYamlPath == null) {
            val directModuleYaml = "${normalizedPrefix}module.yaml"
            if (systemEnvironment.fileExists(directModuleYaml)) {
                moduleYamlPath = directModuleYaml
                moduleYamlContent = systemEnvironment.readFileText(directModuleYaml)
            }
        }

        val violations = mutableListOf<RuleViolation>()

        for ((filePath, content) in ktFiles) {
            violations.addAll(checkFile(filePath, content))
        }

        if (moduleYamlPath != null && moduleYamlContent != null) {
            violations.addAll(auditModuleYaml(moduleYamlPath, moduleYamlContent, ktFiles))
        }

        val passed = violations.none { it.severity == ViolationSeverity.ERROR }
        return ArchitectureReport(
            passed = passed,
            violations = violations,
            scannedFilesCount = ktFiles.size + (if (moduleYamlPath != null) 1 else 0),
        )
    }
}
