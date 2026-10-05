package com.jesusdmedinac.kmp.core.scaffold

import com.jesusdmedinac.kmp.core.scaffold.model.ProjectTemplate
import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions
import com.jesusdmedinac.kmp.core.test.FakeSystemEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScaffoldingEngineTest {
    private val fakeEnv = FakeSystemEnvironment()
    private val engine = ScaffoldingEngine(fakeEnv)

    @Test
    fun `scaffold compose-multiplatform generates complete project hierarchy`() {
        val options = ScaffoldingOptions(
            name = "MyComposeApp",
            packageName = "com.example.app",
            template = ProjectTemplate.COMPOSE_MULTIPLATFORM,
            targets = listOf("android", "ios", "desktop", "wasm"),
        )

        val result = engine.scaffold(options)

        assertEquals("SUCCESS", result.status)
        assertEquals("compose-multiplatform", result.template)
        assertEquals("MyComposeApp", result.projectPath)
        assertTrue(result.createdFiles.isNotEmpty())

        val settingsContent = fakeEnv.readFileText("MyComposeApp/settings.gradle.kts")
        assertNotNull(settingsContent)
        assertTrue(settingsContent.contains("rootProject.name = \"MyComposeApp\""))
        assertTrue(settingsContent.contains("include(\":composeApp\")"))

        val catalogContent = fakeEnv.readFileText("MyComposeApp/gradle/libs.versions.toml")
        assertNotNull(catalogContent)
        assertTrue(catalogContent.contains("[versions]"))
        assertTrue(catalogContent.contains("compose-multiplatform"))

        val appBuildContent = fakeEnv.readFileText("MyComposeApp/composeApp/build.gradle.kts")
        assertNotNull(appBuildContent)
        assertTrue(appBuildContent.contains("androidTarget"))
        assertTrue(appBuildContent.contains("iosArm64"))
        assertTrue(appBuildContent.contains("jvm(\"desktop\")") || appBuildContent.contains("jvm()"))

        val appSource = fakeEnv.readFileText("MyComposeApp/composeApp/src/commonMain/kotlin/com/example/app/App.kt")
        assertNotNull(appSource)
        assertTrue(appSource.contains("package com.example.app"))
        assertTrue(appSource.contains("fun App()"))
    }

    @Test
    fun `scaffold toolchain-app generates declarative project yaml and module yaml`() {
        val options = ScaffoldingOptions(
            name = "MyToolchainApp",
            packageName = "com.example.toolchain",
            template = ProjectTemplate.TOOLCHAIN_APP,
            targets = listOf("android", "ios", "wasm"),
        )

        val result = engine.scaffold(options)

        assertEquals("SUCCESS", result.status)
        assertEquals("toolchain-app", result.template)

        val projectYaml = fakeEnv.readFileText("MyToolchainApp/project.yaml")
        assertNotNull(projectYaml)
        assertTrue(projectYaml.contains("modules:"))
        assertTrue(projectYaml.contains("- app"))

        val moduleYaml = fakeEnv.readFileText("MyToolchainApp/app/module.yaml")
        assertNotNull(moduleYaml)
        assertTrue(moduleYaml.contains("platforms:"))
        assertTrue(moduleYaml.contains("- android"))
        assertTrue(moduleYaml.contains("- ios"))
        assertTrue(moduleYaml.contains("- wasm"))

        val mainSource = fakeEnv.readFileText("MyToolchainApp/app/src/commonMain/kotlin/com/example/toolchain/Main.kt")
        assertNotNull(mainSource)
        assertTrue(mainSource.contains("package com.example.toolchain"))
        assertTrue(mainSource.contains("fun main()"))
    }

    @Test
    fun `scaffold kmp-library generates publishing setup and expect actual skeletons`() {
        val options = ScaffoldingOptions(
            name = "MyKmpLib",
            packageName = "com.example.lib",
            template = ProjectTemplate.KMP_LIBRARY,
            targets = listOf("android", "ios", "desktop"),
        )

        val result = engine.scaffold(options)

        assertEquals("SUCCESS", result.status)
        assertEquals("kmp-library", result.template)

        val buildGradle = fakeEnv.readFileText("MyKmpLib/build.gradle.kts")
        assertNotNull(buildGradle)
        assertTrue(buildGradle.contains("`maven-publish`"))
        assertTrue(buildGradle.contains("publishing {"))

        val platformCommon = fakeEnv.readFileText("MyKmpLib/src/commonMain/kotlin/com/example/lib/Platform.kt")
        assertNotNull(platformCommon)
        assertTrue(platformCommon.contains("expect fun getPlatformName(): String"))

        val platformJvm = fakeEnv.readFileText("MyKmpLib/src/jvmMain/kotlin/com/example/lib/Platform.jvm.kt")
        assertNotNull(platformJvm)
        assertTrue(platformJvm.contains("actual fun getPlatformName(): String"))
    }

    @Test
    fun `scaffold fullstack generates shared server and client modules`() {
        val options = ScaffoldingOptions(
            name = "MyFullstackApp",
            packageName = "com.example.fullstack",
            template = ProjectTemplate.FULLSTACK,
        )

        val result = engine.scaffold(options)

        assertEquals("SUCCESS", result.status)
        assertEquals("fullstack", result.template)

        val settingsGradle = fakeEnv.readFileText("MyFullstackApp/settings.gradle.kts")
        assertNotNull(settingsGradle)
        assertTrue(settingsGradle.contains("include(\":shared\")"))
        assertTrue(settingsGradle.contains("include(\":server\")"))
        assertTrue(settingsGradle.contains("include(\":composeApp\")"))

        val serverApp = fakeEnv.readFileText("MyFullstackApp/server/src/main/kotlin/com/example/fullstack/server/Application.kt")
        assertNotNull(serverApp)
        assertTrue(serverApp.contains("embeddedServer"))
        assertTrue(serverApp.contains("routing {"))

        val sharedMessage = fakeEnv.readFileText("MyFullstackApp/shared/src/commonMain/kotlin/com/example/fullstack/shared/Message.kt")
        assertNotNull(sharedMessage)
        assertTrue(sharedMessage.contains("data class Message"))
    }

    @Test
    fun `scaffold with customized targets only configures requested platforms`() {
        val options = ScaffoldingOptions(
            name = "CustomTargetsApp",
            packageName = "com.example.custom",
            template = ProjectTemplate.COMPOSE_MULTIPLATFORM,
            targets = listOf("android", "ios"),
        )

        val result = engine.scaffold(options)
        assertEquals("SUCCESS", result.status)

        val appBuild = fakeEnv.readFileText("CustomTargetsApp/composeApp/build.gradle.kts")
        assertNotNull(appBuild)
        assertTrue(appBuild.contains("androidTarget"))
        assertTrue(appBuild.contains("iosArm64"))
        assertFalse(appBuild.contains("wasmJs"))
        assertFalse(appBuild.contains("jvm(\"desktop\")"))
    }

    @Test
    fun `scaffold sdui-starter generates json-to-compose dependency and sdui starter code`() {
        val options = ScaffoldingOptions(
            name = "MySduiApp",
            packageName = "com.example.sdui",
            template = ProjectTemplate.SDUI_STARTER,
        )

        val result = engine.scaffold(options)
        assertEquals("SUCCESS", result.status)

        val catalog = fakeEnv.readFileText("MySduiApp/gradle/libs.versions.toml")
        assertNotNull(catalog)
        assertTrue(catalog.contains("json-to-compose"))

        val sduiApp = fakeEnv.readFileText("MySduiApp/composeApp/src/commonMain/kotlin/com/example/sdui/SduiApp.kt")
        assertNotNull(sduiApp)
        assertTrue(sduiApp.contains("fun SduiApp()"))
    }

    @Test
    fun `scaffold fails gracefully when project name is empty`() {
        val options = ScaffoldingOptions(
            name = "",
            packageName = "com.example.empty",
        )

        val result = engine.scaffold(options)
        assertEquals("FAILED", result.status)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `scaffold loads templates from physical disk directory when present`() {
        fakeEnv.writeFileText(
            "templates/compose-multiplatform/settings.gradle.kts",
            "// Custom physical template\nrootProject.name = \"{{PROJECT_NAME}}\""
        )
        fakeEnv.writeFileText(
            "templates/compose-multiplatform/build.gradle.kts",
            "// Physical build.gradle.kts"
        )
        fakeEnv.writeFileText(
            "templates/compose-multiplatform/composeApp/build.gradle.kts",
            "// Physical composeApp build"
        )
        fakeEnv.writeFileText(
            "templates/compose-multiplatform/gradle/libs.versions.toml",
            "[versions]\ncustom = \"1.0.0\""
        )

        val options = ScaffoldingOptions(
            name = "PhysicalApp",
            packageName = "com.example.physical",
            template = ProjectTemplate.COMPOSE_MULTIPLATFORM,
        )

        val result = engine.scaffold(options)
        assertEquals("SUCCESS", result.status)

        val settings = fakeEnv.readFileText("PhysicalApp/settings.gradle.kts")
        assertNotNull(settings)
        assertTrue(settings.contains("// Custom physical template"))
        assertTrue(settings.contains("rootProject.name = \"PhysicalApp\""))
    }
}
