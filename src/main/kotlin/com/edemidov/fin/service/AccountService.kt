package com.edemidov.fin.service

import com.edemidov.fin.api.*
import com.edemidov.fin.entity.Account
import com.edemidov.fin.repository.AccountRepository
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

@Singleton
class AccountService @Inject constructor(private val accountRepository: AccountRepository) {

    fun create(account: AccountResource): AccountResource {
        return transaction {
            val newId = accountRepository.create(
                Account(
                    id = 0L,
                    name = account.name,
                    status = account.status,
                    amount = account.amount
                )
            )
            account.apply { id = newId }
        }
    }

    fun retrieveOne(accountId: Long): AccountResource {
        return transaction {
            accountRepository.fetchById(accountId)
                ?.let(::accountToResource) ?: throw throw ResourceNotFoundException(ACCOUNT_NOT_FOUND(accountId))
        }
    }

    fun retrieveWithCursor(cursorRequest: CursorRequest): CursorResponse<AccountResource> {
        return transaction {
            val fetchedResources = accountRepository.fetch(cursorRequest.cursor, cursorRequest.numberOfEntitiesToFetch)
                .map(::accountToResource)
            toCursorResponse(fetchedResources, cursorRequest)
        }
    }

    fun update(accountId: Long, account: AccountResource): AccountResource {
        return transaction {
            accountRepository.modify(accountId) { update, _ ->
                update[name] = account.name
                update[status] = account.status
                update[amount] = account.amount
            }
            account.apply { id = accountId }
        }
    }

    fun delete(accountId: Long) {
        transaction {
            accountRepository.modify(accountId, ::validateBeforeDelete) { update, _ ->
                update[removed] = true
            }
        }
    }

    private fun validateBeforeDelete(account: Account) {
        if (account.amount != BigDecimal("0.00")) {
            throw BadRequestException(ACCOUNT_WITH_FUNDS_CANNOT_BE_REMOVED(account.id))
        }
    }

    private fun accountToResource(account: Account) =
        AccountResource(
            id = account.id,
            name = account.name,
            status = account.status,
            amount = account.amount
        )
}