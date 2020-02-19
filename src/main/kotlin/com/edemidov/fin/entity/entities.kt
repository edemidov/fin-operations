package com.edemidov.fin.entity

import java.math.BigDecimal
import java.time.Instant

enum class AccountStatus {
    ACTIVE, BLOCKED
}

data class Account(val id: Long,
                   val name: String,
                   val status: AccountStatus = AccountStatus.ACTIVE,
                   val amount: BigDecimal = BigDecimal.ZERO,
                   val version: Long = 1L)

data class Transaction(val id: Long,
                       val operationTime: Instant?,
                       val sourceAccountId: Long,
                       val targetAccountId: Long,
                       val amount: BigDecimal)