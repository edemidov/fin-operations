package com.edemidov.fin.api

import com.edemidov.fin.entity.AccountStatus
import java.math.BigDecimal
import java.time.Instant

interface IdentifiableResource {
    var id: Long?
}

data class AccountResource(val name: String,
                           val status: AccountStatus,
                           val amount: BigDecimal,
                           override var id: Long? = null) : IdentifiableResource

data class TransactionResource(val sourceAccountId: Long,
                               val targetAccountId: Long,
                               val amount: BigDecimal,
                               override var id: Long? = null,
                               var operationTime: Instant? = null) : IdentifiableResource

data class ErrorResource(val errors: List<ErrorDto>)

data class ErrorDto(val code: Int, val message: String)