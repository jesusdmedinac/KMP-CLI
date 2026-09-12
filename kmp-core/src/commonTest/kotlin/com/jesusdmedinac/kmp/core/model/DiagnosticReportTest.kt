package com.jesusdmedinac.kmp.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiagnosticReportTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `should serialize and deserialize DiagnosticReport correctly`() {
        val check = CheckResult(
            id = "jdk",
            title = "Java Development Kit (JDK)",
            status = CheckStatus.SUCCESS,
            message = "JDK 21.0.10 detected",
            details = listOf("JAVA_HOME: /Library/Java/JavaVirtualMachines/jdk-21.jdk"),
            remediation = null,
        )

        val report = DiagnosticReport(
            version = "0.1.0-SNAPSHOT",
            timestamp = "2026-09-10T20:00:00Z",
            overallStatus = CheckStatus.SUCCESS,
            checks = listOf(check),
        )

        val serialized = json.encodeToString(DiagnosticReport.serializer(), report)
        val deserialized = json.decodeFromString(DiagnosticReport.serializer(), serialized)

        assertEquals(report, deserialized)
        assertTrue(deserialized.isHealthy)
        assertEquals("jdk", deserialized.checks.first().id)
        assertEquals(CheckStatus.SUCCESS, deserialized.checks.first().status)
    }

    @Test
    fun `should report unhealthy when any check fails`() {
        val check = CheckResult(
            id = "jdk",
            title = "Java Development Kit (JDK)",
            status = CheckStatus.FAILURE,
            message = "No Java runtime found",
            remediation = Remediation(
                description = "Install OpenJDK 17 or 21 via Homebrew",
                command = "brew install openjdk@21",
                docUrl = "https://formulae.brew.sh/formula/openjdk@21",
            ),
        )

        val report = DiagnosticReport(
            version = "0.1.0-SNAPSHOT",
            timestamp = "2026-09-10T20:00:00Z",
            overallStatus = CheckStatus.FAILURE,
            checks = listOf(check),
        )

        val serialized = json.encodeToString(DiagnosticReport.serializer(), report)
        val deserialized = json.decodeFromString(DiagnosticReport.serializer(), serialized)

        assertEquals(CheckStatus.FAILURE, deserialized.overallStatus)
        assertEquals(false, deserialized.isHealthy)
        assertEquals("brew install openjdk@21", deserialized.checks.first().remediation?.command)
    }
}
