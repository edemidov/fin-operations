package com.edemidov.fin.service

import com.edemidov.fin.api.*
import com.edemidov.fin.entity.Account
import com.edemidov.fin.entity.AccountStatus
import com.edemidov.fin.entity.Transaction
import com.edemidov.fin.repository.AccountRepository
import com.edemidov.fin.repository.TransactionRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class TransactionServiceTest(@MockK val transactionRepository: TransactionRepository,
                                      @MockK val accountRepository: AccountRepository) {

    private val transactionService = TransactionService(transactionRepository, accountRepository)

    init {
        Database.connect({ mockk() })
    }

    @Test
    fun `retrieve one - success`() {
        val now = Instant.now()
        val money = BigDecimal("10.10")
        every { transactionRepository.fetchById(15) }
            .returns(Transaction(15, now, 5, 9, money))
        val transaction = transactionService.retrieveOne(15L)

        with(transaction) {
            assertThat(id).isEqualTo(15)
            assertThat(operationTime).isEqualTo(now)
            assertThat(sourceAccountId).isEqualTo(5)
            assertThat(targetAccountId).isEqualTo(9)
            assertThat(amount).isEqualTo(money)
        }
    }

    @Test
    fun `retrieve one - fail with not found`() {
        every { transactionRepository.fetchById(15) }
            .returns(null)

        try {
            transactionService.retrieveOne(15L)
        } catch (e: ResourceNotFoundException) {
            assertThat(e.errors).containsOnly(TRANSACTION_NOT_FOUND(15))
            return
        }
        fail("Exception expected")
    }

    @Test
    fun `make transaction - success`() {
        val transaction = TransactionResource(1, 2, BigDecimal("1.50"))
        val sourceAccount = Account(1, "Bill", AccountStatus.ACTIVE, BigDecimal("300.00"))
        val targetAccount = Account(2, "Jim", AccountStatus.ACTIVE, BigDecimal("100.00"))
        runTransactionAndExpect(transaction, sourceAccount, targetAccount)
    }

    @Test
    fun `make transaction - fail because both accounts not found`() {
        val transaction = TransactionResource(1, 2, BigDecimal("1000.00"))
        val sourceAccount = null
        val targetAccount = null
        runTransactionAndExpect(transaction, sourceAccount, targetAccount, ACCOUNT_NOT_FOUND(1), ACCOUNT_NOT_FOUND(2))
    }

    @Test
    fun `make transaction - fail because source has not enough funds and target is blocked`() {
        val transaction = TransactionResource(1, 2, BigDecimal("1000.00"))
        val sourceAccount = Account(1, "Bill", AccountStatus.ACTIVE, BigDecimal("300.00"))
        val targetAccount = Account(2, "Jim", AccountStatus.BLOCKED, BigDecimal("100.00"))
        runTransactionAndExpect(transaction, sourceAccount, targetAccount, INSUFFICIENT_FUNDS(1), ACCOUNT_BLOCKED(2))
    }

    @Test
    fun `make transaction - fail because of self transaction`() {
        val transaction = TransactionResource(1, 1, BigDecimal("50.00"))
        val sourceAccount = Account(1, "Bill", AccountStatus.ACTIVE, BigDecimal("300.00"))
        val targetAccount = Account(2, "Jim", AccountStatus.ACTIVE, BigDecimal("100.00"))
        runTransactionAndExpect(transaction, sourceAccount, targetAccount, SELF_TRANSACTION(1))
    }

    private fun runTransactionAndExpect(transaction: TransactionResource,
                                        sourceAccount: Account?,
                                        targetAccount: Account?,
                                        vararg errors: ErrorDto) {
        val transactionEntity = Transaction(0, null, transaction.sourceAccountId, transaction.targetAccountId, transaction.amount)
        every { accountRepository.fetchById(1) }
            .returns(sourceAccount)
        every { accountRepository.fetchById(2) }
            .returns(targetAccount)
        every { accountRepository.tryModifyTwo(sourceAccount ?: any(), any(), targetAccount ?: any(), any()) }
            .returns(true)
        every { transactionRepository.create(transactionEntity) }
            .returns(22)
        val now = Instant.now()
        every { transactionRepository.fetchById(22) }
            .returns(transactionEntity.copy(id = 22, operationTime = now))
        try {
            val resultingTransaction = transactionService.makeTransaction(transaction)
            assertThat(resultingTransaction).isEqualTo(transaction.apply { id = 22; operationTime = now })
        } catch (apiEx: ApiException) {
            if (errors.isEmpty()) fail("unexpected exception: $apiEx")
            assertThat(apiEx.errors).containsOnly(*errors)
            return
        }
        if (errors.isNotEmpty()) fail("exceptions expected but not caught: $errors")
    }
}