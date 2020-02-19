package com.edemidov.fin.repository

import com.edemidov.fin.api.ACCOUNT_NOT_FOUND
import com.edemidov.fin.api.ResourceNotFoundException
import com.edemidov.fin.entity.Account
import com.edemidov.fin.tables.Accounts
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateStatement

@Singleton
class AccountRepository {

    fun fetchById(accountId: Long): Account? {
        return Accounts.select { (Accounts.id eq accountId) and (Accounts.removed eq false) }
            .map(::resultRowToAccount)
            .firstOrNull()
    }

    fun fetch(cursor: Long, numberOfEntitiesToFetch: Int): List<Account> {
        return Accounts.select { Accounts.id.greater(cursor) and (Accounts.removed eq false) }
                .orderBy(Accounts.id)
                .limit(numberOfEntitiesToFetch)
                .map(::resultRowToAccount)
    }

    fun create(account: Account): Long {
        return Accounts.insertAndGetId {
            it[name] = account.name
            it[status] = account.status
            it[amount] = account.amount
        }.value
    }

    fun modify(id: Long, validation: ((Account) -> Unit)? = null, update: Accounts.(UpdateStatement, Account) -> Unit) {
        var rowUpdated = false
        while (!rowUpdated) {
            val existingAccount = fetchById(id) ?: throw ResourceNotFoundException(ACCOUNT_NOT_FOUND(id))
            validation?.invoke(existingAccount)
            rowUpdated = tryModify(existingAccount, update)
        }
    }

    fun tryModifyTwo(firstAccount: Account,
                     firstUpdate: Accounts.(UpdateStatement, Account) -> Unit,
                     secondAccount: Account,
                     secondUpdate: Accounts.(UpdateStatement, Account) -> Unit): Boolean {
        // to prevent DB deadlocks during concurrent transactions the order of operations is adjusted
        return if (firstAccount.id < secondAccount.id) {
            tryModifyTwoOrdered(firstAccount, firstUpdate, secondAccount, secondUpdate)
        } else {
            tryModifyTwoOrdered(secondAccount, secondUpdate, firstAccount, firstUpdate)
        }
    }

    private fun tryModifyTwoOrdered(firstAccount: Account, firstUpdate: Accounts.(UpdateStatement, Account) -> Unit,
                                    secondAccount: Account, secondUpdate: Accounts.(UpdateStatement, Account) -> Unit): Boolean {
        val success = tryModify(firstAccount, firstUpdate)
        if (!success) {
            return false
        }
        return tryModify(secondAccount, secondUpdate)
    }

    private fun tryModify(account: Account, update: Accounts.(UpdateStatement, Account) -> Unit): Boolean {
        val updatedRows = Accounts.update({ (Accounts.id eq account.id) and (Accounts.version eq account.version) }) {
            update(it, account)
            it[version] = account.version + 1L
        }
        return updatedRows != 0
    }

    private fun resultRowToAccount(row: ResultRow) =
            Account(
                    id = row[Accounts.id].value,
                    name = row[Accounts.name],
                    status = row[Accounts.status],
                    amount = row[Accounts.amount],
                    version = row[Accounts.version]
            )
}