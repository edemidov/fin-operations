package com.edemidov.fin.repository

import com.edemidov.fin.entity.Transaction
import com.edemidov.fin.tables.Transactions
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

@Singleton
class TransactionRepository {

    fun fetchById(transactionId: Long): Transaction? {
        return Transactions.select { Transactions.id eq transactionId }
            .map(::resultRowToTransaction)
            .firstOrNull()
    }

    fun fetch(cursor: Long, numberOfEntitiesToFetch: Int, sourceAccountId: Long?, targetAccountId: Long?): List<Transaction> {
        return Transactions.select {
            var where = Transactions.id.greater(cursor)
            sourceAccountId?.also { where = where.and(Transactions.sourceAccountId eq it) }
            targetAccountId?.also { where = where.and(Transactions.targetAccountId eq it) }
            where
        }
            .orderBy(Transactions.id)
            .limit(numberOfEntitiesToFetch)
            .map(::resultRowToTransaction)
    }

    fun create(transaction: Transaction): Long {
        return Transactions.insertAndGetId {
            it[sourceAccountId] = transaction.sourceAccountId
            it[targetAccountId] = transaction.targetAccountId
            it[amount] = transaction.amount
        }.value
    }

    private fun resultRowToTransaction(row: ResultRow) =
        Transaction(
            id = row[Transactions.id].value,
            operationTime = row[Transactions.operationTime],
            sourceAccountId = row[Transactions.sourceAccountId],
            targetAccountId = row[Transactions.targetAccountId],
            amount = row[Transactions.amount]
        )
}