package com.jesusdmedinac.kmp.core.project.parser

import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlatformRunnerDetectorTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val detector = PlatformRunnerDetector(fakeEnv)

    @Test
    fun `detects all platform runners when all entry points are present`() {
        fakeEnv.writeFileText(
            "composeApp/src/androidMain/kotlin/com/example/MainActivity.kt",
            """
            package com.example
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent { App() }
                }
            }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "composeApp/src/iosMain/kotlin/com/example/MainViewController.kt",
            """
            package com.example
            import androidx.compose.ui.window.ComposeUIViewController
            fun MainViewController() = ComposeUIViewController { App() }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "composeApp/src/desktopMain/kotlin/com/example/main.kt",
            """
            package com.example
            import androidx.compose.ui.window.application
            import androidx.compose.ui.window.Window
            fun main() = application {
                Window(onCloseRequest = ::exitApplication) { App() }
            }
            """.trimIndent()
        )
        fakeEnv.writeFileText(
            "composeApp/src/wasmJsMain/kotlin/com/example/main.kt",
            """
            package com.example
            import androidx.compose.ui.window.ComposeViewport
            import kotlinx.browser.document
            fun main() {
                ComposeViewport(document.body!!) { App() }
            }
            """.trimIndent()
        )

        val declaredTargets = listOf("android", "iosArm64", "jvm", "wasmJs")
        val runners = detector.detect(".", declaredTargets)

        assertEquals(4, runners.size)

        val android = runners.find { it.target == "android" }
        assertNotNull(android)
        assertTrue(android.present)
        assertEquals("composeApp/src/androidMain/kotlin/com/example/MainActivity.kt", android.entryPoint)
        assertEquals("Android Activity", android.runnerType)

        val ios = runners.find { it.target == "iosArm64" }
        assertNotNull(ios)
        assertTrue(ios.present)
        assertEquals("composeApp/src/iosMain/kotlin/com/example/MainViewController.kt", ios.entryPoint)

        val desktop = runners.find { it.target == "jvm" }
        assertNotNull(desktop)
        assertTrue(desktop.present)
        assertEquals("composeApp/src/desktopMain/kotlin/com/example/main.kt", desktop.entryPoint)

        val wasm = runners.find { it.target == "wasmJs" }
        assertNotNull(wasm)
        assertTrue(wasm.present)
        assertEquals("composeApp/src/wasmJsMain/kotlin/com/example/main.kt", wasm.entryPoint)
    }

    @Test
    fun `reports missing runner when declared target lacks entry point`() {
        fakeEnv.writeFileText(
            "androidApp/src/main/kotlin/com/example/MainActivity.kt",
            """
            package com.example
            class MainActivity : ComponentActivity()
            """.trimIndent()
        )

        val declaredTargets = listOf("android", "wasmJs")
        val runners = detector.detect(".", declaredTargets)

        assertEquals(2, runners.size)

        val android = runners.find { it.target == "android" }
        assertNotNull(android)
        assertTrue(android.present)

        val wasm = runners.find { it.target == "wasmJs" }
        assertNotNull(wasm)
        assertFalse(wasm.present)
        assertNull(wasm.entryPoint)
        assertEquals("Wasm/Web Canvas/Viewport Entry", wasm.runnerType)
    }

    @Test
    fun `ignores test and build files when detecting runners`() {
        fakeEnv.writeFileText(
            "build/generated/MainActivity.kt",
            "class MainActivity : ComponentActivity()"
        )
        fakeEnv.writeFileText(
            "src/commonTest/kotlin/com/example/TestMainActivity.kt",
            "class TestMainActivity : ComponentActivity()"
        )

        val runners = detector.detect(".", listOf("android"))
        val android = runners.find { it.target == "android" }
        assertNotNull(android)
        assertFalse(android.present)
    }
}
