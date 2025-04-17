package org.example.services

import org.example.config.JWTConfig
import org.example.models.*
import org.example.repositories.TokenRepository
import org.example.repositories.UserRepository
import org.example.UnauthorizedAccessException
import org.example.NotFoundException
import io.ktor.server.config.*
import java.time.LocalDateTime
import java.util.*

class AuthService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val config: ApplicationConfig
) {
    private val jwtConfig = JWTConfig(config)
    private val expiresIn = config.property("jwt.expiresIn").getString().toLong()

    suspend fun register(request: RegisterRequest): MessageResponse {
        // Check if user with same email or username already exists
        if (userRepository.getUserByEmail(request.email) != null) {
            throw IllegalArgumentException("User with this email already exists")
        }

        if (userRepository.getUserByUsername(request.username) != null) {
            throw IllegalArgumentException("User with this username already exists")
        }

        // Create user
        val userCreate = UserCreateDTO(
            username = request.username,
            email = request.email,
            password = request.password
        )

        // If username is "admin", set role to "admin"
        val role = if (request.username.lowercase() == "admin") "admin" else null

        val user = userRepository.createUser(userCreate, role)
            ?: throw IllegalArgumentException("Failed to create user")

        return MessageResponse(
            message = "User registered successfully",
            userId = user.id
        )
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        // Validate credentials
        val user = userRepository.validateCredentials(request.email, request.password)
            ?: throw UnauthorizedAccessException()

        // Generate JWT token
        val token = jwtConfig.generateToken(user)

        // Store token in database
        val expiresAt = LocalDateTime.now().plusSeconds(expiresIn / 1000)
        tokenRepository.createToken(user.id, token, expiresAt)

        return AuthResponse(
            token = token,
            userId = user.id,
            username = user.username
        )
    }

    suspend fun getUserInfo(userId: Int): UserDTO {
        val user = userRepository.getUser(userId)
            ?: throw NotFoundException("User not found")

        return userRepository.toUserDTO(user)
    }

    suspend fun validateToken(token: String): Boolean {
        return tokenRepository.isTokenValid(token)
    }

    suspend fun logout(token: String): Boolean {
        val tokenEntity = tokenRepository.getTokenByValue(token)

        return if (tokenEntity != null) {
            tokenRepository.deleteToken(tokenEntity.id)
        } else {
            false
        }
    }

    suspend fun cleanupExpiredTokens(): Int {
        return tokenRepository.deleteExpiredTokens()
    }
}
