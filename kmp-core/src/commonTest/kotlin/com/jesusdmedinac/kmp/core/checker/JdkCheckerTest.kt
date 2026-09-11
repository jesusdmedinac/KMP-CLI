package com.jesusdmedinac.kmp.core.checker

import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.system.ProcessResult
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JdkCheckerTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val checker = JdkChecker(fakeEnv)

    @Test
    fun `returns FAILURE when java command is missing or fails`() = runTest {
        fakeEnv.whenCommand(
            listOf("java", "-version"),
            ProcessResult(exitCode = 127, stdout = "", stderr = "java: command not found")
        )

        val result = checker.check()

        assertEquals("jdk", result.id)
        assertEquals("Java Development Kit (JDK)", result.title)
        assertEquals(CheckStatus.FAILURE, result.status)
        assertNotNull(result.remediation)
        assertEquals("brew install openjdk@17", result.remediation?.command)
    }

    @Test
    fun `returns FAILURE when JDK version is below 17`() = runTest {
        fakeEnv.whenCommand(
            listOf("java", "-version"),
            ProcessResult(
                exitCode = 0,
                stdout = "",
                stderr = """
                    openjdk version "11.0.18" 2023-01-17
                    OpenJDK Runtime Environment Homebrew (build 11.0.18+0)
                    OpenJDK 64-Bit Server VM Homebrew (build 11.0.18+0, mixed mode)
                """.trimIndent()
            )
        )

        val result = checker.check()

        assertEquals(CheckStatus.FAILURE, result.status)
        assertEquals(true, result.message.contains("require JDK 17 or higher"))
        assertNotNull(result.remediation)
    }

    @Test
    fun `returns FAILURE when legacy Java 8 is installed`() = runTest {
        fakeEnv.whenCommand(
            listOf("java", "-version"),
            ProcessResult(
                exitCode = 0,
                stdout = "",
                stderr = """java version "1.8.0_312""""
            )
        )

        val result = checker.check()

        assertEquals(CheckStatus.FAILURE, result.status)
        assertEquals(true, result.message.contains("require JDK 17 or higher"))
    }

    @Test
    fun `returns WARNING when JDK 17+ is detected but JAVA_HOME is not set`() = runTest {
        fakeEnv.whenCommand(
            listOf("java", "-version"),
            ProcessResult(
                exitCode = 0,
                stdout = "",
                stderr = """openjdk version "17.0.9" 2023-10-17"""
            )
        )

        val result = checker.check()

        assertEquals(CheckStatus.WARNING, result.status)
        assertEquals(true, result.message.contains("JAVA_HOME"))
        assertNotNull(result.remediation)
    }

    @Test
    fun `returns WARNING when JAVA_HOME points to non-existent directory`() = runTest {
        fakeEnv.whenCommand(
            listOf("java", "-version"),
            ProcessResult(
                exitCode = 0,
                stdout = "",
                stderr = """openjdk version "21.0.2" 2024-01-16"""
            )
        )
        fakeEnv.setEnv("JAVA_HOME", "/invalid/jdk/path")

        val result = checker.check()

        assertEquals(CheckStatus.WARNING, result.status)
        assertEquals(true, result.message.contains("does not exist"))
        assertNotNull(result.remediation)
    }

    @Test
    fun `returns SUCCESS when JDK 17+ is installed and valid JAVA_HOME is configured`() = runTest {
        val javaHome = "/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home"
        fakeEnv.whenCommand(
            listOf("java", "-version"),
            ProcessResult(
                exitCode = 0,
                stdout = "",
                stderr = """openjdk version "21.0.2" 2024-01-16"""
            )
        )
        fakeEnv.setEnv("JAVA_HOME", javaHome)
        fakeEnv.addFile(javaHome)

        val result = checker.check()

        assertEquals(CheckStatus.SUCCESS, result.status)
        assertEquals(true, result.message.contains("21.0.2"))
        assertNull(result.remediation)
    }
}
