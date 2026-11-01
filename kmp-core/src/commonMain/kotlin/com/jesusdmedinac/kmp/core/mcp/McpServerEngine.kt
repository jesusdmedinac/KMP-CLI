package com.jesusdmedinac.kmp.core.mcp

import com.jesusdmedinac.kmp.core.architecture.ArchitectureRuleChecker
import com.jesusdmedinac.kmp.core.architecture.model.ArchitectureReport
import com.jesusdmedinac.kmp.core.engine.DiagnosticEngine
import com.jesusdmedinac.kmp.core.klibs.KlibsCatalog
import com.jesusdmedinac.kmp.core.klibs.model.LibraryRecommendationResult
import com.jesusdmedinac.kmp.core.mcp.model.*
import com.jesusdmedinac.kmp.core.migration.MigrationEngine
import com.jesusdmedinac.kmp.core.migration.model.MigrationAuditReport
import com.jesusdmedinac.kmp.core.migration.model.MigrationExecuteOptions
import com.jesusdmedinac.kmp.core.migration.model.MigrationExecuteResult
import com.jesusdmedinac.kmp.core.migration.model.MigrationPlan
import com.jesusdmedinac.kmp.core.migration.model.MigrationStrategy
import com.jesusdmedinac.kmp.core.migration.model.ModernizationPathway
import com.jesusdmedinac.kmp.core.model.DiagnosticReport
import com.jesusdmedinac.kmp.core.project.model.ProjectDependencies
import com.jesusdmedinac.kmp.core.project.model.ProjectDescriptor
import com.jesusdmedinac.kmp.core.project.parser.UnifiedProjectParser
import com.jesusdmedinac.kmp.core.project.parser.VersionCatalogParser
import com.jesusdmedinac.kmp.core.skill.InstallResult
import com.jesusdmedinac.kmp.core.skill.SkillsRepository
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalog
import com.jesusdmedinac.kmp.core.skill.model.SkillCatalogEntry
import com.jesusdmedinac.kmp.core.system.SystemEnvironment
import com.jesusdmedinac.kmp.core.system.createDefaultSystemEnvironment
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class McpServerEngine(
    private val systemEnvironment: SystemEnvironment = createDefaultSystemEnvironment(),
    private val diagnosticsEngine: DiagnosticEngine = DiagnosticEngine(systemEnvironment),
    private val architectureChecker: ArchitectureRuleChecker = ArchitectureRuleChecker(systemEnvironment),
    private val projectParser: UnifiedProjectParser = UnifiedProjectParser(systemEnvironment),
    private val versionCatalogParser: VersionCatalogParser = VersionCatalogParser(systemEnvironment),
    private val klibsCatalog: KlibsCatalog = KlibsCatalog(),
    private val skillsRepository: SkillsRepository = SkillsRepository(systemEnvironment),
    private val migrationEngine: MigrationEngine = MigrationEngine(systemEnvironment),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val prettyJson = Json {
        encodeDefaults = true
        prettyPrint = true
    }

    fun handleMessage(rawMessage: String): String? {
        val trimmed = rawMessage.trim()
        if (trimmed.isEmpty()) return null

        val jsonElement = try {
            json.parseToJsonElement(trimmed)
        } catch (_: Exception) {
            return encodeResponse(McpResponse(error = McpError(-32700, "Parse error: Invalid JSON-RPC")))
        }

        if (jsonElement !is JsonObject) {
            return encodeResponse(McpResponse(error = McpError(-32600, "Invalid Request: Expected JSON object")))
        }

        val id = jsonElement["id"]
        val method = jsonElement["method"]?.jsonPrimitive?.contentOrNull
        val params = jsonElement["params"]?.jsonObject

        // If no method, invalid JSON-RPC
        if (method == null) {
            return encodeResponse(McpResponse(id = id, error = McpError(-32600, "Invalid Request: Missing 'method'")))
        }

        // Notification: notifications/initialized
        if (method.startsWith("notifications/") || method == "initialized") {
            return null
        }

        return when (method) {
            "initialize" -> handleInitialize(id)
            "ping" -> encodeResponse(McpResponse(id = id, result = buildJsonObject { }))
            "tools/list" -> handleToolsList(id)
            "tools/call" -> handleToolsCall(id, params)
            else -> encodeResponse(McpResponse(id = id, error = McpError(-32601, "Method not found: '$method'")))
        }
    }

    private fun handleInitialize(id: JsonElement?): String {
        val result = McpInitializeResult()
        return encodeResponse(McpResponse(id = id, result = json.encodeToJsonElement(result)))
    }

    private fun handleToolsList(id: JsonElement?): String {
        val tools = listOf(
            McpTool(
                name = "kmp_doctor",
                description = "Run full environment health diagnosis for JDK, Android SDK, and Kotlin Toolchain.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject { })
                },
            ),
            McpTool(
                name = "kmp_doctor_architecture",
                description = "Run clean architecture guard linting and check living foundation status across active targets.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("path", buildJsonObject {
                            put("type", "string")
                            put("description", "Path to project root (defaults to current directory)")
                        })
                    })
                },
            ),
            McpTool(
                name = "kmp_describe",
                description = "Inspect KMP project build system, declared targets, modules, and platform runners.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("path", buildJsonObject {
                            put("type", "string")
                            put("description", "Path to project root (defaults to current directory)")
                        })
                    })
                },
            ),
            McpTool(
                name = "kmp_analyze_dependencies",
                description = "Analyze project version catalog dependencies, plugins, and versions.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("path", buildJsonObject {
                            put("type", "string")
                            put("description", "Path to project root (defaults to current directory)")
                        })
                    })
                },
            ),
            McpTool(
                name = "kmp_analyze_libraries",
                description = "Query klibs.io multiplatform library compatibility and verify against project declared targets.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("query", buildJsonObject {
                            put("type", "string")
                            put("description", "Search query for library name, coordinate, or category (e.g. room, coil, ktor)")
                        })
                        put("path", buildJsonObject {
                            put("type", "string")
                            put("description", "Path to project root to evaluate declared targets against")
                        })
                    })
                    put("required", buildJsonArray { add("query") })
                },
            ),
            McpTool(
                name = "kmp_list_skills",
                description = "List curated canonical and grant-winner Agent Skills available in the KMP Skills Hub.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("query", buildJsonObject {
                            put("type", "string")
                            put("description", "Optional search term for skill ID, alias, or tag")
                        })
                    })
                },
            ),
            McpTool(
                name = "kmp_add_skill",
                description = "Install a certified KMP Agent Skill into a project.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("skill", buildJsonObject {
                            put("type", "string")
                            put("description", "Skill identifier, alias, or remote GitHub repository (owner/repo)")
                        })
                        put("destination", buildJsonObject {
                            put("type", "string")
                            put("description", "Target destination directory (defaults to current directory)")
                        })
                    })
                    put("required", buildJsonArray { add("skill") })
                },
            ),
            McpTool(
                name = "kmp_migrate_audit",
                description = "Audit React Native or Flutter cross-platform codebases, detect native bridge dependencies, and recommend modernization pathways.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("path", buildJsonObject {
                            put("type", "string")
                            put("description", "Path to legacy project root (defaults to current directory)")
                        })
                    })
                },
            ),
            McpTool(
                name = "kmp_migrate_plan",
                description = "Generate phased Strangler Fig migration roadmap (Phases 1-4) for migrating legacy codebase to KMP.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("path", buildJsonObject {
                            put("type", "string")
                            put("description", "Path to legacy project root (defaults to current directory)")
                        })
                        put("pathway", buildJsonObject {
                            put("type", "string")
                            put("description", "Target pathway override: 'dual-native', 'native-ui-shared-core', or 'compose-multiplatform'")
                        })
                    })
                },
            ),
            McpTool(
                name = "kmp_migrate_execute",
                description = "Execute cross-platform modernization: scaffold KMP app, synthesize libs.versions.toml, migrate assets, and emit migration manifest.",
                inputSchema = buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("sourcePath", buildJsonObject {
                            put("type", "string")
                            put("description", "Path to legacy project root (defaults to current directory)")
                        })
                        put("outputDir", buildJsonObject {
                            put("type", "string")
                            put("description", "Destination directory for the migrated KMP project")
                        })
                        put("package", buildJsonObject {
                            put("type", "string")
                            put("description", "Package ID / namespace for the new project")
                        })
                        put("pathway", buildJsonObject {
                            put("type", "string")
                            put("description", "Modernization pathway: 'compose-multiplatform', 'native-ui-shared-core', or 'dual-native'")
                        })
                        put("strategy", buildJsonObject {
                            put("type", "string")
                            put("description", "Migration strategy: 'autonomous' or 'guided'")
                        })
                    })
                    put("required", buildJsonArray { add("outputDir") })
                },
            ),
        )

        val result = McpToolsListResult(tools = tools)
        return encodeResponse(McpResponse(id = id, result = json.encodeToJsonElement(result)))
    }

    private fun handleToolsCall(id: JsonElement?, params: JsonObject?): String {
        if (params == null) {
            return encodeResponse(McpResponse(id = id, error = McpError(-32602, "Invalid params: Expected object")))
        }

        val toolName = params["name"]?.jsonPrimitive?.contentOrNull
        val arguments = params["arguments"]?.jsonObject ?: buildJsonObject { }

        if (toolName == null) {
            return encodeResponse(McpResponse(id = id, error = McpError(-32602, "Invalid params: Missing tool 'name'")))
        }

        val result = when (toolName) {
            "kmp_doctor" -> executeDoctor()
            "kmp_doctor_architecture" -> executeDoctorArchitecture(arguments)
            "kmp_describe" -> executeDescribe(arguments)
            "kmp_analyze_dependencies" -> executeAnalyzeDependencies(arguments)
            "kmp_analyze_libraries" -> executeAnalyzeLibraries(arguments)
            "kmp_list_skills" -> executeListSkills(arguments)
            "kmp_add_skill" -> executeAddSkill(arguments)
            "kmp_migrate_audit" -> executeMigrateAudit(arguments)
            "kmp_migrate_plan" -> executeMigratePlan(arguments)
            "kmp_migrate_execute" -> executeMigrateExecute(arguments)
            else -> McpToolResult(
                content = listOf(McpToolContent(text = "Error: Unknown tool '$toolName'")),
                isError = true,
            )
        }

        return encodeResponse(McpResponse(id = id, result = json.encodeToJsonElement(result)))
    }

    private fun executeDoctor(): McpToolResult {
        val report = runBlocking { diagnosticsEngine.diagnose() }
        return McpToolResult(
            content = listOf(McpToolContent(text = prettyJson.encodeToString<DiagnosticReport>(report))),
        )
    }

    private fun executeDoctorArchitecture(args: JsonObject): McpToolResult {
        val path = args["path"]?.jsonPrimitive?.contentOrNull ?: "."
        val report = architectureChecker.checkProject(path)
        return McpToolResult(
            content = listOf(McpToolContent(text = prettyJson.encodeToString<ArchitectureReport>(report))),
        )
    }

    private fun executeDescribe(args: JsonObject): McpToolResult {
        val path = args["path"]?.jsonPrimitive?.contentOrNull ?: "."
        val descriptor = projectParser.parse(path)
        return if (descriptor != null) {
            McpToolResult(
                content = listOf(McpToolContent(text = prettyJson.encodeToString<ProjectDescriptor>(descriptor))),
            )
        } else {
            McpToolResult(
                content = listOf(McpToolContent(text = "Error: No recognized Kotlin Multiplatform project found at '$path'")),
                isError = true,
            )
        }
    }

    private fun executeAnalyzeDependencies(args: JsonObject): McpToolResult {
        val path = args["path"]?.jsonPrimitive?.contentOrNull ?: "."
        val tomlPath = when {
            path == "." || path.isEmpty() -> "gradle/libs.versions.toml"
            path.endsWith("libs.versions.toml") -> path
            else -> "${path.trimEnd('/')}/gradle/libs.versions.toml"
        }
        val catalog = versionCatalogParser.parse(tomlPath)
        return if (catalog != null) {
            McpToolResult(
                content = listOf(McpToolContent(text = prettyJson.encodeToString<ProjectDependencies>(catalog))),
            )
        } else {
            McpToolResult(
                content = listOf(McpToolContent(text = "Error: No version catalog found at '$tomlPath'")),
                isError = true,
            )
        }
    }

    private fun executeAnalyzeLibraries(args: JsonObject): McpToolResult {
        val query = args["query"]?.jsonPrimitive?.contentOrNull ?: ""
        val path = args["path"]?.jsonPrimitive?.contentOrNull ?: "."
        val descriptor = projectParser.parse(path)
        val targets = descriptor?.targets ?: emptyList()
        val result = klibsCatalog.query(query = query, projectTargets = targets)
        return McpToolResult(
            content = listOf(McpToolContent(text = prettyJson.encodeToString<LibraryRecommendationResult>(result))),
        )
    }

    private fun executeListSkills(args: JsonObject): McpToolResult {
        val query = args["query"]?.jsonPrimitive?.contentOrNull
        val skills = if (query.isNullOrBlank()) {
            skillsRepository.getCatalog().skills
        } else {
            skillsRepository.findSkills(query)
        }
        return McpToolResult(
            content = listOf(McpToolContent(text = prettyJson.encodeToString<List<SkillCatalogEntry>>(skills))),
        )
    }

    private fun executeAddSkill(args: JsonObject): McpToolResult {
        val skill = args["skill"]?.jsonPrimitive?.contentOrNull
        if (skill.isNullOrBlank()) {
            return McpToolResult(
                content = listOf(McpToolContent(text = "Error: Missing required argument 'skill'")),
                isError = true,
            )
        }
        val destination = args["destination"]?.jsonPrimitive?.contentOrNull
        val result = skillsRepository.installSkill(skill, destination)
        return when (result) {
            is InstallResult.Success -> McpToolResult(
                content = listOf(McpToolContent(text = "Skill '${result.skillId}' installed successfully into '${result.installedPath}'")),
            )
            is InstallResult.AlreadyExists -> McpToolResult(
                content = listOf(McpToolContent(text = "Skill '${result.skillId}' already exists at '${result.path}' (use force to overwrite)")),
            )
            is InstallResult.NotFound -> McpToolResult(
                content = listOf(McpToolContent(text = "Error: Skill '${result.skillId}' not found in catalog")),
                isError = true,
            )
            is InstallResult.Error -> McpToolResult(
                content = listOf(McpToolContent(text = "Error: Failed to install skill '${result.skillId}': ${result.message}")),
                isError = true,
            )
        }
    }

    private fun executeMigrateAudit(args: JsonObject): McpToolResult {
        val path = args["path"]?.jsonPrimitive?.contentOrNull ?: "."
        val report = migrationEngine.audit(path)
        return McpToolResult(
            content = listOf(McpToolContent(text = prettyJson.encodeToString<MigrationAuditReport>(report))),
        )
    }

    private fun executeMigratePlan(args: JsonObject): McpToolResult {
        val path = args["path"]?.jsonPrimitive?.contentOrNull ?: "."
        val pathwayStr = args["pathway"]?.jsonPrimitive?.contentOrNull
        val override = pathwayStr?.let {
            when (it.lowercase()) {
                "dual-native" -> ModernizationPathway.DUAL_NATIVE
                "native-ui-shared-core" -> ModernizationPathway.NATIVE_UI_SHARED_CORE
                "compose-multiplatform" -> ModernizationPathway.COMPOSE_MULTIPLATFORM
                else -> null
            }
        }
        val plan = migrationEngine.plan(path, override)
        return McpToolResult(
            content = listOf(McpToolContent(text = prettyJson.encodeToString<MigrationPlan>(plan))),
        )
    }

    private fun executeMigrateExecute(args: JsonObject): McpToolResult {
        val sourcePath = args["sourcePath"]?.jsonPrimitive?.contentOrNull ?: "."
        val outputDir = args["outputDir"]?.jsonPrimitive?.contentOrNull ?: "${sourcePath.trimEnd('/', '\\').substringAfterLast('/')}Kmp"
        val packageName = args["package"]?.jsonPrimitive?.contentOrNull
        val pathwayStr = args["pathway"]?.jsonPrimitive?.contentOrNull
        val strategyStr = args["strategy"]?.jsonPrimitive?.contentOrNull ?: "autonomous"

        val strategy = when (strategyStr.lowercase()) {
            "guided" -> MigrationStrategy.GUIDED
            else -> MigrationStrategy.AUTONOMOUS
        }

        val pathway = pathwayStr?.let {
            when (it.lowercase().replace('_', '-')) {
                "dual-native" -> ModernizationPathway.DUAL_NATIVE
                "native-ui-shared-core" -> ModernizationPathway.NATIVE_UI_SHARED_CORE
                "compose-multiplatform" -> ModernizationPathway.COMPOSE_MULTIPLATFORM
                else -> null
            }
        }

        val options = MigrationExecuteOptions(
            sourcePath = sourcePath,
            outputDir = outputDir,
            packageName = packageName,
            pathway = pathway,
            strategy = strategy,
        )

        val result = migrationEngine.execute(options)
        return McpToolResult(
            content = listOf(McpToolContent(text = prettyJson.encodeToString<MigrationExecuteResult>(result))),
            isError = result.status != "SUCCESS",
        )
    }

    private fun encodeResponse(response: McpResponse): String {
        return json.encodeToString(response)
    }
}
