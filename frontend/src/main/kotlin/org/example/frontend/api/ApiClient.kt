package org.example.frontend.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

object ApiClient {
    private const val API_URL = "http://localhost:8080"
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    // Auth API
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("$API_URL/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                // Store token in localStorage
                localStorage.setItem("auth_token", authResponse.token)
                localStorage.setItem("user_id", authResponse.userId.toString())
                localStorage.setItem("username", authResponse.username)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, email: String, password: String): Result<MessageResponse> {
        return try {
            val response = client.post("$API_URL/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password))
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Registration failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): Result<UserDTO> {
        return try {
            val token = localStorage.getItem("auth_token") ?: return Result.failure(Exception("Not logged in"))
            
            val response = client.get("$API_URL/api/auth/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get current user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Boolean> {
        localStorage.removeItem("auth_token")
        localStorage.removeItem("user_id")
        localStorage.removeItem("username")
        return Result.success(true)
    }
    
    // User API
    
    suspend fun getUsers(): Result<List<UserDTO>> {
        return try {
            val token = localStorage.getItem("auth_token") ?: return Result.failure(Exception("Not logged in"))
            
            val response = client.get("$API_URL/api/users") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get users: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUser(userId: Int): Result<UserDTO> {
        return try {
            val token = localStorage.getItem("auth_token") ?: return Result.failure(Exception("Not logged in"))
            
            val response = client.get("$API_URL/api/users/$userId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(userId: Int, username: String? = null, email: String? = null): Result<UserDTO> {
        return try {
            val token = localStorage.getItem("auth_token") ?: return Result.failure(Exception("Not logged in"))
            
            val response = client.put("$API_URL/api/users/$userId") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(UserUpdateDTO(username, email))
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to update user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUser(userId: Int): Result<MessageResponse> {
        return try {
            val token = localStorage.getItem("auth_token") ?: return Result.failure(Exception("Not logged in"))
            
            val response = client.delete("$API_URL/api/users/$userId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to delete user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}