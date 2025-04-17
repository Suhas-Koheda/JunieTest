package org.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.example.models.AuthResponse
import org.example.models.LoginRequest
import org.example.models.RegisterRequest
import kotlin.test.*

class AuthTest {
    
    @Test
    fun testRegisterAndLogin() = testApplication {
        // Test registration
        val registerResponse = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    RegisterRequest.serializer(),
                    RegisterRequest(
                        username = "testuser",
                        email = "test@example.com",
                        password = "password123"
                    )
                )
            )
        }
        
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        
        // Test login
        val loginResponse = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    LoginRequest.serializer(),
                    LoginRequest(
                        email = "test@example.com",
                        password = "password123"
                    )
                )
            )
        }
        
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        
        // Parse response to get token
        val responseBody = loginResponse.bodyAsText()
        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
        
        // Verify response contains token and user info
        assertTrue(jsonResponse.containsKey("token"))
        assertTrue(jsonResponse.containsKey("userId"))
        assertTrue(jsonResponse.containsKey("username"))
        
        // Test get current user (me) endpoint
        val token = jsonResponse["token"]?.jsonPrimitive?.content
        assertNotNull(token, "Token should not be null")
        
        val meResponse = client.get("/api/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, meResponse.status)
        
        // Parse response to verify user info
        val meResponseBody = meResponse.bodyAsText()
        val meJsonResponse = Json.parseToJsonElement(meResponseBody).jsonObject
        
        assertEquals("testuser", meJsonResponse["username"]?.jsonPrimitive?.content)
        assertEquals("test@example.com", meJsonResponse["email"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testInvalidLogin() = testApplication {
        // Test login with invalid credentials
        val loginResponse = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    LoginRequest.serializer(),
                    LoginRequest(
                        email = "nonexistent@example.com",
                        password = "wrongpassword"
                    )
                )
            )
        }
        
        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
    }
    
    @Test
    fun testInvalidRegistration() = testApplication {
        // Test registration with invalid data (short password)
        val registerResponse = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    RegisterRequest.serializer(),
                    RegisterRequest(
                        username = "testuser2",
                        email = "test2@example.com",
                        password = "short"
                    )
                )
            )
        }
        
        assertEquals(HttpStatusCode.BadRequest, registerResponse.status)
    }
}