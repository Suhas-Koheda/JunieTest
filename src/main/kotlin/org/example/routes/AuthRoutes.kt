package org.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.config.getUserId
import org.example.models.*
import org.example.services.AuthService
import org.example.services.UserService

fun Route.authRoutes() {
    val authService = AuthService(
        userRepository = org.example.repositories.UserRepository(),
        tokenRepository = org.example.repositories.TokenRepository(),
        config = application.environment.config
    )
    
    route("/api/auth") {
        // Register endpoint
        post("/register") {
            val request = call.receive<RegisterRequest>()
            
            // Validate request
            if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Username, email, and password cannot be empty")
                return@post
            }
            
            if (request.password.length < 8) {
                call.respond(HttpStatusCode.BadRequest, "Password must be at least 8 characters long")
                return@post
            }
            
            if (!request.email.contains("@")) {
                call.respond(HttpStatusCode.BadRequest, "Invalid email format")
                return@post
            }
            
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }
        
        // Login endpoint
        post("/login") {
            val request = call.receive<LoginRequest>()
            
            // Validate request
            if (request.email.isBlank() || request.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Email and password cannot be empty")
                return@post
            }
            
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }
        
        // Get current user info (protected)
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getUserId() ?: throw IllegalArgumentException("Invalid token")
                
                val userInfo = authService.getUserInfo(userId)
                call.respond(HttpStatusCode.OK, userInfo)
            }
            
            // Logout endpoint
            post("/logout") {
                val authHeader = call.request.header(HttpHeaders.Authorization)
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    val token = authHeader.substring(7)
                    val success = authService.logout(token)
                    
                    if (success) {
                        call.respond(HttpStatusCode.OK, MessageResponse(message = "Logged out successfully"))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, MessageResponse(message = "Failed to logout"))
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, MessageResponse(message = "Invalid token"))
                }
            }
        }
    }
}