package com.jesusdmedinac.kmp.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.jesusdmedinac.kmp.core.mcp.McpServerEngine

/**
 * Runs kmp-cli as a native stdio Model Context Protocol (MCP) server.
 * Enables zero-dependency tool calling for AI coding assistants (Claude Desktop, Cursor, Antigravity).
 */
class McpCommand(
    private val serverEngine: McpServerEngine = McpServerEngine(),
    private val lineReader: () -> String? = { readlnOrNull() },
    private val lineWriter: (String) -> Unit = { println(it) },
) : CliktCommand(
    name = "mcp",
) {
    override fun help(context: Context): String =
        "Start native Model Context Protocol (MCP) server over standard I/O (stdio) for AI coding agents."

    override fun run() {
        while (true) {
            val line = lineReader() ?: break
            val response = serverEngine.handleMessage(line)
            if (response != null) {
                lineWriter(response)
            }
        }
    }
}
