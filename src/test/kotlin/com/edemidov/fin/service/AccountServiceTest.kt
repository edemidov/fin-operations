package com.edemidov.fin.service

import com.edemidov.fin.api.ACCOUNT_NOT_FOUND
import com.edemidov.fin.api.AccountResource
import com.edemidov.fin.api.ResourceNotFoundException
import com.edemidov.fin.entity.Account
import com.edemidov.fin.entity.AccountStatus
import com.edemidov.fin.repository.AccountRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
internal class AccountServiceTest(@MockK val accountRepository: AccountRepository) {

    private val accountService = AccountService(accountRepository)

    init {
        Database.connect({ mockk() })
    }

    @Test
    fun `retrieve one - success`() {
        val money = BigDecimal("10.10")
        every { accountRepository.fetchById(15) }
            .returns(Account(15, "Bill", AccountStatus.ACTIVE, money))
        val account = accountService.retrieveOne(15L)
        with(account) {
            assertThat(id).isEqualTo(15)
            assertThat(name).isEqualTo("Bill")
            assertThat(status).isEqualTo(AccountStatus.ACTIVE)
            assertThat(amount).isEqualTo(money)
        }
    }

    @Test
    fun `retrieve one - fail with not found`() {
        every { accountRepository.fetchById(15) }
            .returns(null)

        try {
            accountService.retrieveOne(15L)
        } catch (e: ResourceNotFoundException) {
            assertThat(e.errors).containsOnly(ACCOUNT_NOT_FOUND(15))
            return
        }
        fail("Exception expected")
    }

    @Test
    fun `create account - success`() {
        val account = AccountResource("Tom", AccountStatus.BLOCKED, BigDecimal("1.50"))
        every { accountRepository.create(Account(0, "Tom", AccountStatus.BLOCKED, BigDecimal("1.50"))) }
            .returns(6)

        val newAccount = accountService.create(account)
        assertThat(newAccount).isEqualTo(account.apply { id = 6 })
    }

    @Test
    fun `update account - success`() {
        val account = AccountResource("Tom", AccountStatus.BLOCKED, BigDecimal("1.50"))
        every { accountRepository.modify(6, null, any()) } just runs

        val updatedAccount = accountService.update(6, account)
        assertThat(updatedAccount).isEqualTo(account.apply { id = 6 })
        verify {
            accountRepository.modify(6, any(), any())
        }
    }

    @Test
    fun `delete account - success`() {
        every { accountRepository.modify(6, any(), any()) } just runs

        accountService.delete(6)
        verify {
            accountRepository.modify(6, any(), any())
        }
    }
}