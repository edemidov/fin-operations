package com.edemidov.fin.service

import com.edemidov.fin.api.*
import com.edemidov.fin.entity.Account
import com.edemidov.fin.entity.AccountStatus
import com.edemidov.fin.entity.Transaction
import com.edemidov.fin.repository.AccountRepository
import com.edemidov.fin.repository.TransactionRepository
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

@Singleton
class TransactionService @Inject constructor(private val transactionRepository: TransactionRepository,
                                             private val accountRepository: AccountRepository) {

    fun retrieveOne(transactionId: Long): TransactionResource {
        return transaction {
            transactionRepository.fetchById(transactionId)
                ?.let(::transactionToResource)
        } ?: throw throw ResourceNotFoundException(TRANSACTION_NOT_FOUND(transactionId))
    }

    fun retrieveWithCursor(cursorRequest: CursorRequest, sourceAccountId: Long? = null, targetAccountId: Long? = null): CursorResponse<TransactionResource> {
        return transaction {
            val fetchedResources = transactionRepository.fetch(cursorRequest.cursor, cursorRequest.numberOfEntitiesToFetch, sourceAccountId, targetAccountId)
                .map(::transactionToResource)
            toCursorResponse(fetchedResources, cursorRequest)
        }
    }

    fun makeTransaction(transactionResource: TransactionResource): TransactionResource {
        val transactionEntity = Transaction(
            id = 0,
            operationTime = null,
            sourceAccountId = transactionResource.sourceAccountId,
            targetAccountId = transactionResource.targetAccountId,
            amount = transactionResource.amount
        )
        var transactionId: Long? = null
        while (transactionId == null) {
            transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED, repetitionAttempts = 3) {
                val sourceAccount = accountRepository.fetchById(transactionEntity.sourceAccountId)
                val targetAccount = accountRepository.fetchById(transactionEntity.targetAccountId)
                validateTransaction(transactionEntity, sourceAccount, targetAccount)
                val success = accountRepository.tryModifyTwo(
                    sourceAccount!!,
                    { statement, account ->
                        statement[amount] = account.amount.subtract(transactionEntity.amount)
                    },
                    targetAccount!!,
                    { statement, account ->
                        statement[amount] = account.amount.add(transactionEntity.amount)
                    })
                if (success) {
                    transactionId = transactionRepository.create(transactionEntity)
                } else {
                    rollback()
                }
            }
        }
        return retrieveOne(transactionId!!)
    }

    private fun validateTransaction(transaction: Transaction, sourceAccount: Account?, targetAccount: Account?) {
        if (transaction.sourceAccountId == transaction.targetAccountId) {
            throw BadRequestException(SELF_TRANSACTION(transaction.sourceAccountId))
        }
        val errors = mutableListOf<ErrorDto>()
        if (sourceAccount == null) {
            errors.add(ACCOUNT_NOT_FOUND(transaction.sourceAccountId))
        } else {
            if (sourceAccount.status == AccountStatus.BLOCKED) {
                errors.add(ACCOUNT_BLOCKED(sourceAccount.id))
            }
            if (sourceAccount.amount < transaction.amount) {
                errors.add(INSUFFICIENT_FUNDS(sourceAccount.id))
            }
        }
        if (targetAccount == null) {
            errors.add(ACCOUNT_NOT_FOUND(transaction.targetAccountId))
        } else if (targetAccount.status == AccountStatus.BLOCKED) {
            errors.add(ACCOUNT_BLOCKED(targetAccount.id))
        }
        if (errors.isNotEmpty()) {
            throw BadRequestException(errors)
        }
    }

    private fun transactionToResource(transaction: Transaction) =
        TransactionResource(
            id = transaction.id,
            amount = transaction.amount,
            operationTime = transaction.operationTime!!,
            sourceAccountId = transaction.sourceAccountId,
            targetAccountId = transaction.targetAccountId
        )
}