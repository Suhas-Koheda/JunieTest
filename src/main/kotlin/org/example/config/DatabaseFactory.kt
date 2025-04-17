package org.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.example.models.Users
import org.example.models.Tokens
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("database.driverClassName").getString()
        val jdbcURL = config.property("database.jdbcURL").getString()
        val username = config.property("database.username").getString()
        val password = config.property("database.password").getString()
        val maxPoolSize = config.property("database.maxPoolSize").getString().toInt()

        try {
            val dataSource = hikari(driverClassName, jdbcURL, username, password, maxPoolSize)
            val database = Database.connect(dataSource)
            
            // Create tables
            transaction(database) {
                SchemaUtils.create(Users)
                SchemaUtils.create(Tokens)
                logger.info("Database tables created or already exist")
            }
        } catch (e: Exception) {
            logger.error("Failed to connect to database: ${e.message}")
            throw e
        }
    }

    private fun hikari(driverClassName: String, jdbcURL: String, username: String, password: String, maxPoolSize: Int): HikariDataSource {
        val config = HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcURL
            this.username = username
            this.password = password
            this.maximumPoolSize = maxPoolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            this.validate()
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}