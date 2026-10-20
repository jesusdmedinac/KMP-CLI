package com.jesusdmedinac.kmp.core.mcp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class McpServerInfo(
    val name: String = "kmp-cli",
    val version: String = "0.1.0-SNAPSHOT",
)

@Serializable
data class McpToolCapabilities(
    val listChanged: Boolean = false,
)

@Serializable
data class McpCapabilities(
    val tools: McpToolCapabilities = McpToolCapabilities(),
)

@Serializable
data class McpInitializeResult(
    val protocolVersion: String = "2024-11-05",
    val capabilities: McpCapabilities = McpCapabilities(),
    val serverInfo: McpServerInfo = McpServerInfo(),
)

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject,
)

@Serializable
data class McpToolsListResult(
    val tools: List<McpTool>,
)

@Serializable
data class McpToolContent(
    val type: String = "text",
    val text: String,
)

@Serializable
data class McpToolResult(
    val content: List<McpToolContent>,
    val isError: Boolean = false,
)

@Serializable
data class McpError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null,
)

@Serializable
data class McpResponse(
    val jsonrpc: String = "2.0",
    val id: JsonElement? = null,
    val result: JsonElement? = null,
    val error: McpError? = null,
)
