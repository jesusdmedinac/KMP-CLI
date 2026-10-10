package {{PACKAGE_NAME}}

actual fun getPlatformName(): String = "JVM (${'$'}{System.getProperty("java.version")})"
