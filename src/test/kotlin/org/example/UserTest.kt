package org.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.example.models.LoginRequest
import org.example.models.RegisterRequest
import org.example.models.UserUpdateDTO
import kotlin.test.*

class UserTest {

    private suspend fun registerAndLoginAdmin(client: io.ktor.client.HttpClient): String {
        // Register admin user
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    RegisterRequest.serializer(),
                    RegisterRequest(
                        username = "admin",
                        email = "admin@example.com",
                        password = "adminpassword"
                    )
                )
            )
        }

        // We need to manually set the admin role in a real application
        // For testing purposes, we'll assume the first user is an admin

        // Login as admin
        val loginResponse = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    LoginRequest.serializer(),
                    LoginRequest(
                        email = "admin@example.com",
                        password = "adminpassword"
                    )
                )
            )
        }

        val responseBody = loginResponse.bodyAsText()
        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
        return jsonResponse["token"]?.jsonPrimitive?.content ?: ""
    }

    private suspend fun registerAndLoginRegularUser(client: io.ktor.client.HttpClient): Pair<String, Int> {
        // Register regular user
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    RegisterRequest.serializer(),
                    RegisterRequest(
                        username = "regularuser",
                        email = "user@example.com",
                        password = "userpassword"
                    )
                )
            )
        }

        // Login as regular user
        val loginResponse = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    LoginRequest.serializer(),
                    LoginRequest(
                        email = "user@example.com",
                        password = "userpassword"
                    )
                )
            )
        }

        val responseBody = loginResponse.bodyAsText()
        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
        val token = jsonResponse["token"]?.jsonPrimitive?.content ?: ""
        val userId = jsonResponse["userId"]?.jsonPrimitive?.int ?: 0

        return Pair(token, userId)
    }

    @Test
    fun testGetUser() = testApplication {
        // Register and login a user
        val (userToken, userId) = registerAndLoginRegularUser(client)

        // Get user by ID
        val getUserResponse = client.get("/api/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $userToken")
        }

        assertEquals(HttpStatusCode.OK, getUserResponse.status)

        // Parse response to verify user info
        val responseBody = getUserResponse.bodyAsText()
        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("regularuser", jsonResponse["username"]?.jsonPrimitive?.content)
        assertEquals("user@example.com", jsonResponse["email"]?.jsonPrimitive?.content)
    }

    @Test
    fun testUpdateUser() = testApplication {
        // Register and login a user
        val (userToken, userId) = registerAndLoginRegularUser(client)

        // Generate unique username and email with timestamp
        val timestamp = System.currentTimeMillis()
        val uniqueUsername = "updateduser$timestamp"
        val uniqueEmail = "updated$timestamp@example.com"

        // Update user
        val updateResponse = client.put("/api/users/$userId") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $userToken")
            setBody(
                Json.encodeToString(
                    UserUpdateDTO.serializer(),
                    UserUpdateDTO(
                        username = uniqueUsername,
                        email = uniqueEmail
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, updateResponse.status)

        // Parse response to verify updated user info
        val responseBody = updateResponse.bodyAsText()
        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals(uniqueUsername, jsonResponse["username"]?.jsonPrimitive?.content)
        assertEquals(uniqueEmail, jsonResponse["email"]?.jsonPrimitive?.content)
    }

    @Test
    fun testDeleteUser() = testApplication {
        // Register and login a user
        val (userToken, userId) = registerAndLoginRegularUser(client)

        // Delete user
        val deleteResponse = client.delete("/api/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $userToken")
        }

        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Try to get deleted user (should fail)
        val getUserResponse = client.get("/api/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $userToken")
        }

        assertEquals(HttpStatusCode.NotFound, getUserResponse.status)
    }

    @Test
    fun testAdminListUsers() = testApplication {
        // Register and login admin
        val adminToken = registerAndLoginAdmin(client)

        // Register a regular user
        registerAndLoginRegularUser(client)

        // List all users (admin only)
        val listUsersResponse = client.get("/api/users") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }

        assertEquals(HttpStatusCode.OK, listUsersResponse.status)

        // Parse response to verify users list
        val responseBody = listUsersResponse.bodyAsText()
        val jsonArray = Json.parseToJsonElement(responseBody).jsonArray

        assertTrue(jsonArray.size >= 2) // At least admin and regular user
    }
}
