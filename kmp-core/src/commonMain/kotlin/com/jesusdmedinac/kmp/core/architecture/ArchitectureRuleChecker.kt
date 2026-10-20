package com.jesusdmedinac.kmp.core.architecture

import com.jesusdmedinac.kmp.core.architecture.model.ArchitectureReport
import com.jesusdmedinac.kmp.core.architecture.model.RuleViolation
import com.jesusdmedinac.kmp.core.architecture.model.ViolationSeverity
import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser
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
                lines.any { line ->
                    val t = line.trim()
                    (t.startsWith("class ") || t.startsWith("abstract class ") || t.startsWith("open class ")) &&
                            (t.contains("ViewModel") || t.contains(": ViewModel"))
                }

        var inTopLevelDeclaration = false
        var inBlockComment = false
        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1
            val trimmed = line.trim()

            if (trimmed.startsWith("/*")) {
                inBlockComment = !trimmed.contains("*/")
                continue
            }
            if (inBlockComment) {
                if (trimmed.contains("*/")) {
                    inBlockComment = false
                }
                continue
            }
            if (trimmed.startsWith("//") || trimmed.startsWith("*")) {
                continue
            }

            if (!inTopLevelDeclaration) {
                if (trimmed.startsWith("class ") || trimmed.startsWith("interface ") ||
                    trimmed.startsWith("fun ") || trimmed.startsWith("val ") ||
                    trimmed.startsWith("var ") || trimmed.startsWith("object ") ||
                    trimmed.startsWith("enum ") || trimmed.startsWith("sealed ") ||
                    trimmed.startsWith("data class ") || trimmed.startsWith("abstract ")
                ) {
                    inTopLevelDeclaration = true
                }
            }

            // Rule ARCH-001: UI-to-Data leak
            if (isPresentationLayer && !inTopLevelDeclaration && trimmed.startsWith("import ")) {
                val importPath = trimmed.removePrefix("import ").trim().removeSuffix(";")
                val segments = importPath.split(".")
                val isDataPackage = segments.contains("data") && !segments.contains("domain")
                val isDataLeak = (isDataPackage && (importPath.endsWith("Dao") || importPath.endsWith("RepositoryImpl") || segments.contains("dao") || segments.contains("database"))) ||
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
                val isProperty = trimmed.startsWith("val ") || trimmed.startsWith("var ") ||
                        trimmed.startsWith("public val ") || trimmed.startsWith("public var ") ||
                        trimmed.startsWith("internal val ") || trimmed.startsWith("internal var ") ||
                        trimmed.startsWith("override val ") || trimmed.startsWith("override var ")
                val isPrivate = trimmed.startsWith("private ")
                if (isProperty && !isPrivate) {
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
            var inTopLevel = false
            var inBlockComment = false
            for ((lineIdx, line) in content.lines().withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("/*")) {
                    inBlockComment = !trimmed.contains("*/")
                    continue
                }
                if (inBlockComment) {
                    if (trimmed.contains("*/")) inBlockComment = false
                    continue
                }
                if (trimmed.startsWith("//") || trimmed.startsWith("*")) continue

                if (!inTopLevel) {
                    if (trimmed.startsWith("class ") || trimmed.startsWith("interface ") ||
                        trimmed.startsWith("fun ") || trimmed.startsWith("val ") ||
                        trimmed.startsWith("var ") || trimmed.startsWith("object ") ||
                        trimmed.startsWith("enum ") || trimmed.startsWith("sealed ") ||
                        trimmed.startsWith("data class ") || trimmed.startsWith("abstract ")
                    ) {
                        inTopLevel = true
                    }
                }

                if (!inTopLevel && trimmed.startsWith("import ")) {
                    val imp = trimmed.removePrefix("import ").trim().removeSuffix(";")
                    // Skip invalid Kotlin imports (e.g. JS/TS template strings)
                    if (imp.contains("{") || imp.contains("}") || imp.contains("from ") || imp.contains("'") || imp.contains("\"")) {
                        continue
                    }
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

        val declaredPackages = sourceFilesContent.values.mapNotNull { content ->
            content.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("package ") }
                ?.removePrefix("package ")
                ?.trim()
                ?.removeSuffix(";")
        }.filter { it.isNotBlank() }.toSet()

        val rootPackagePrefix = findCommonPackagePrefix(declaredPackages)

        // Check for undeclared external library imports
        for ((filePath, importPair) in allImports) {
            val (lineNum, imp) = importPair
            if (imp.startsWith("kotlin.") ||
                imp.startsWith("kotlinx.coroutines.") ||
                imp.startsWith("kotlinx.serialization.") ||
                imp.startsWith("kotlinx.cinterop.") ||
                imp.startsWith("platform.") ||
                imp.startsWith("androidx.compose.") ||
                imp.startsWith("java.") ||
                imp.startsWith("android.") ||
                imp.startsWith("org.jetbrains.compose.")
            ) {
                continue
            }

            val isInternal = declaredPackages.any { pkg -> imp == pkg || imp.startsWith("$pkg.") } ||
                    (rootPackagePrefix != null && (imp == rootPackagePrefix || imp.startsWith("$rootPackagePrefix.")))

            if (isInternal) {
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
            val isIgnored = relPath.startsWith("build/") || relPath.contains("/build/") ||
                    relPath.startsWith(".gradle/") || relPath.contains("/.gradle/") ||
                    relPath.startsWith(".git/") || relPath.contains("/.git/") ||
                    relPath.startsWith(".idea/") || relPath.contains("/.idea/") ||
                    relPath.startsWith("templates/") || relPath.contains("/templates/") ||
                    relPath.startsWith("playground/") || relPath.contains("/playground/") ||
                    relPath.startsWith("wizard/") || relPath.contains("/wizard/") ||
                    relPath.startsWith("jetbrains/") || relPath.contains("/jetbrains/") ||
                    relPath.startsWith(".kmp-cache/") || relPath.contains("/.kmp-cache/")

            if (isIgnored) continue

            val isTestFile = relPath.contains("/test/") ||
                    relPath.contains("/commonTest/") ||
                    relPath.contains("/jvmTest/") ||
                    relPath.contains("/macosArm64Test/") ||
                    relPath.contains("/linuxX64Test/") ||
                    relPath.endsWith("Test.kt")

            if (relPath.endsWith(".kt") && !isTestFile) {
                systemEnvironment.readFileText(fullPath)?.let { ktFiles[fullPath] = it }
            } else if (relPath == "module.yaml" || relPath == "app/module.yaml" || fullPath == "${normalizedPrefix}module.yaml") {
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

        val projectDescriptor = UnifiedProjectParser(systemEnvironment).parse(projectPath)
        if (projectDescriptor != null && isApplicationProject(projectDescriptor, projectPath, ktFiles)) {
            for (runner in projectDescriptor.platformRunners) {
                if (!runner.present) {
                    val suggestedPath = getSuggestedFilePathForTarget(runner.target, projectDescriptor, normalizedPrefix)
                    violations.add(
                        RuleViolation(
                            ruleId = "ARCH004_MISSING_PLATFORM_RUNNER",
                            severity = ViolationSeverity.WARNING,
                            filePath = suggestedPath,
                            lineNumber = 1,
                            message = "Target '${runner.target}' is declared in project configuration but lacks an executable platform runner entry point (Stage 1: Living Foundation).",
                            remediation = getBoilerplateRemediation(runner.target),
                        )
                    )
                }
            }
        }

        val passed = violations.none { it.severity == ViolationSeverity.ERROR }
        return ArchitectureReport(
            passed = passed,
            violations = violations,
            scannedFilesCount = ktFiles.size + (if (moduleYamlPath != null) 1 else 0),
        )
    }

    private fun isApplicationProject(
        descriptor: ProjectDescriptor,
        projectPath: String,
        ktFiles: Map<String, String>,
    ): Boolean {
        if (descriptor.platformRunners.any { it.present }) return true
        if (descriptor.modules.any { it.name.contains("app", ignoreCase = true) }) return true

        val root = if (projectPath == "." || projectPath.isEmpty()) "" else projectPath.trimEnd('/') + "/"
        val buildFiles = listOf(
            "${root}build.gradle.kts",
            "${root}build.gradle",
            "${root}app/build.gradle.kts",
            "${root}composeApp/build.gradle.kts",
            "${root}module.yaml",
            "${root}app/module.yaml",
        )
        for (path in buildFiles) {
            val content = systemEnvironment.readFileText(path) ?: continue
            if (content.contains("application") ||
                content.contains("com.android.application") ||
                content.contains("type: app") ||
                content.contains("product:\n  type: app") ||
                content.contains("org.jetbrains.compose")
            ) {
                return true
            }
        }

        if (ktFiles.values.any { it.contains("fun App(") || it.contains("fun main(") }) {
            return true
        }

        return false
    }

    private fun getSuggestedFilePathForTarget(target: String, descriptor: ProjectDescriptor, prefix: String): String {
        val normalized = target.lowercase()
        val hasComposeApp = descriptor.modules.any { it.name == "composeApp" }
        val moduleDir = if (hasComposeApp) "composeApp/" else ""
        return when {
            normalized.contains("android") -> "${prefix}${moduleDir}src/androidMain/kotlin/MainActivity.kt"
            normalized.startsWith("ios") || normalized.contains("apple") -> "${prefix}${moduleDir}src/iosMain/kotlin/MainViewController.kt"
            normalized == "desktop" || normalized == "jvm" -> "${prefix}${moduleDir}src/desktopMain/kotlin/main.kt"
            normalized.startsWith("wasm") || normalized == "js" || normalized.contains("web") -> "${prefix}${moduleDir}src/wasmJsMain/kotlin/main.kt"
            else -> "${prefix}src/${target}Main/kotlin/main.kt"
        }
    }

    private fun getBoilerplateRemediation(target: String): String {
        val normalized = target.lowercase()
        return when {
            normalized.contains("android") -> {
                "Establish Stage 1 Living Foundation: create Android entry point inheriting from ComponentActivity:\n" +
                "class MainActivity : ComponentActivity() {\n" +
                "    override fun onCreate(savedInstanceState: Bundle?) {\n" +
                "        super.onCreate(savedInstanceState)\n" +
                "        setContent { App() }\n" +
                "    }\n" +
                "}"
            }
            normalized.startsWith("ios") || normalized.contains("apple") -> {
                "Establish Stage 1 Living Foundation: create iOS Compose UI entry point in MainViewController.kt:\n" +
                "fun MainViewController() = ComposeUIViewController { App() }"
            }
            normalized == "desktop" || normalized == "jvm" -> {
                "Establish Stage 1 Living Foundation: create Desktop runner entry point with application window in main.kt:\n" +
                "fun main() = application {\n" +
                "    Window(onCloseRequest = ::exitApplication, title = \"App\") {\n" +
                "        App()\n" +
                "    }\n" +
                "}"
            }
            normalized.startsWith("wasm") || normalized == "js" || normalized.contains("web") -> {
                "Establish Stage 1 Living Foundation: create Wasm ComposeViewport entry point in main.kt:\n" +
                "@OptIn(ExperimentalComposeUiApi::class)\n" +
                "fun main() {\n" +
                "    ComposeViewport(document.body!!) { App() }\n" +
                "}"
            }
            else -> {
                "Establish Stage 1 Living Foundation: create an executable runner entry point for $target:\n" +
                "fun main() {\n" +
                "    println(\"Running $target\")\n" +
                "}"
            }
        }
    }

    private fun findCommonPackagePrefix(packages: Set<String>): String? {
        if (packages.isEmpty()) return null
        val splitPackages = packages.map { it.split(".") }
        val minLength = splitPackages.minOf { it.size }
        val common = mutableListOf<String>()
        for (i in 0 until minLength) {
            val segment = splitPackages.first()[i]
            if (splitPackages.all { it[i] == segment }) {
                common.add(segment)
            } else {
                break
            }
        }
        return if (common.isNotEmpty()) common.joinToString(".") else null
    }
}
