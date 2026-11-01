package {{PACKAGE_NAME}}.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface AppDispatchers {
    val main: CoroutineDispatcher get() = Dispatchers.Main
    val default: CoroutineDispatcher get() = Dispatchers.Default
}

object DefaultAppDispatchers : AppDispatchers
