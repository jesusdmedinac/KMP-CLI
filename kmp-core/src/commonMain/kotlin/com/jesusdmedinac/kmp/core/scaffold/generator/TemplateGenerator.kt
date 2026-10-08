package com.jesusdmedinac.kmp.core.scaffold.generator

import com.jesusdmedinac.kmp.core.scaffold.model.ScaffoldingOptions

interface TemplateGenerator {
    fun generate(options: ScaffoldingOptions): Map<String, String>
}
