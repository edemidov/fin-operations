package com.edemidov.fin.entity

import org.joda.time.DateTime
import java.math.BigDecimal

enum class AccountStatus {
    ACTIVE, BLOCKED
}

data class Account(val id: Long,
                   val name: String,
                   val notes: String? = null,
                   val status: AccountStatus = AccountStatus.ACTIVE,
                   val amount: BigDecimal = BigDecimal.ZERO,
                   val version: Long = 1L)

data class Transaction(val id: Long,
                       val timestamp: DateTime,
                       val sourceAccountId: Long,
                       val targetAccountId: Long,
                       val amount: BigDecimal)