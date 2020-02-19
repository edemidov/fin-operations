package com.edemidov.fin

import com.edemidov.fin.config.TestAppModule
import com.edemidov.fin.tables.Accounts
import com.edemidov.fin.tables.Transactions
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.kittinunf.fuel.jackson.defaultMapper
import com.google.inject.Guice
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import spark.Spark
import javax.sql.DataSource


class IntegrationTestExtension : BeforeAllCallback, BeforeEachCallback, CloseableResource {

    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            // used by test http client
            defaultMapper
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            startApplication()
            started = true
            context.root.getStore(GLOBAL).put("integration_tests", this)
        }
    }

    override fun beforeEach(context: ExtensionContext?) {
        // in case a unit test mocked transactions
        Database.connect(injector.getInstance(DataSource::class.java))
        transaction {
            Transactions.deleteAll()
            Accounts.deleteAll()
        }
    }

    override fun close() {
        Spark.stop()
        injector.getInstance(EmbeddedPostgres::class.java).stop()
    }

    private fun startApplication() {
        injector = Guice.createInjector(TestAppModule())
        initDbSchema(injector.getInstance(DataSource::class.java))
        configureWeb(injector, 0)
        Spark.awaitInitialization()
        port = Spark.port()
    }

    companion object {
        private var started = false

    }
}