package com.edemidov.fin

import com.edemidov.fin.api.*
import com.edemidov.fin.config.AppModule
import com.edemidov.fin.entity.AccountStatus
import com.edemidov.fin.service.AccountService
import com.edemidov.fin.service.TransactionService
import com.edemidov.fin.tables.Accounts
import com.edemidov.fin.tables.Transactions
import com.fasterxml.jackson.core.JsonParseException
import com.google.inject.Guice
import com.google.inject.Injector
import org.eclipse.jetty.http.HttpStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import spark.Spark.*
import java.math.BigDecimal
import javax.sql.DataSource

@Suppress("UnusedMainParameter")
fun main(args: Array<String>) {
    val injector = Guice.createInjector(AppModule())
    initDbSchema(injector.getInstance(DataSource::class.java))
    initializeTestData(injector)
    configureWeb(injector, 8080)
}

fun configureWeb(injector: Injector, port: Int) {
    port(port)
    afterAfter("/api/*") { _, response ->
        if (response.body() != null) {
            response.type("application/json")
        }
    }
    exception(ApiException::class.java) { exception, _, response ->
        response.status(exception.status)
        response.body(toJson(ErrorResource(exception.errors)))
    }
    exception(JsonParseException::class.java) { exception, _, response ->
        response.status(HttpStatus.BAD_REQUEST_400)
        response.body(toJson(JSON_PARSE_ERROR(exception.message ?: "")))
    }
    exception(Exception::class.java) { exception, _, response ->
        exception.printStackTrace()
        response.status(500)
        response.body(toJson(ErrorResource(listOf(UNEXPECTED_ERROR_OCCURRED(exception)))))
    }
    injector.getInstance(AccountRoutesInitializer::class.java).init()
    injector.getInstance(TransactionRoutesInitializer::class.java).init()
}

fun initDbSchema(dataSource: DataSource) {
    Database.connect(dataSource)
    transaction {
        SchemaUtils.drop(Accounts, Transactions)
        exec("DROP TYPE IF EXISTS ACCOUNT_STATUS")
    }
    transaction {
        exec("CREATE TYPE ACCOUNT_STATUS AS ENUM ('ACTIVE', 'BLOCKED');")
        SchemaUtils.create(Accounts, Transactions)
    }
}

fun initializeTestData(injector: Injector) {
    val accountService = injector.getInstance(AccountService::class.java)
    val firstAccountId = accountService.create(AccountResource(
        name = "John Smith",
        status = AccountStatus.ACTIVE,
        amount = BigDecimal("1000.00")
    )).id!!
    val secondAccountId = accountService.create(AccountResource(
        name = "Tim Walker",
        status = AccountStatus.ACTIVE,
        amount = BigDecimal("5000.00")
    )).id!!
    accountService.create(AccountResource(
        name = "Bill Green",
        status = AccountStatus.BLOCKED,
        amount = BigDecimal("5000.00")
    ))
    val transactionService = injector.getInstance(TransactionService::class.java)
    transactionService.makeTransaction(TransactionResource(
        sourceAccountId = firstAccountId,
        targetAccountId = secondAccountId,
        amount = BigDecimal("100.00")
    ))
    transactionService.makeTransaction(TransactionResource(
        sourceAccountId = secondAccountId,
        targetAccountId = firstAccountId,
        amount = BigDecimal("100.00")
    ))
}