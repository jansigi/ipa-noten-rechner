package ch.jf.ipa.repository

import ch.jf.ipa.config.DatabaseFactory
import java.util.UUID
import kotlin.test.AfterTest

abstract class RepositoryTestBase {
    protected fun initDatabase() {
        val name = UUID.randomUUID().toString()
        DatabaseFactory.init(
            DatabaseFactory.Settings(
                url = "jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                driver = "org.h2.Driver",
                user = "sa",
                password = "",
            ),
        )
    }

    @AfterTest
    fun tearDown() {
        DatabaseFactory.close()
    }
}

