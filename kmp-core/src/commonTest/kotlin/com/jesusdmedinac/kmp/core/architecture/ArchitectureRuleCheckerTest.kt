package com.jesusdmedinac.kmp.core.architecture

import com.jesusdmedinac.kmp.core.architecture.model.ViolationSeverity
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArchitectureRuleCheckerTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val checker = ArchitectureRuleChecker(fakeEnv)

    @Test
    fun `detects direct data layer and DAO imports in Compose UI presentation components`() {
        val uiCode = """
            package com.example.feature.vehicle.presentation
            
            import androidx.compose.runtime.Composable
            import com.example.feature.vehicle.data.VehicleDao
            import com.example.feature.vehicle.data.VehicleRepositoryImpl
            
            @Composable
            fun VehicleScreen() {
                // UI Code
            }
        """.trimIndent()

        val violations = checker.checkFile("src/commonMain/kotlin/presentation/VehicleScreen.kt", uiCode)

        assertEquals(2, violations.size)
        assertTrue(violations.all { it.ruleId == "ARCH001_UI_DATA_LEAK" })
        assertTrue(violations.all { it.severity == ViolationSeverity.ERROR })
        assertEquals(4, violations[0].lineNumber)
        assertTrue(violations[0].message.contains("VehicleDao"))
        assertTrue(violations[0].remediation.contains("Inject the domain interface"))
        assertEquals(5, violations[1].lineNumber)
        assertTrue(violations[1].message.contains("VehicleRepositoryImpl"))
    }

    @Test
    fun `allows domain and use case imports in presentation components`() {
        val uiCode = """
            package com.example.feature.vehicle.presentation
            
            import androidx.compose.runtime.Composable
            import com.example.feature.vehicle.domain.GetVehicleUseCase
            
            @Composable
            fun VehicleScreen() {
            }
        """.trimIndent()

        val violations = checker.checkFile("src/commonMain/kotlin/presentation/VehicleScreen.kt", uiCode)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `detects ViewModel exposing MutableStateFlow directly to public API`() {
        val viewModelCode = """
            package com.example.feature.vehicle.presentation
            
            import kotlinx.coroutines.flow.MutableStateFlow
            
            class VehicleViewModel {
                val state = MutableStateFlow(VehicleState())
                val anotherState: MutableStateFlow<String> = MutableStateFlow("")
            }
        """.trimIndent()

        val violations = checker.checkFile("src/commonMain/kotlin/presentation/VehicleViewModel.kt", viewModelCode)

        assertEquals(2, violations.size)
        assertTrue(violations.all { it.ruleId == "ARCH002_MUTABLE_STATE_EXPOSURE" })
        assertTrue(violations.all { it.severity == ViolationSeverity.ERROR })
        assertEquals(6, violations[0].lineNumber)
        assertEquals(7, violations[1].lineNumber)
        assertTrue(violations[0].remediation.contains("asStateFlow()"))
    }

    @Test
    fun `allows ViewModel with private MutableStateFlow and public StateFlow`() {
        val viewModelCode = """
            package com.example.feature.vehicle.presentation
            
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.StateFlow
            import kotlinx.coroutines.flow.asStateFlow
            
            class VehicleViewModel {
                private val _state = MutableStateFlow(VehicleState())
                val state: StateFlow<VehicleState> = _state.asStateFlow()
            }
        """.trimIndent()

        val violations = checker.checkFile("src/commonMain/kotlin/presentation/VehicleViewModel.kt", viewModelCode)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `audits module yaml dependencies against code imports`() {
        val moduleYaml = """
            product: app
            dependencies:
              - ${'$'}compose.foundation
              - androidx.room:room-runtime:2.7.0
              - com.unused.library:artifact:1.0.0
        """.trimIndent()

        val sourceFiles = mapOf(
            "src/commonMain/kotlin/App.kt" to """
                import androidx.room.RoomDatabase
                import com.unregistered.external.Client
            """.trimIndent()
        )

        val violations = checker.auditModuleYaml("module.yaml", moduleYaml, sourceFiles)

        val unused = violations.filter { it.ruleId == "ARCH003_UNUSED_DEPENDENCY" }
        assertEquals(1, unused.size)
        assertTrue(unused.first().message.contains("com.unused.library:artifact:1.0.0"))
        assertEquals(ViolationSeverity.WARNING, unused.first().severity)

        val undeclared = violations.filter { it.ruleId == "ARCH003_UNDECLARED_DEPENDENCY" }
        assertEquals(1, undeclared.size)
        assertTrue(undeclared.first().message.contains("com.unregistered.external.Client"))
        assertEquals(ViolationSeverity.ERROR, undeclared.first().severity)
    }

    @Test
    fun `ignores commented out code and imports`() {
        val uiCode = """
            package com.example.feature.vehicle.presentation
            
            import androidx.compose.runtime.Composable
            // import com.example.feature.vehicle.data.VehicleDao
            /*
             * import com.example.feature.vehicle.data.VehicleRepositoryImpl
             */
            
            class VehicleViewModel {
                // val state = MutableStateFlow("")
                /* val state2 = MutableStateFlow("") */
            }
            
            @Composable
            fun VehicleScreen() {
            }
        """.trimIndent()

        val violations = checker.checkFile("src/commonMain/kotlin/presentation/VehicleScreen.kt", uiCode)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `allows domain metadata packages containing data segment`() {
        val uiCode = """
            package com.example.feature.vehicle.presentation
            
            import androidx.compose.runtime.Composable
            import com.example.feature.vehicle.domain.data.VehicleMetadata
            
            @Composable
            fun VehicleScreen() {
            }
        """.trimIndent()

        val violations = checker.checkFile("src/commonMain/kotlin/presentation/VehicleScreen.kt", uiCode)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `dynamically resolves internal project packages for non-author namespace`() {
        val moduleYaml = """
            product: app
            dependencies:
              - androidx.room:room-runtime:2.7.0
        """.trimIndent()

        val sourceFiles = mapOf(
            "src/commonMain/kotlin/App.kt" to """
                package com.enterprise.acme.feature.home
                import com.enterprise.acme.core.network.ApiClient
                import androidx.room.RoomDatabase
            """.trimIndent(),
            "src/commonMain/kotlin/ApiClient.kt" to """
                package com.enterprise.acme.core.network
                class ApiClient
            """.trimIndent()
        )

        val violations = checker.auditModuleYaml("module.yaml", moduleYaml, sourceFiles)
        val undeclared = violations.filter { it.ruleId == "ARCH003_UNDECLARED_DEPENDENCY" }
        assertTrue(undeclared.isEmpty(), "Internal imports across enterprise namespaces must not be flagged as undeclared dependencies")
    }

    @Test
    fun `checkProject aggregates all files and checks pass when clean`() {
        fakeEnv.writeFileText(
            "src/commonMain/kotlin/presentation/VehicleViewModel.kt",
            """
                package com.example.presentation
                import kotlinx.coroutines.flow.MutableStateFlow
                import kotlinx.coroutines.flow.StateFlow
                import kotlinx.coroutines.flow.asStateFlow
                class VehicleViewModel {
                    private val _state = MutableStateFlow("")
                    val state: StateFlow<String> = _state.asStateFlow()
                }
            """.trimIndent()
        )

        val report = checker.checkProject("src")
        assertTrue(report.passed)
        assertTrue(report.violations.isEmpty())
        assertTrue(report.scannedFilesCount >= 1)
    }

    @Test
    fun `checkProject detects declared targets missing platform runners and emits ARCH004 with boilerplate remediation`() {
        fakeEnv.writeFileText(
            "settings.gradle.kts",
            """
            rootProject.name = "MyAwesomeApp"
            include(":composeApp")
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "build.gradle.kts",
            """
            plugins {
                id("org.jetbrains.compose")
            }
            kotlin {
                androidTarget()
                jvm("desktop")
                wasmJs()
            }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "composeApp/src/androidMain/kotlin/MainActivity.kt",
            """
            package com.example
            class MainActivity : ComponentActivity()
            """.trimIndent()
        )

        val report = checker.checkProject(".")
        assertTrue(report.passed, "Warning violations should not fail the overall check")

        val runnerViolations = report.violations.filter { it.ruleId == "ARCH004_MISSING_PLATFORM_RUNNER" }
        assertEquals(2, runnerViolations.size, "Should flag missing desktop and wasmJs runners")

        val desktopViolation = runnerViolations.find { it.message.contains("jvm") || it.message.contains("desktop") }
        kotlin.test.assertNotNull(desktopViolation)
        assertEquals(ViolationSeverity.WARNING, desktopViolation.severity)
        assertTrue(desktopViolation.remediation.contains("fun main() = application"))

        val wasmViolation = runnerViolations.find { it.message.contains("wasmJs") }
        kotlin.test.assertNotNull(wasmViolation)
        assertEquals(ViolationSeverity.WARNING, wasmViolation.severity)
        assertTrue(wasmViolation.remediation.contains("ComposeViewport"))
    }
}
