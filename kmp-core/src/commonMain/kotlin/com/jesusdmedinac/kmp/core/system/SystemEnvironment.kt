package com.jesusdmedinac.kmp.core.system

interface SystemEnvironment {
    fun execute(command: List<String>): ProcessResult
    fun getEnv(name: String): String?
    fun fileExists(path: String): Boolean
    fun readFileText(path: String): String?
    fun writeFileText(path: String, content: String): Boolean
    fun nowIso8601(): String
    fun listFilesRecursively(dir: String): List<String>
    fun setExecutable(path: String): Boolean
    fun copyFile(source: String, destination: String): Boolean
    val operatingSystem: OperatingSystem
}

expect fun createDefaultSystemEnvironment(): SystemEnvironment
