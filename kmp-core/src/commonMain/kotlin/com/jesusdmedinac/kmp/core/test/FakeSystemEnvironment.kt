package com.jesusdmedinac.kmp.core.test

import com.jesusdmedinac.kmp.core.system.OperatingSystem
import com.jesusdmedinac.kmp.core.system.ProcessResult
import com.jesusdmedinac.kmp.core.system.SystemEnvironment

class FakeSystemEnvironment(
    override var operatingSystem: OperatingSystem = OperatingSystem.MACOS,
) : SystemEnvironment {
    private val commandResponses = mutableMapOf<List<String>, ProcessResult>()
    private val environmentVariables = mutableMapOf<String, String>()
    private val files = mutableSetOf<String>()
    val executedCommands = mutableListOf<List<String>>()

    fun whenCommand(command: List<String>, respondWith: ProcessResult) {
        commandResponses[command] = respondWith
    }

    fun setEnv(key: String, value: String) {
        environmentVariables[key] = value
    }

    fun addFile(path: String) {
        files.add(path)
    }

    override fun execute(command: List<String>): ProcessResult {
        executedCommands.add(command)
        return commandResponses[command] ?: ProcessResult(
            exitCode = 127,
            stdout = "",
            stderr = "Command not found: ${command.joinToString(" ")}"
        )
    }

    override fun getEnv(name: String): String? = environmentVariables[name]

    override fun fileExists(path: String): Boolean = files.contains(path)

    val fileContents = mutableMapOf<String, String>()

    override fun readFileText(path: String): String? = fileContents[path]

    override fun writeFileText(path: String, content: String): Boolean {
        fileContents[path] = content
        files.add(path)
        return true
    }

    var fixedTimestamp: String = "2026-09-11T12:00:00Z"
    override fun nowIso8601(): String = fixedTimestamp
}
