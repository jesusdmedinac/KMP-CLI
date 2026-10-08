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
        assertEquals("shared-ui", result.template)
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
        assertEquals("toolchain-shared-ui", result.template)

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
        assertEquals("multiplatform-library", result.template)

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
        assertTrue(settingsGradle.contains("include(\":server\")"))
        assertTrue(settingsGradle.contains("include(\":app:androidApp\")"))
        assertTrue(settingsGradle.contains("include(\":app:desktopApp\")"))

        val serverApp = fakeEnv.readFileText("MyFullstackApp/server/src/main/kotlin/com/example/fullstack/server/Application.kt")
        assertNotNull(serverApp)
        assertTrue(serverApp.contains("embeddedServer"))
        assertTrue(serverApp.contains("routing {"))

        assertTrue(fakeEnv.fileExists("MyFullstackApp/app/desktopApp/build.gradle.kts"))
        val desktopBuild = fakeEnv.readFileText("MyFullstackApp/app/desktopApp/build.gradle.kts") ?: ""
        assertTrue(desktopBuild.contains("compose.desktop"))
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
    fun `scaffold json-to-compose-sample-app generates json-to-compose dependency and run configurations`() {
        val options = ScaffoldingOptions(
            name = "MySampleApp",
            packageName = "com.example.sample",
            template = ProjectTemplate.JSON_TO_COMPOSE_SAMPLE,
        )

        val result = engine.scaffold(options)
        assertEquals("SUCCESS", result.status)

        val catalog = fakeEnv.readFileText("MySampleApp/gradle/libs.versions.toml")
        assertNotNull(catalog)
        assertTrue(catalog.contains("json-to-compose"))

        val desktopRun = fakeEnv.readFileText("MySampleApp/.run/desktopApp.run.xml")
        assertNotNull(desktopRun)
        assertTrue(desktopRun.contains(":composeApp:desktopRun"))

        val wasmRun = fakeEnv.readFileText("MySampleApp/.run/wasmJs.run.xml")
        assertNotNull(wasmRun)
        assertTrue(wasmRun.contains(":composeApp:wasmJsBrowserDevelopmentRun"))
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

    @Test
    fun `scaffold sets executable permissions on gradlew and kotlin scripts`() {
        fakeEnv.writeFileText(
            "templates/shared-ui/gradlew",
            "#!/usr/bin/env sh\nexec gradle"
        )
        fakeEnv.writeFileText(
            "templates/shared-ui/settings.gradle.kts",
            "rootProject.name = \"{{PROJECT_NAME}}\""
        )

        val options = ScaffoldingOptions(
            name = "ExecApp",
            template = ProjectTemplate.SHARED_UI,
        )

        val result = engine.scaffold(options)
        assertEquals("SUCCESS", result.status)
        assertTrue(fakeEnv.executableFiles.contains("ExecApp/gradlew"))
    }

    @Test
    fun `scaffold native-ui template generates native ios target`() {
        val options = ScaffoldingOptions(
            name = "MyNativeApp",
            template = ProjectTemplate.NATIVE_UI,
        )

        val result = engine.scaffold(options)
        assertEquals("SUCCESS", result.status)
        assertEquals("native-ui", result.template)
    }

    @Test
    fun `scaffold toolchain-native-ui template generates toolchain native target`() {
        val options = ScaffoldingOptions(
            name = "MyToolchainNative",
            template = ProjectTemplate.TOOLCHAIN_NATIVE_UI,
        )

        val result = engine.scaffold(options)
        assertEquals("SUCCESS", result.status)
        assertEquals("toolchain-native-ui", result.template)
    }
}
