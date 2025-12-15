package ch.jf.ipa.config

import ch.jf.ipa.model.CriterionProgressTable
import ch.jf.ipa.model.PersonsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.ApplicationEnvironment
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    data class Settings(
        val url: String,
        val driver: String,
        val user: String,
        val password: String,
    )

    private var dataSource: HikariDataSource? = null
    private lateinit var database: Database

    fun init(environment: ApplicationEnvironment) {
        val config = environment.config.config("ktor.database")
        val settings = Settings(
            url = config.property("url").getString(),
            driver = config.property("driver").getString(),
            user = config.property("user").getString(),
            password = config.propertyOrNull("password")?.getString().orEmpty(),
        )
        init(settings)
    }

    fun init(settings: Settings) {
        dataSource?.close()
        val hikariDataSource = createDataSource(settings)
        dataSource = hikariDataSource
        database = Database.connect(hikariDataSource)
        transaction(database) {
            SchemaUtils.create(PersonsTable, CriterionProgressTable)
        }
    }

    private fun createDataSource(settings: Settings): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = settings.url
            driverClassName = settings.driver
            username = settings.user
            password = settings.password
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T {
        check(::database.isInitialized) { "Database has not been initialized. Call DatabaseFactory.init() first." }
        return newSuspendedTransaction(Dispatchers.IO, database) {
            block()
        }
    }

    fun close() {
        if (::database.isInitialized) {
            TransactionManager.closeAndUnregister(database)
        }
        dataSource?.close()
        dataSource = null
    }
}

