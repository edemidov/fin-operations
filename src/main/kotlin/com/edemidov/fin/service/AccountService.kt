package com.edemidov.fin.service

import com.edemidov.fin.api.AccountResource
import com.edemidov.fin.entity.Account
import com.edemidov.fin.repository.AccountRepository
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.transactions.transaction

@Singleton
class AccountService @Inject constructor(private val accountRepository: AccountRepository) {

    fun createAccount(account: AccountResource): Long {
        return transaction {
            accountRepository.create(Account(0L, account.name, account.notes, account.status, account.amount))
        }
    }

    fun getAccount(accountId: Long): AccountResource {
        return transaction {
            accountRepository.fetchById(accountId).let { account ->
                AccountResource().apply {
                    id = account.id
                    name = account.name
                    notes = account.notes
                    status = account.status
                    amount = account.amount
                }
            }
        }
    }

    fun updateAccount(accountId: Long, account: AccountResource) {
        return transaction {
            accountRepository.modify(accountId) { update ->
                update[name] = account.name
                update[notes] = account.notes
                update[status] = account.status
                update[amount] = account.amount
            }
        }
    }

    fun patchAccount(accountId: Long, account: AccountResource) {
        return transaction {
            accountRepository.modify(accountId) { update ->
                if (account.hasName()) {
                    update[name] = account.name
                }
                if (account.hasNotes()) {
                    update[notes] = account.notes
                }
                if (account.hasStatus()) {
                    update[status] = account.status
                }
                if (account.hasAmount()) {
                    update[amount] = account.amount
                }
            }
        }
    }

    fun deleteAccount(accountId: Long) {
        transaction {
            accountRepository.modify(accountId) {update ->
                update[removed] = true
            }
        }
    }
}