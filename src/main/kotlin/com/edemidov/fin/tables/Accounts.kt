package com.edemidov.fin.tables

import com.edemidov.fin.entity.AccountStatus
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.booleanLiteral
import org.jetbrains.exposed.sql.longLiteral

object Accounts : LongIdTable("account") {
    val name = varchar("name", 100)
    val notes = varchar("notes", 1000).nullable()
    val status = customEnumeration("status", "ENUM('ACTIVE', 'BLOCKED')",
        { AccountStatus.values()[it as Int] }, { it.name })
    val amount = decimal("amount", 20, 2)
    val version = long("version").defaultExpression(longLiteral(1))
    val removed = bool("removed").defaultExpression(booleanLiteral(false))
}