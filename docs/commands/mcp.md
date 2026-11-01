# `kmp mcp`

A native **Model Context Protocol (MCP)** stdio server that connects `kmp-cli` directly to AI coding agents (Claude Desktop, Antigravity, Cursor, Windsurf, Cline).

---

## What is MCP?

The Model Context Protocol (MCP) is an open standard that enables AI models to discover and invoke tools, query environment state, and read structured project metadata safely.

---

## Starting the Server

```bash
kmp mcp
```

This starts a JSON-RPC 2.0 communication loop over standard input/output (`stdio`).

---

## Adding `kmp-cli` to Your AI Agent

=== "Claude Desktop (`claude_desktop_config.json`)"
    ```json
    {
      "mcpServers": {
        "kmp-cli": {
          "command": "kmp",
          "args": ["mcp"]
        }
      }
    }
    ```

=== "Cursor (`.cursor/mcp.json`)"
    ```json
    {
      "mcpServers": {
        "kmp-cli": {
          "command": "kmp",
          "args": ["mcp"]
        }
      }
    }
    ```

=== "Google Antigravity (`mcp_config.json`)"
    ```json
    {
      "mcpServers": {
        "kmp-cli": {
          "command": "kmp",
          "args": ["mcp"]
        }
      }
    }
    ```

---

## Exposed MCP Tools

- `kmp_doctor`: Execute environment health checks and return structured diagnostic JSON.
- `kmp_describe`: Inspect project architecture, active build system, and declared target platforms.
- `kmp_analyze_dependencies`: Audit declared dependencies against KMP compatibility catalog.
- `kmp_scaffold_project`: Scaffold new multiplatform modules and projects with targeted templates.
