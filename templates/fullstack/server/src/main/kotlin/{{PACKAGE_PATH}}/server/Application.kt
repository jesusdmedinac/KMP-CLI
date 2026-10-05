package {{PACKAGE_NAME}}.server

import {{PACKAGE_NAME}}.shared.Message
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/api/hello") {
                call.respond(Message(id = "1", text = "Hello from Ktor Server!", timestamp = System.currentTimeMillis()))
            }
        }
    }.start(wait = true)
}
