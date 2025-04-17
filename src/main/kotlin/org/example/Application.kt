package org.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.example.config.DatabaseFactory
import org.example.config.configureJWT
import org.example.plugins.configureRouting
import org.example.routes.authRoutes
import org.example.routes.userRoutes

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // Initialize database
    DatabaseFactory.init(environment.config)

    // Configure serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // Configure CORS
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    // Configure error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: $cause",
                status = HttpStatusCode.InternalServerError
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respondText(
                text = "400: $cause",
                status = HttpStatusCode.BadRequest
            )
        }

        exception<UnauthorizedAccessException> { call, _ ->
            call.respondText(
                text = "401: Unauthorized",
                status = HttpStatusCode.Unauthorized
            )
        }

        exception<ForbiddenAccessException> { call, _ ->
            call.respondText(
                text = "403: Forbidden",
                status = HttpStatusCode.Forbidden
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respondText(
                text = "404: ${cause.message}",
                status = HttpStatusCode.NotFound
            )
        }
    }

    // Configure JWT authentication
    configureJWT(environment.config)

    // Configure routing
    configureRouting()

    // Register routes
    routing {
        authRoutes()
        userRoutes()
    }
}

// Custom exceptions
class UnauthorizedAccessException : RuntimeException()
class ForbiddenAccessException : RuntimeException()
class NotFoundException(message: String) : RuntimeException(message)
