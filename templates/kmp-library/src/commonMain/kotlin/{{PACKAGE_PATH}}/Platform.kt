package {{PACKAGE_NAME}}

expect fun getPlatformName(): String

class Greeting {
    fun greet(): String = "Hello from ${'$'}{getPlatformName()}!"
}
