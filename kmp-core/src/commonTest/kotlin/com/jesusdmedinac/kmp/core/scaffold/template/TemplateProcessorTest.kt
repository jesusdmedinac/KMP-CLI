package com.jesusdmedinac.kmp.core.scaffold.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TemplateProcessorTest {

    @Test
    fun `process replaces variables in content and file paths`() {
        val rawFiles = mapOf(
            "composeApp/src/commonMain/kotlin/{{PACKAGE_PATH}}/App.kt" to """
                package {{PACKAGE_NAME}}
                
                class {{PROJECT_NAME}}App
            """.trimIndent()
        )

        val context = TemplateContext(
            variables = mapOf(
                "PROJECT_NAME" to "SuperApp",
                "PACKAGE_NAME" to "com.example.superapp",
                "PACKAGE_PATH" to "com/example/superapp",
            )
        )

        val processed = TemplateProcessor.process(rawFiles, context)

        val expectedPath = "composeApp/src/commonMain/kotlin/com/example/superapp/App.kt"
        assertTrue(processed.containsKey(expectedPath))

        val content = processed[expectedPath]!!
        assertTrue(content.contains("package com.example.superapp"))
        assertTrue(content.contains("class SuperAppApp"))
        assertFalse(content.contains("{{PACKAGE_NAME}}"))
        assertFalse(content.contains("{{PROJECT_NAME}}"))
    }

    @Test
    fun `process keeps conditional blocks when target is active and strips markers`() {
        val rawContent = """
            kotlin {
                // {{#TARGET:android}}
                androidTarget()
                // {{/TARGET:android}}
                // {{#TARGET:wasm}}
                wasmJs()
                // {{/TARGET:wasm}}
            }
        """.trimIndent()

        val rawFiles = mapOf("build.gradle.kts" to rawContent)
        val context = TemplateContext(
            variables = emptyMap(),
            activeTargets = setOf("android"),
        )

        val processed = TemplateProcessor.process(rawFiles, context)
        val content = processed["build.gradle.kts"]!!

        assertTrue(content.contains("androidTarget()"))
        assertFalse(content.contains("wasmJs()"))
        assertFalse(content.contains("{{#TARGET:"))
        assertFalse(content.contains("{{/TARGET:"))
    }

    @Test
    fun `process leaves original rawFiles map unmodified ensuring immutability`() {
        val originalKey = "src/{{PACKAGE_PATH}}/Main.kt"
        val originalValue = "package {{PACKAGE_NAME}}"
        val rawFiles = mapOf(originalKey to originalValue)

        val context = TemplateContext(
            variables = mapOf(
                "PACKAGE_PATH" to "com/app",
                "PACKAGE_NAME" to "com.app",
            )
        )

        val processed = TemplateProcessor.process(rawFiles, context)

        // Verify original map remains intact
        assertEquals(1, rawFiles.size)
        assertEquals(originalValue, rawFiles[originalKey])
        assertTrue(rawFiles.containsKey(originalKey))

        // Verify output is a transformed copy
        assertFalse(processed.containsKey(originalKey))
        assertTrue(processed.containsKey("src/com/app/Main.kt"))
        assertEquals("package com.app", processed["src/com/app/Main.kt"])
    }
}
