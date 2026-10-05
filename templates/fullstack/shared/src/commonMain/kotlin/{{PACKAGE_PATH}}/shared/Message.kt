package {{PACKAGE_NAME}}.shared

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val text: String,
    val timestamp: Long,
)
