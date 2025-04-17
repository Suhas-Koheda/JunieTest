package org.example.services

import org.example.models.*
import org.example.repositories.UserRepository
import org.example.repositories.TokenRepository
import org.example.NotFoundException
import org.example.ForbiddenAccessException

class UserService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository
) {

    suspend fun getAllUsers(): List<UserDTO> {
        return userRepository.getAllUsers().map { userRepository.toUserDTO(it) }
    }

    suspend fun getUser(id: Int): UserDTO {
        val user = userRepository.getUser(id)
            ?: throw NotFoundException("User not found")

        return userRepository.toUserDTO(user)
    }

    suspend fun updateUser(id: Int, request: UserUpdateDTO, currentUserId: Int, isAdmin: Boolean): UserDTO {
        // Check if user exists
        val user = userRepository.getUser(id)
            ?: throw NotFoundException("User not found")

        println("[DEBUG_LOG] Updating user: $id, username: ${user.username}, email: ${user.email}")
        println("[DEBUG_LOG] Request: username: ${request.username}, email: ${request.email}")

        // Check if current user has permission to update this user
        if (id != currentUserId && !isAdmin) {
            println("[DEBUG_LOG] Permission denied: id=$id, currentUserId=$currentUserId, isAdmin=$isAdmin")
            throw ForbiddenAccessException()
        }

        // Check if username is already taken
        if (request.username != null && request.username != user.username) {
            println("[DEBUG_LOG] Checking if username ${request.username} is already taken")
            userRepository.getUserByUsername(request.username)?.let {
                if (it.id != id) {
                    println("[DEBUG_LOG] Username already taken by user: ${it.id}")
                    throw IllegalArgumentException("Username already taken")
                }
            }
        }

        // Check if email is already taken
        if (request.email != null && request.email != user.email) {
            println("[DEBUG_LOG] Checking if email ${request.email} is already taken")
            userRepository.getUserByEmail(request.email)?.let {
                if (it.id != id) {
                    println("[DEBUG_LOG] Email already taken by user: ${it.id}")
                    throw IllegalArgumentException("Email already taken")
                }
            }
        }

        // Update user
        println("[DEBUG_LOG] Updating user in repository")
        val updatedUser = userRepository.updateUser(id, request)
            ?: throw IllegalArgumentException("Failed to update user")

        println("[DEBUG_LOG] User updated successfully: ${updatedUser.id}, username: ${updatedUser.username}, email: ${updatedUser.email}")
        return userRepository.toUserDTO(updatedUser)
    }

    suspend fun deleteUser(id: Int, currentUserId: Int, isAdmin: Boolean): Boolean {
        // Check if user exists
        userRepository.getUser(id)
            ?: throw NotFoundException("User not found")

        // Check if current user has permission to delete this user
        if (id != currentUserId && !isAdmin) {
            throw ForbiddenAccessException()
        }

        // Delete user's tokens
        tokenRepository.deleteUserTokens(id)

        // Delete user
        return userRepository.deleteUser(id)
    }
}
