package com.edemidov.fin.config

import com.edemidov.fin.service.AccountService
import com.google.gson.Gson
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.h2.jdbcx.JdbcConnectionPool
import javax.sql.DataSource


class AppModule : AbstractModule() {

    override fun configure() {
        bind(AccountService::class.java).`in`(Singleton::class.java)
    }

    @Provides
    @Singleton
    private fun gson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    private fun dataSource(): DataSource {
        return JdbcConnectionPool.create("jdbc:h2:mem:testdb", "test", "test")
    }
}