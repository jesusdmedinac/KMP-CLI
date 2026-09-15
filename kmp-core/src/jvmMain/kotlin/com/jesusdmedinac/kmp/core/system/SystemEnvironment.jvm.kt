package com.jesusdmedinac.kmp.core.system

import java.io.File

class JvmSystemEnvironment : SystemEnvironment {
    override fun execute(command: List<String>): ProcessResult {
        return try {
            val process = ProcessBuilder(command).start()
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            ProcessResult(exitCode, stdout.trim(), stderr.trim())
        } catch (e: Exception) {
            ProcessResult(-1, "", e.message ?: "Process execution failed")
        }
    }

    override fun getEnv(name: String): String? = System.getenv(name)

    override fun fileExists(path: String): Boolean = File(path).exists()

    override fun readFileText(path: String): String? = try {
        val file = File(path)
        if (file.exists()) file.readText() else null
    } catch (e: Exception) {
        null
    }

    override fun writeFileText(path: String, content: String): Boolean = try {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
        true
    } catch (e: Exception) {
        false
    }

    override fun nowIso8601(): String = java.time.Instant.now().toString()

    override val operatingSystem: OperatingSystem
        get() {
            val osName = System.getProperty("os.name")?.lowercase() ?: ""
            return when {
                osName.contains("mac") -> OperatingSystem.MACOS
                osName.contains("linux") -> OperatingSystem.LINUX
                osName.contains("win") -> OperatingSystem.WINDOWS
                else -> OperatingSystem.UNKNOWN
            }
        }
}

actual fun createDefaultSystemEnvironment(): SystemEnvironment = JvmSystemEnvironment()
