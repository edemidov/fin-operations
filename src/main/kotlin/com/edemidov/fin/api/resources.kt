package com.edemidov.fin.api

import com.edemidov.fin.entity.AccountStatus
import org.joda.time.DateTime
import java.math.BigDecimal

class AccountResource(val backingMap: MutableMap<String, Any?> = mutableMapOf()) {
    var id: Long by backingMap
    fun hasId() = backingMap.containsKey(::id.name)

    var name: String by backingMap
    fun hasName() = backingMap.containsKey(::name.name)

    var notes: String? by backingMap
    fun hasNotes() = backingMap.containsKey(::notes.name)

    var status: AccountStatus by backingMap
    fun hasStatus() = backingMap.containsKey(::status.name)

    var amount: BigDecimal by backingMap
    fun hasAmount() = backingMap.containsKey(::amount.name)
}

data class Transaction(val id: Long,
                       val timestamp: DateTime,
                       val sourceAccountId: Long,
                       val targetAccountId: Long,
                       val amount: BigDecimal)