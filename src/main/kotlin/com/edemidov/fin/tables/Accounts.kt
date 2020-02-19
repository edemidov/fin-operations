package com.edemidov.fin.tables

import com.edemidov.fin.entity.AccountStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.booleanLiteral
import org.jetbrains.exposed.sql.longLiteral

object Accounts : LongIdTable("account") {
    val name = varchar("name", 100)
    val status = customEnumeration("status", "ACCOUNT_STATUS",
        {value -> AccountStatus.valueOf(value as String)}, { PGEnum("ACCOUNT_STATUS", it)})
    val amount = decimal("amount", 20, 2)
    val version = long("version").defaultExpression(longLiteral(1))
    val removed = bool("removed").defaultExpression(booleanLiteral(false))
}