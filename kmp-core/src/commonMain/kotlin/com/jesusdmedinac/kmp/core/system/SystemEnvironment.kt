package com.jesusdmedinac.kmp.core.system

interface SystemEnvironment {
    fun execute(command: List<String>): ProcessResult
    fun getEnv(name: String): String?
    fun fileExists(path: String): Boolean
    val operatingSystem: OperatingSystem
}

expect fun createDefaultSystemEnvironment(): SystemEnvironment
