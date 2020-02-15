package com.edemidov.fin.tables

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.CurrentDateTime

object Transactions : LongIdTable("transaction") {
    val timestamp = datetime("timestamp").defaultExpression(CurrentDateTime())
    val sourceAccountId = long("source_account_id").references(Accounts.id)
    val targetAccountId = long("target_account_id").references(Accounts.id)
    val amount = decimal("amount", 20, 2)
}