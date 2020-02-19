package com.edemidov.fin.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Transactions : LongIdTable("transaction") {
    val operationTime = timestamp("operation_time").defaultExpression(CurrentTimestamp())
    val sourceAccountId = long("source_account_id").references(Accounts.id)
    val targetAccountId = long("target_account_id").references(Accounts.id)
    val amount = decimal("amount", 20, 2)
}