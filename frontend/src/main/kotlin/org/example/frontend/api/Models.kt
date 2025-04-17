package org.example.frontend.api

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val userId: Int,
    val username: String,
    val email: String,
    val role: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Int,
    val username: String
)

@Serializable
data class UserUpdateDTO(
    val username: String? = null,
    val email: String? = null
)

@Serializable
data class MessageResponse(
    val message: String,
    val userId: Int? = null
)