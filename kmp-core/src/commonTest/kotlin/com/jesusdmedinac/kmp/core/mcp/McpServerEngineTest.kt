package com.jesusdmedinac.kmp.core.mcp

import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.*
import kotlin.test.*

class McpServerEngineTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val json = Json { ignoreUnknownKeys = true }
    private val engine = McpServerEngine(systemEnvironment = fakeEnv)

    @Test
    fun `initialize request negotiates 2024-11-05 protocol version and declares tools capability`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 1,
                "method": "initialize",
                "params": {
                    "protocolVersion": "2024-11-05",
                    "clientInfo": {
                        "name": "test-client",
                        "version": "1.0.0"
                    },
                    "capabilities": {}
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        assertEquals("2.0", responseObj["jsonrpc"]?.jsonPrimitive?.content)
        assertEquals(1, responseObj["id"]?.jsonPrimitive?.int)

        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)
        assertEquals("2024-11-05", result["protocolVersion"]?.jsonPrimitive?.content)

        val serverInfo = result["serverInfo"]?.jsonObject
        assertNotNull(serverInfo)
        assertEquals("kmp-cli", serverInfo["name"]?.jsonPrimitive?.content)

        val capabilities = result["capabilities"]?.jsonObject
        assertNotNull(capabilities)
        assertTrue(capabilities.containsKey("tools"))
    }

    @Test
    fun `tools list returns all 9 core diagnostic skills and migration tools with input schemas`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 2,
                "method": "tools/list"
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)

        val tools = result["tools"]?.jsonArray
        assertNotNull(tools)
        assertEquals(9, tools.size)

        val toolNames = tools.map { it.jsonObject["name"]?.jsonPrimitive?.content }
        assertTrue(toolNames.contains("kmp_doctor"))
        assertTrue(toolNames.contains("kmp_doctor_architecture"))
        assertTrue(toolNames.contains("kmp_describe"))
        assertTrue(toolNames.contains("kmp_analyze_dependencies"))
        assertTrue(toolNames.contains("kmp_analyze_libraries"))
        assertTrue(toolNames.contains("kmp_list_skills"))
        assertTrue(toolNames.contains("kmp_add_skill"))
        assertTrue(toolNames.contains("kmp_migrate_audit"))
        assertTrue(toolNames.contains("kmp_migrate_plan"))

        for (tool in tools) {
            val schema = tool.jsonObject["inputSchema"]?.jsonObject
            assertNotNull(schema)
            assertEquals("object", schema["type"]?.jsonPrimitive?.content)
        }
    }

    @Test
    fun `tool call kmp_doctor returns structured diagnostic report`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 3,
                "method": "tools/call",
                "params": {
                    "name": "kmp_doctor",
                    "arguments": {}
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)

        val content = result["content"]?.jsonArray
        assertNotNull(content)
        assertTrue(content.isNotEmpty())
        val text = content[0].jsonObject["text"]?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("diagnostics") || text.contains("overallStatus"))
    }

    @Test
    fun `tool call kmp_describe on valid project returns targets and runners`() {
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            kotlin {
                jvm()
                androidTarget()
            }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "src/androidMain/kotlin/com/example/MainActivity.kt",
            "class MainActivity : ComponentActivity()"
        )

        val request = """
            {
                "jsonrpc": "2.0",
                "id": 4,
                "method": "tools/call",
                "params": {
                    "name": "kmp_describe",
                    "arguments": {
                        "path": "."
                    }
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)

        val text = result["content"]?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("platformRunners"))
        assertTrue(text.contains("android"))
    }

    @Test
    fun `tool call kmp_doctor_architecture returns clean architecture report`() {
        fakeEnv.writeFileText(
            "composeApp/src/commonMain/kotlin/com/example/ui/SampleScreen.kt",
            """
            package com.example.ui
            fun SampleScreen() {}
            """.trimIndent()
        )

        val request = """
            {
                "jsonrpc": "2.0",
                "id": 11,
                "method": "tools/call",
                "params": {
                    "name": "kmp_doctor_architecture",
                    "arguments": {
                        "path": "."
                    }
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val text = responseObj["result"]?.jsonObject?.get("content")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("scannedFilesCount") || text.contains("passed"))
    }

    @Test
    fun `tool call kmp_analyze_dependencies parses version catalog toml`() {
        fakeEnv.writeFileText(
            "gradle/libs.versions.toml",
            """
            [versions]
            kotlin = "2.1.0"
            [libraries]
            ktor-client = { module = "io.ktor:ktor-client-core", version = "3.1.1" }
            """.trimIndent()
        )

        val request = """
            {
                "jsonrpc": "2.0",
                "id": 12,
                "method": "tools/call",
                "params": {
                    "name": "kmp_analyze_dependencies",
                    "arguments": {
                        "path": "."
                    }
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val text = responseObj["result"]?.jsonObject?.get("content")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("ktor-client") || text.contains("libraries"))
    }

    @Test
    fun `tool call kmp_add_skill installs canonical skill`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 13,
                "method": "tools/call",
                "params": {
                    "name": "kmp_add_skill",
                    "arguments": {
                        "skill": "kotlin-kmp-router",
                        "destination": ".agents/skills"
                    }
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val text = responseObj["result"]?.jsonObject?.get("content")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("installed successfully") || text.contains("already exists"))
    }

    @Test
    fun `tool call kmp_analyze_libraries queries target compatibility from klibs catalog`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 5,
                "method": "tools/call",
                "params": {
                    "name": "kmp_analyze_libraries",
                    "arguments": {
                        "query": "room"
                    }
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val text = responseObj["result"]?.jsonObject?.get("content")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("Room KMP"))
        assertTrue(text.contains("androidx.room"))
    }

    @Test
    fun `tool call kmp_list_skills returns curated skills catalog`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 6,
                "method": "tools/call",
                "params": {
                    "name": "kmp_list_skills",
                    "arguments": {}
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val text = responseObj["result"]?.jsonObject?.get("content")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("kotlin-kmp-router") || text.contains("kotlin-concurrency-coroutines"))
    }

    @Test
    fun `unknown tool call returns error result`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 7,
                "method": "tools/call",
                "params": {
                    "name": "non_existent_tool",
                    "arguments": {}
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)
        assertEquals(true, result["isError"]?.jsonPrimitive?.boolean)
        val text = result["content"]?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        assertTrue(text?.contains("Unknown tool 'non_existent_tool'") == true)
    }

    @Test
    fun `ping request returns empty result`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 42,
                "method": "ping"
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        assertEquals("2.0", responseObj["jsonrpc"]?.jsonPrimitive?.content)
        assertEquals(42, responseObj["id"]?.jsonPrimitive?.int)

        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `unknown method returns JSON-RPC method not found error code -32601`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 99,
                "method": "unknown/method"
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        assertEquals(99, responseObj["id"]?.jsonPrimitive?.int)

        val error = responseObj["error"]?.jsonObject
        assertNotNull(error)
        assertEquals(-32601, error["code"]?.jsonPrimitive?.int)
        assertTrue(error["message"]?.jsonPrimitive?.content?.contains("Method not found") == true)
    }

    @Test
    fun `notification initialized produces no response`() {
        val notification = """
            {
                "jsonrpc": "2.0",
                "method": "notifications/initialized"
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(notification)
        assertNull(responseJson)
    }

    @Test
    fun `tool call kmp_migrate_audit returns structured migration report over MCP`() {
        fakeEnv.writeFileText(
            "package.json",
            """
            {
              "name": "McpRNApp",
              "dependencies": {
                "react-native": "0.72.0",
                "axios": "^1.0.0"
              }
            }
            """.trimIndent()
        )

        val request = """
            {
                "jsonrpc": "2.0",
                "id": 10,
                "method": "tools/call",
                "params": {
                    "name": "kmp_migrate_audit",
                    "arguments": {
                        "path": "."
                    }
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)

        val content = result["content"]?.jsonArray
        assertNotNull(content)
        val text = content.first().jsonObject["text"]?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("REACT_NATIVE"))
        assertTrue(text.contains("axios"))
        assertTrue(text.contains("kotlin-networking-ktor"))
    }

    @Test
    fun `tool call kmp_migrate_plan returns 4-phase Strangler Fig plan over MCP`() {
        val request = """
            {
                "jsonrpc": "2.0",
                "id": 11,
                "method": "tools/call",
                "params": {
                    "name": "kmp_migrate_plan",
                    "arguments": {
                        "path": ".",
                        "pathway": "native-ui-shared-core"
                    }
                }
            }
        """.trimIndent()

        val responseJson = engine.handleMessage(request)
        assertNotNull(responseJson)

        val responseObj = json.parseToJsonElement(responseJson).jsonObject
        val result = responseObj["result"]?.jsonObject
        assertNotNull(result)

        val content = result["content"]?.jsonArray
        assertNotNull(content)
        val text = content.first().jsonObject["text"]?.jsonPrimitive?.content
        assertNotNull(text)
        assertTrue(text.contains("NATIVE_UI_SHARED_CORE"))
        assertTrue(text.contains("Foundation & Core Bridge"))
        assertTrue(text.contains("Legacy Shell Deletion"))
    }
}
