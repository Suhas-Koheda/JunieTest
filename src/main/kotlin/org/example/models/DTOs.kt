package org.example.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// User DTOs
@Serializable
data class UserDTO(
    val userId: Int,
    val username: String,
    val email: String,
    val role: String
)

@Serializable
data class UserCreateDTO(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class UserUpdateDTO(
    val username: String? = null,
    val email: String? = null
)

// Authentication DTOs
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
data class MessageResponse(
    val message: String,
    val userId: Int? = null
)

// Internal models (not serialized)
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val role: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Token(
    val id: Int,
    val userId: Int,
    val token: String,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime
)