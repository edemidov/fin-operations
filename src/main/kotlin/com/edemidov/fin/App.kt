package com.edemidov.fin

import com.edemidov.fin.config.AppModule
import com.edemidov.fin.service.AccountService
import com.edemidov.fin.tables.Accounts
import com.edemidov.fin.tables.Transactions
import com.google.inject.Guice
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import spark.Spark.port
import javax.sql.DataSource


fun main(args: Array<String>) {
    val injector = Guice.createInjector(AppModule())
    Database.connect(injector.getInstance(DataSource::class.java))
    transaction {
        SchemaUtils.create(Accounts, Transactions)
    }
    val accountService = injector.getInstance(AccountService::class.java)
    port(8080)
}
