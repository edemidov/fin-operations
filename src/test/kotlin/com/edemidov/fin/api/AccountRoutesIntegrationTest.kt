@file:Suppress("MemberVisibilityCanBePrivate")

package com.edemidov.fin.api

import com.edemidov.fin.IntegrationTestExtension
import com.edemidov.fin.assertResponseHasErrors
import com.edemidov.fin.entity.AccountStatus
import com.edemidov.fin.injector
import com.edemidov.fin.port
import com.edemidov.fin.service.AccountService
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.jackson.responseObject
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import java.math.BigDecimal

@ExtendWith(IntegrationTestExtension::class)
internal class AccountRoutesIntegrationTest {
    val baseUrl = "http://localhost:$port/api/v1/accounts"

    lateinit var accountService: AccountService
    lateinit var account1: AccountResource
    lateinit var account2: AccountResource
    lateinit var account3: AccountResource

    @BeforeEach
    internal fun setUp() {
        accountService = injector.getInstance(AccountService::class.java)
        account1 = accountService.create(AccountResource("Bill", AccountStatus.ACTIVE, BigDecimal("0.00")))
        account2 = accountService.create(AccountResource("Tim", AccountStatus.ACTIVE, BigDecimal("200.00")))
        account3 = accountService.create(AccountResource("Tom", AccountStatus.BLOCKED, BigDecimal("300.00")))
    }

    @Test
    internal fun `get by id - success`() {
        val (_, response, result) = "$baseUrl/${account2.id}".httpGet()
            .responseObject<AccountResource>()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result.get()).isEqualTo(account2)
    }

    @Test
    internal fun `get by id - not found`() {
        val (_, response, _) = "$baseUrl/${Long.MAX_VALUE}".httpGet()
            .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND_404)
        assertResponseHasErrors(response, ACCOUNT_NOT_FOUND(Long.MAX_VALUE))
    }

    @Test
    internal fun `get all in one batch - success`() {
        val (_, response, result) = "$baseUrl?size=10".httpGet()
            .responseObject<CursorResponse<AccountResource>>()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result.get().content).containsExactly(account1, account2, account3)
        assertThat(result.get().nextCursor).isEqualTo(0)
    }

    @Test
    internal fun `get all in two batches - success`() {
        val (_, response1, result1) = "$baseUrl?size=2".httpGet()
            .responseObject<CursorResponse<AccountResource>>()
        assertThat(response1.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result1.get().content).containsExactly(account1, account2)
        assertThat(result1.get().nextCursor).isNotEqualTo(0)

        val (_, response2, result2) = "$baseUrl?size=2&cursor=${result1.get().nextCursor}".httpGet()
            .responseObject<CursorResponse<AccountResource>>()
        assertThat(response2.statusCode).isEqualTo(HttpStatus.OK_200)
        assertThat(result2.get().content).containsExactly(account3)
        assertThat(result2.get().nextCursor).isEqualTo(0)
    }

    @Test
    internal fun `create account - success`() {
        val newAccount = AccountResource("Sam", AccountStatus.ACTIVE, BigDecimal("50.00"))
        val (_, response, result) = baseUrl.httpPost()
            .body(toJson(newAccount))
            .responseObject<AccountResource>()
        val returnedAccount = result.get()
        val newId = returnedAccount.id!!
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED_201)
        assertThat(response.header("Location").single()).isEqualTo("$baseUrl/$newId")
        val persistedAccount = accountService.retrieveOne(newId)
        assertThat(newAccount.apply { id = newId }).isEqualTo(persistedAccount)
        assertThat(returnedAccount).isEqualTo(persistedAccount)
    }

    @Test
    internal fun `update account - success`() {
        val updatedAccount = account3.copy(status = AccountStatus.ACTIVE, amount = BigDecimal("30.00"))
        val (_, response, result) = "$baseUrl/${account3.id}".httpPut()
            .body(toJson(updatedAccount))
            .responseObject<AccountResource>()
        val returnedAccount = result.get()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK_200)
        val persistedAccount = accountService.retrieveOne(account3.id!!)
        assertThat(updatedAccount).isEqualTo(persistedAccount)
        assertThat(returnedAccount).isEqualTo(persistedAccount)
    }

    @Test
    internal fun `delete account - success`() {
        val (_, response, _) = "$baseUrl/${account1.id}".httpDelete()
            .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT_204)
        try {
            accountService.retrieveOne(account1.id!!)
        } catch (e: ResourceNotFoundException) {
            return
        }
        fail("Account was not deleted")
    }

    @Test
    internal fun `delete account with non-zero funds - fail`() {
        val (_, response, _) = "$baseUrl/${account2.id}".httpDelete()
            .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST_400)
        assertResponseHasErrors(response, ACCOUNT_WITH_FUNDS_CANNOT_BE_REMOVED(account2.id!!))
        accountService.retrieveOne(account2.id!!)
    }
}