package org.example.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import java.util.*
import org.example.models.User

class JWTConfig(private val config: ApplicationConfig) {
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val expiresIn = config.property("jwt.expiresIn").getString().toLong()
    
    private val algorithm = Algorithm.HMAC256(secret)
    
    fun generateToken(user: User): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", user.id)
            .withClaim("username", user.username)
            .withClaim("role", user.role)
            .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
            .sign(algorithm)
    }
    
    fun verifyToken(token: String): JWTPrincipal? {
        return try {
            val jwtVerifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
            
            val jwtToken = jwtVerifier.verify(token)
            JWTPrincipal(jwtToken)
        } catch (e: Exception) {
            null
        }
    }
}

fun Application.configureJWT(config: ApplicationConfig) {
    val jwtConfig = JWTConfig(config)
    val jwtRealm = config.property("jwt.realm").getString()
    
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier {
                JWT.require(Algorithm.HMAC256(config.property("jwt.secret").getString()))
                    .withIssuer(config.property("jwt.issuer").getString())
                    .withAudience(config.property("jwt.audience").getString())
                    .build()
            }
            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != 0) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                throw org.example.UnauthorizedAccessException()
            }
        }
    }
}

// Extension function to get user ID from JWT principal
fun JWTPrincipal.getUserId(): Int = payload.getClaim("userId").asInt()

// Extension function to get username from JWT principal
fun JWTPrincipal.getUsername(): String = payload.getClaim("username").asString()

// Extension function to get user role from JWT principal
fun JWTPrincipal.getRole(): String = payload.getClaim("role").asString()

// Extension function to check if user is admin
fun JWTPrincipal.isAdmin(): Boolean = getRole() == "admin"