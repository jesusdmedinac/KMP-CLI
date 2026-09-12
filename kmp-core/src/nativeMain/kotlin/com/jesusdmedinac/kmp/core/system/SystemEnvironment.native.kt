@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package com.jesusdmedinac.kmp.core.system

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.native.Platform
import kotlin.native.OsFamily

class NativeSystemEnvironment : SystemEnvironment {
    @OptIn(ExperimentalForeignApi::class)
    override fun execute(command: List<String>): ProcessResult {
        if (command.isEmpty()) return ProcessResult(-1, "", "Command cannot be empty")
        val cmdString = command.joinToString(" ") { arg ->
            if (arg.contains(" ") || arg.contains("\"") || arg.contains("'")) {
                "\"" + arg.replace("\"", "\\\"") + "\""
            } else {
                arg
            }
        } + " 2>&1"

        val fp = popen(cmdString, "r") ?: return ProcessResult(-1, "", "Failed to execute popen")
        val buffer = ByteArray(4096)
        val sb = StringBuilder()
        try {
            while (true) {
                val line = fgets(buffer.refTo(0), buffer.size, fp) ?: break
                sb.append(line.toKString())
            }
        } finally {
            val status = pclose(fp)
            val exitCode = if (status >= 0) status shr 8 else status
            return ProcessResult(exitCode, sb.toString().trim(), "")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun getEnv(name: String): String? {
        return getenv(name)?.toKString()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun fileExists(path: String): Boolean {
        return access(path, F_OK) == 0
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun nowIso8601(): String = memScoped {
        val now = alloc<time_tVar>()
        now.value = time(null)
        val tm = gmtime(now.ptr)
        val buf = allocArray<ByteVar>(64)
        strftime(buf, 64.convert(), "%Y-%m-%dT%H:%M:%SZ", tm)
        buf.toKString()
    }

    override val operatingSystem: OperatingSystem
        get() = when (Platform.osFamily) {
            OsFamily.MACOSX -> OperatingSystem.MACOS
            OsFamily.LINUX -> OperatingSystem.LINUX
            OsFamily.WINDOWS -> OperatingSystem.WINDOWS
            else -> OperatingSystem.UNKNOWN
        }
}

actual fun createDefaultSystemEnvironment(): SystemEnvironment = NativeSystemEnvironment()
