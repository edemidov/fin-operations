@file:Suppress("unused")

package com.edemidov.fin.config

import com.edemidov.fin.service.AccountService
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import javax.sql.DataSource


open class AppModule : AbstractModule() {

    override fun configure() {
        bind(AccountService::class.java).`in`(Singleton::class.java)
    }

    @Provides
    @Singleton
    private fun dataSource(postgres: EmbeddedPostgres): DataSource {
        val url: String = postgres.start("localhost", 5434, "dbName", "userName", "password")
        val config = HikariConfig()
        config.username = "userName"
        config.password = "password"
        config.jdbcUrl = url
        config.isAutoCommit = false
        return HikariDataSource(config)
    }

    @Provides
    @Singleton
    private fun postgres(): EmbeddedPostgres {
        return EmbeddedPostgres()
    }
}