package org.example.repositories

import org.example.config.DatabaseFactory.dbQuery
import org.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import java.time.LocalDateTime

class TokenRepository {

    suspend fun createToken(userId: Int, token: String, expiresAt: LocalDateTime): Token? {
        val tokenId = dbQuery {
            Tokens.insert {
                it[this.userId] = userId
                it[this.token] = token
                it[this.expiresAt] = expiresAt
                it[createdAt] = LocalDateTime.now()
            } get Tokens.id
        }

        return tokenId?.value?.let { getToken(it) }
    }

    suspend fun getToken(id: Int): Token? {
        return dbQuery {
            Tokens.select { Tokens.id eq id }
                .mapNotNull { toToken(it) }
                .singleOrNull()
        }
    }

    suspend fun getTokenByValue(token: String): Token? {
        return dbQuery {
            Tokens.select { Tokens.token eq token }
                .mapNotNull { toToken(it) }
                .singleOrNull()
        }
    }

    suspend fun getUserTokens(userId: Int): List<Token> {
        return dbQuery {
            Tokens.select { Tokens.userId eq userId }
                .mapNotNull { toToken(it) }
        }
    }

    suspend fun deleteToken(id: Int): Boolean {
        val deletedRows = dbQuery {
            Tokens.deleteWhere { Tokens.id eq id }
        }

        return deletedRows > 0
    }

    suspend fun deleteUserTokens(userId: Int): Boolean {
        val deletedRows = dbQuery {
            Tokens.deleteWhere { Tokens.userId eq userId }
        }

        return deletedRows > 0
    }

    suspend fun deleteExpiredTokens(): Int {
        return dbQuery {
            Tokens.deleteWhere { Tokens.expiresAt less LocalDateTime.now() }
        }
    }

    suspend fun isTokenValid(token: String): Boolean {
        val tokenEntity = getTokenByValue(token)

        return tokenEntity != null && tokenEntity.expiresAt.isAfter(LocalDateTime.now())
    }

    private fun toToken(row: ResultRow): Token =
        Token(
            id = row[Tokens.id].value,
            userId = row[Tokens.userId].value,
            token = row[Tokens.token],
            expiresAt = row[Tokens.expiresAt],
            createdAt = row[Tokens.createdAt]
        )
}
