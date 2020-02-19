@file:Suppress("MemberVisibilityCanBePrivate")

package com.edemidov.fin.api

import com.edemidov.fin.IntegrationTestExtension
import com.edemidov.fin.entity.AccountStatus
import com.edemidov.fin.injector
import com.edemidov.fin.port
import com.edemidov.fin.service.AccountService
import com.edemidov.fin.service.TransactionService
import com.github.kittinunf.fuel.httpPost
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ExtendWith(IntegrationTestExtension::class)
class ConcurrentTransactionsIntegrationTest {
    val threadPool: ExecutorService = Executors.newFixedThreadPool(10)
    val accountService: AccountService = injector.getInstance(AccountService::class.java)

    @Test
    fun `multiple concurrent transactions on two accounts must not fail, money must not be lost`() {
        val id1 = accountService.create(AccountResource("Bill", AccountStatus.ACTIVE, BigDecimal("1000.00"))).id!!
        val id2 = accountService.create(AccountResource("Tim", AccountStatus.ACTIVE, BigDecimal("1000.00"))).id!!
        val url = "http://localhost:$port/api/v1/transactions"
        for (i in 1..100) {
            if (i % 2 == 0) {
                threadPool.submit {
                    url.httpPost()
                        .body(toJson(TransactionResource(id1, id2, BigDecimal("20.00"))))
                        .responseString()
                }
            } else {
                threadPool.submit {
                    url.httpPost()
                        .body(toJson(TransactionResource(id2, id1, BigDecimal("10.00"))))
                        .responseString()
                }
            }
        }
        threadPool.shutdown()
        threadPool.awaitTermination(10, TimeUnit.SECONDS)

        assertAccountsHaveCorrectAmounts(id1, id2)
        assertTransactionsCreated()
    }

    private fun assertAccountsHaveCorrectAmounts(id1: Long, id2: Long) {
        val amountFirst = accountService.retrieveOne(id1).amount
        val amountSecond = accountService.retrieveOne(id2).amount
        assertThat(amountFirst).isEqualTo("500.00")
        assertThat(amountSecond).isEqualTo("1500.00")
    }

    private fun assertTransactionsCreated() {
        val transactionService = injector.getInstance(TransactionService::class.java)
        val (transactions, nextCursor) = transactionService.retrieveWithCursor(CursorRequest(0, 100))
        assertThat(transactions).hasSize(100)
        assertThat(nextCursor).isEqualTo(0)
    }
}