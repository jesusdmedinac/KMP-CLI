package com.jesusdmedinac.kmp.cli.command

import com.jesusdmedinac.kmp.core.mcp.McpServerEngine
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class McpCommandTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val engine = McpServerEngine(systemEnvironment = fakeEnv)
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `mcp command reads lines from stdin and outputs json-rpc responses to stdout`() {
        val inputLines = mutableListOf(
            """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{}}}""",
            """{"jsonrpc":"2.0","id":2,"method":"ping"}""",
        )
        val outputLines = mutableListOf<String>()

        val command = McpCommand(
            serverEngine = engine,
            lineReader = { if (inputLines.isNotEmpty()) inputLines.removeAt(0) else null },
            lineWriter = { outputLines.add(it) },
        )

        command.run()

        assertEquals(2, outputLines.size)

        val initResponse = json.parseToJsonElement(outputLines[0]).jsonObject
        assertEquals("2.0", initResponse["jsonrpc"]?.jsonPrimitive?.content)
        assertEquals(1, initResponse["id"]?.jsonPrimitive?.int)
        val serverInfo = initResponse["result"]?.jsonObject?.get("serverInfo")?.jsonObject
        assertEquals("kmp-cli", serverInfo?.get("name")?.jsonPrimitive?.content)

        val pingResponse = json.parseToJsonElement(outputLines[1]).jsonObject
        assertEquals("2.0", pingResponse["jsonrpc"]?.jsonPrimitive?.content)
        assertEquals(2, pingResponse["id"]?.jsonPrimitive?.int)
    }

    @Test
    fun `mcp command ignores notifications without writing to output`() {
        val inputLines = mutableListOf(
            """{"jsonrpc":"2.0","method":"notifications/initialized"}""",
            """{"jsonrpc":"2.0","id":10,"method":"ping"}""",
        )
        val outputLines = mutableListOf<String>()

        val command = McpCommand(
            serverEngine = engine,
            lineReader = { if (inputLines.isNotEmpty()) inputLines.removeAt(0) else null },
            lineWriter = { outputLines.add(it) },
        )

        command.run()

        assertEquals(1, outputLines.size)
        val pingResponse = json.parseToJsonElement(outputLines[0]).jsonObject
        assertEquals(10, pingResponse["id"]?.jsonPrimitive?.int)
    }
}
