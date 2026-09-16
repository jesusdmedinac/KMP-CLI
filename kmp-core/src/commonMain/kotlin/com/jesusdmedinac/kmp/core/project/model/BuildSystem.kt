package com.jesusdmedinac.kmp.core.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BuildSystem {
    @SerialName("gradle")
    GRADLE,

    @SerialName("kotlin-toolchain")
    KOTLIN_TOOLCHAIN,

    @SerialName("unknown")
    UNKNOWN,
}
