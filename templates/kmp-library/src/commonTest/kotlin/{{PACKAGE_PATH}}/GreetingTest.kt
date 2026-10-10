package {{PACKAGE_NAME}}

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun testGreeting() {
        val greeting = Greeting().greet()
        assertTrue(greeting.startsWith("Hello from"))
    }
}
