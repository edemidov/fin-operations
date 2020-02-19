@file:Suppress("MemberVisibilityCanBePrivate")

package com.edemidov.fin.api

import com.edemidov.fin.IntegrationTestExtension
import com.edemidov.fin.assertResponseHasErrors
import com.edemidov.fin.entity.AccountStatus
import com.edemidov.fin.injector
import com.edemidov.fin.port
import com.edemidov.fin.service.AccountService
import com.edemidov.fin.service.TransactionService
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.responseObject
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import kotlin.properties.Delegates

@ExtendWith(IntegrationTestExtension::class)
internal class TransactionRoutesIntegrationTest {
    val baseUrl = "http://localhost:$port/api/v1/transactions"

    lateinit var transactionService: TransactionService
    var accountId1 by Delegates.notNull<Long>()
    var accountId2 by Delegates.notNull<Long>()
    var accountId3 by Delegates.notNull<Long>()
    lateinit var transaction1: TransactionResource
    lateinit var transaction2: TransactionResource

    @BeforeEach
    internal fun setUp() {
        val accountService = injector.getInstance(AccountService::class.java)
        accountId1 = accountService.create(AccountResource("Bill", AccountStatus.ACTIVE, BigDecimal("0.00"))).id!!
        accountId2 = accountService.create(AccountResource("Tim", AccountStatus.ACTIVE, BigDecimal("200.00"))).id!!
        accountId3 = accountService.create(AccountResource("Tom", AccountStatus.BLOCKED, BigDecimal("300.00"))).id!!
        transactionService = injector.getInstance(TransactionService::class.java)
        transaction1 = transactionService.makeTransaction(TransactionResource(accountId2, accountId1, BigDecimal("100.00")))
        transaction2 = transactionService.makeTransaction(TransactionResource(accountId1, accountId2, BigDecimal("100.00")))
    }

    @Test
    internal fun `get by id - success`() {
        val (_, response, result) = "$baseUrl/${transaction1.id}".httpGet()
            .responseObject<TransactionResource>()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result.get()).isEqualTo(transaction1)
    }

    @Test
    internal fun `get by id - not found`() {
        val (_, response, _) = "$baseUrl/${Long.MAX_VALUE}".httpGet()
            .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND_404)
        assertResponseHasErrors(response, TRANSACTION_NOT_FOUND(Long.MAX_VALUE))
    }

    @Test
    internal fun `get all in one batch - success`() {
        val (_, response, result) = "$baseUrl?size=10".httpGet()
            .responseObject<CursorResponse<TransactionResource>>()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result.get().content).containsExactly(transaction1, transaction2)
        assertThat(result.get().nextCursor).isEqualTo(0)
    }

    @Test
    internal fun `get all in two batches - success`() {
        val (_, response1, result1) = "$baseUrl?size=1".httpGet()
            .responseObject<CursorResponse<TransactionResource>>()
        assertThat(response1.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result1.get().content).containsExactly(transaction1)
        assertThat(result1.get().nextCursor).isNotEqualTo(0)

        val (_, response2, result2) = "$baseUrl?size=1&cursor=${result1.get().nextCursor}".httpGet()
            .responseObject<CursorResponse<TransactionResource>>()
        assertThat(response2.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result2.get().content).containsExactly(transaction2)
        assertThat(result2.get().nextCursor).isEqualTo(0)
    }

    @Test
    internal fun `get with additional search parameters - success`() {
        val (_, response, result) = "$baseUrl?size=10&sourceAccountId=$accountId1&targetAccountId=$accountId2".httpGet()
            .responseObject<CursorResponse<TransactionResource>>()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result.get().content).containsExactly(transaction2)
        assertThat(result.get().nextCursor).isEqualTo(0)
    }

    @Test
    internal fun `make transaction - success`() {
        val newTransaction = TransactionResource(accountId2, accountId1, BigDecimal("50.00"))
        val (_, response, result) = baseUrl.httpPost()
            .body(toJson(newTransaction))
            .responseObject<TransactionResource>()
        val returnedTransaction = result.get()
        val newId = returnedTransaction.id!!
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED_201)
        assertThat(response.header("Location").single()).isEqualTo("$baseUrl/$newId")
        val persistedTransaction = transactionService.retrieveOne(newId)
        with(persistedTransaction) {
            assertThat(sourceAccountId).isEqualTo(newTransaction.sourceAccountId)
            assertThat(targetAccountId).isEqualTo(newTransaction.targetAccountId)
            assertThat(amount).isEqualTo(newTransaction.amount)
        }
        assertThat(returnedTransaction).isEqualTo(persistedTransaction)
    }

    @Test
    internal fun `make transaction - fail with not enough funds and not found target account`() {
        val newTransaction = TransactionResource(accountId2, 6789, BigDecimal("1000.00"))
        val (_, response, _) = baseUrl.httpPost()
            .body(toJson(newTransaction))
            .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST_400)
        assertResponseHasErrors(response, INSUFFICIENT_FUNDS(accountId2), ACCOUNT_NOT_FOUND(6789))
    }
    
    @Test
    internal fun `make transaction - fail with blocked source account`() {
        val newTransaction = TransactionResource(accountId3, accountId1, BigDecimal("10.00"))
        val (_, response, _) = baseUrl.httpPost()
            .body(toJson(newTransaction))
            .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST_400)
        assertResponseHasErrors(response, ACCOUNT_BLOCKED(accountId3))
    }    
    
    @Test
    internal fun `make transaction - fail because source and target accounts are same`() {
        val newTransaction = TransactionResource(accountId2, accountId2, BigDecimal("1.00"))
        val (_, response, _) = baseUrl.httpPost()
            .body(toJson(newTransaction))
            .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST_400)
        assertResponseHasErrors(response, SELF_TRANSACTION(accountId2))
    }
}