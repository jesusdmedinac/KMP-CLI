package com.jesusdmedinac.kmp.core.checker

import com.jesusdmedinac.kmp.core.model.CheckStatus
import com.jesusdmedinac.kmp.core.system.OperatingSystem
import com.jesusdmedinac.kmp.core.system.ProcessResult
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KDoctorCheckerTest {
    private val fakeEnv = FakeSystemEnvironment(operatingSystem = OperatingSystem.MACOS)
    private val checker = KDoctorChecker(fakeEnv)

    @Test
    fun `returns SUCCESS with note when operating system is not macOS`() = runTest {
        fakeEnv.operatingSystem = OperatingSystem.LINUX

        val result = checker.check()

        assertEquals("kdoctor", result.id)
        assertEquals(CheckStatus.SUCCESS, result.status)
        assertTrue(result.message.contains("macOS"))
        assertNull(result.remediation)
    }

    @Test
    fun `returns WARNING with brew remediation when kdoctor is not found on macOS`() = runTest {
        fakeEnv.operatingSystem = OperatingSystem.MACOS
        fakeEnv.whenCommand(
            listOf("kdoctor", "--version"),
            ProcessResult(exitCode = 127, stdout = "", stderr = "kdoctor: command not found")
        )

        val result = checker.check()

        assertEquals("kdoctor", result.id)
        assertEquals(CheckStatus.WARNING, result.status)
        assertNotNull(result.remediation)
        assertEquals("brew install kdoctor", result.remediation?.command)
        assertTrue(result.message.contains("not installed"))
    }

    @Test
    fun `returns SUCCESS when kdoctor succeeds and all checks pass`() = runTest {
        fakeEnv.operatingSystem = OperatingSystem.MACOS
        fakeEnv.whenCommand(
            listOf("kdoctor", "--version"),
            ProcessResult(exitCode = 0, stdout = "KDoctor 1.1.0", stderr = "")
        )
        val kdoctorOutput = """
            [✓] Operation System
            [✓] Java
            [✓] Android Studio
            [✓] Xcode
            [✓] CocoaPods
            Conclusion:
              ✓ Your operation system is ready for Kotlin Multiplatform Mobile Development!
        """.trimIndent()
        fakeEnv.whenCommand(
            listOf("kdoctor"),
            ProcessResult(exitCode = 0, stdout = kdoctorOutput, stderr = "")
        )

        val result = checker.check()

        assertEquals(CheckStatus.SUCCESS, result.status)
        assertNull(result.remediation)
        assertTrue(result.message.contains("ready"))
    }

    @Test
    fun `returns WARNING when kdoctor reports warnings`() = runTest {
        fakeEnv.operatingSystem = OperatingSystem.MACOS
        fakeEnv.whenCommand(
            listOf("kdoctor", "--version"),
            ProcessResult(exitCode = 0, stdout = "KDoctor 1.1.0", stderr = "")
        )
        val kdoctorOutput = """
            [✓] Operation System
            [✓] Java
            [!] Android Studio
              ! Kotlin Multiplatform Mobile Plugin: not installed
            [✓] Xcode
            [✓] CocoaPods
            Conclusion:
              ✓ Your operation system is ready for Kotlin Multiplatform Mobile Development!
        """.trimIndent()
        fakeEnv.whenCommand(
            listOf("kdoctor"),
            ProcessResult(exitCode = 0, stdout = kdoctorOutput, stderr = "")
        )

        val result = checker.check()

        assertEquals(CheckStatus.WARNING, result.status)
        assertTrue(result.message.contains("warning"))
        assertNotNull(result.remediation)
    }

    @Test
    fun `returns FAILURE when kdoctor reports failure`() = runTest {
        fakeEnv.operatingSystem = OperatingSystem.MACOS
        fakeEnv.whenCommand(
            listOf("kdoctor", "--version"),
            ProcessResult(exitCode = 0, stdout = "KDoctor 1.1.0", stderr = "")
        )
        val kdoctorOutput = """
            [✓] Operation System
            [✓] Java
            [x] Xcode
              x Xcode is not installed
            Conclusion:
              x Failures detected!
        """.trimIndent()
        fakeEnv.whenCommand(
            listOf("kdoctor"),
            ProcessResult(exitCode = 1, stdout = kdoctorOutput, stderr = "")
        )

        val result = checker.check()

        assertEquals(CheckStatus.FAILURE, result.status)
        assertTrue(result.message.contains("failure", ignoreCase = true))
        assertNotNull(result.remediation)
    }
}
