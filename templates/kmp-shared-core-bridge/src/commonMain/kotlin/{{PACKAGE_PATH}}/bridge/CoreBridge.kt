package {{PACKAGE_NAME}}.bridge

import kotlinx.coroutines.flow.StateFlow

interface CoreBridge {
    val isReady: StateFlow<Boolean>
    suspend fun initialize(): Boolean
    fun shutdown()
}
