package org.example.repositories

import org.example.config.DatabaseFactory.dbQuery
import org.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import at.favre.lib.crypto.bcrypt.BCrypt

class UserRepository {
    
    suspend fun createUser(userCreate: UserCreateDTO): User? {
        val passwordHash = hashPassword(userCreate.password)
        
        val userId = dbQuery {
            Users.insert {
                it[username] = userCreate.username
                it[email] = userCreate.email
                it[this.passwordHash] = passwordHash
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            } get Users.id
        }
        
        return userId?.value?.let { getUser(it) }
    }
    
    suspend fun getUser(id: Int): User? {
        return dbQuery {
            Users.select { Users.id eq id }
                .mapNotNull { toUser(it) }
                .singleOrNull()
        }
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return dbQuery {
            Users.select { Users.email eq email }
                .mapNotNull { toUser(it) }
                .singleOrNull()
        }
    }
    
    suspend fun getUserByUsername(username: String): User? {
        return dbQuery {
            Users.select { Users.username eq username }
                .mapNotNull { toUser(it) }
                .singleOrNull()
        }
    }
    
    suspend fun getAllUsers(): List<User> {
        return dbQuery {
            Users.selectAll()
                .mapNotNull { toUser(it) }
        }
    }
    
    suspend fun updateUser(id: Int, userUpdate: UserUpdateDTO): User? {
        dbQuery {
            Users.update({ Users.id eq id }) {
                userUpdate.username?.let { username -> it[Users.username] = username }
                userUpdate.email?.let { email -> it[Users.email] = email }
                it[updatedAt] = LocalDateTime.now()
            }
        }
        
        return getUser(id)
    }
    
    suspend fun deleteUser(id: Int): Boolean {
        val deletedRows = dbQuery {
            Users.deleteWhere { Users.id eq id }
        }
        
        return deletedRows > 0
    }
    
    suspend fun validateCredentials(email: String, password: String): User? {
        val user = getUserByEmail(email)
        
        return if (user != null && verifyPassword(password, user.passwordHash)) {
            user
        } else {
            null
        }
    }
    
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }
    
    private fun verifyPassword(password: String, passwordHash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified
    }
    
    private fun toUser(row: ResultRow): User =
        User(
            id = row[Users.id].value,
            username = row[Users.username],
            email = row[Users.email],
            passwordHash = row[Users.passwordHash],
            role = row[Users.role],
            createdAt = row[Users.createdAt],
            updatedAt = row[Users.updatedAt]
        )
    
    fun toUserDTO(user: User): UserDTO =
        UserDTO(
            userId = user.id,
            username = user.username,
            email = user.email,
            role = user.role
        )
}