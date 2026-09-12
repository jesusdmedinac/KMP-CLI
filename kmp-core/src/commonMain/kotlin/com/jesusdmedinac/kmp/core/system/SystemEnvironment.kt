package com.jesusdmedinac.kmp.core.system

interface SystemEnvironment {
    fun execute(command: List<String>): ProcessResult
    fun getEnv(name: String): String?
    fun fileExists(path: String): Boolean
    fun nowIso8601(): String
    val operatingSystem: OperatingSystem
}

expect fun createDefaultSystemEnvironment(): SystemEnvironment
