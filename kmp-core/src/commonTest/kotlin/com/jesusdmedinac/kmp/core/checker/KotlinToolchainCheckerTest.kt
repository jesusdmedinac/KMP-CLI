package com.jesusdmedinac.kmp.core.checker

import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.system.ProcessResult
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KotlinToolchainCheckerTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val checker = KotlinToolchainChecker(fakeEnv)

    @Test
    fun `returns WARNING with remediation when kotlin toolchain is not found`() = runTest {
        val result = checker.check()

        assertEquals("kotlinToolchain", result.id)
        assertEquals("Kotlin Toolchain (CLI / 0.12+)", result.title)
        assertEquals(CheckStatus.WARNING, result.status)
        assertNotNull(result.remediation)
        assertEquals("brew install kotlin", result.remediation?.command)
    }

    @Test
    fun `returns SUCCESS when local kotlin wrapper exists and returns version`() = runTest {
        fakeEnv.addFile("./kotlin")
        fakeEnv.whenCommand(
            listOf("./kotlin", "version"),
            ProcessResult(exitCode = 0, stdout = "Kotlin Toolchain 0.12.0", stderr = "")
        )

        val result = checker.check()

        assertEquals(CheckStatus.SUCCESS, result.status)
        assertEquals(true, result.message.contains("0.12.0"))
        assertNull(result.remediation)
    }

    @Test
    fun `returns SUCCESS when global kotlin command is available in PATH`() = runTest {
        fakeEnv.whenCommand(
            listOf("kotlin", "-version"),
            ProcessResult(
                exitCode = 0,
                stdout = "Kotlin version 2.1.10-release-502 (JRE 21.0.2)",
                stderr = ""
            )
        )

        val result = checker.check()

        assertEquals(CheckStatus.SUCCESS, result.status)
        assertEquals(true, result.message.contains("2.1.10"))
        assertNull(result.remediation)
    }
}
