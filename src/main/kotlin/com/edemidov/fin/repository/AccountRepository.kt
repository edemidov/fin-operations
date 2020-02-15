package com.edemidov.fin.repository

import com.edemidov.fin.api.ResourceNotFoundException
import com.edemidov.fin.entity.Account
import com.edemidov.fin.tables.Accounts
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update

@Singleton
class AccountRepository {

    fun create(account: Account): Long {
        return Accounts.insertAndGetId {
            it[name] = account.name
            it[notes] = account.notes
            it[status] = account.status
            it[amount] = account.amount
        }.value
    }

    fun fetchById(accountId: Long): Account {
        return Accounts.select { (Accounts.id eq accountId) and (Accounts.removed eq false) }
            .map {
                Account(
                    accountId,
                    it[Accounts.name],
                    notes = it[Accounts.notes],
                    status = it[Accounts.status],
                    amount = it[Accounts.amount],
                    version = it[Accounts.version]
                )
            }
            .firstOrNull() ?: throw ResourceNotFoundException(accountId)
    }

    fun modify(id: Long, update: Accounts.(UpdateStatement) -> Unit) {
        var rowUpdated = false
        while (!rowUpdated) {
            val existingAccount = fetchById(id)
            rowUpdated = Accounts.update({ (Accounts.id eq id) and (Accounts.version eq existingAccount.version) }) {
                update(it)
                it[version] = existingAccount.version + 1L
            } != 0
        }
    }
}