package org.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.config.getUserId
import org.example.config.getRole
import org.example.config.isAdmin
import org.example.models.*
import org.example.services.UserService
import org.example.ForbiddenAccessException

fun Route.userRoutes() {
    val userService = UserService(
        userRepository = org.example.repositories.UserRepository(),
        tokenRepository = org.example.repositories.TokenRepository()
    )
    
    route("/api/users") {
        authenticate("auth-jwt") {
            // Get all users (admin only)
            get {
                val principal = call.principal<JWTPrincipal>()
                
                // Check if user is admin
                if (!principal!!.isAdmin()) {
                    throw ForbiddenAccessException()
                }
                
                val users = userService.getAllUsers()
                call.respond(HttpStatusCode.OK, users)
            }
            
            // Get specific user
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")
                
                val user = userService.getUser(id)
                call.respond(HttpStatusCode.OK, user)
            }
            
            // Update user (owner or admin)
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")
                
                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal!!.getUserId()
                val isAdmin = principal.isAdmin()
                
                // Check if user has permission to update this user
                if (id != currentUserId && !isAdmin) {
                    throw ForbiddenAccessException()
                }
                
                val request = call.receive<UserUpdateDTO>()
                
                // Validate request
                if (request.username != null && request.username.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Username cannot be empty")
                    return@put
                }
                
                if (request.email != null && !request.email.contains("@")) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email format")
                    return@put
                }
                
                val updatedUser = userService.updateUser(id, request, currentUserId, isAdmin)
                call.respond(HttpStatusCode.OK, updatedUser)
            }
            
            // Delete user (owner or admin)
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")
                
                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal!!.getUserId()
                val isAdmin = principal.isAdmin()
                
                // Check if user has permission to delete this user
                if (id != currentUserId && !isAdmin) {
                    throw ForbiddenAccessException()
                }
                
                val success = userService.deleteUser(id, currentUserId, isAdmin)
                
                if (success) {
                    call.respond(HttpStatusCode.OK, MessageResponse(message = "User deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, MessageResponse(message = "Failed to delete user"))
                }
            }
        }
    }
}