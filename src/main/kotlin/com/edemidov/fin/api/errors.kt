package com.edemidov.fin.api

val UNEXPECTED_ERROR_OCCURRED = { ex: Exception -> ErrorDto(0, "Unexpected error occurred: ${ex.message}") }

val ACCOUNT_NOT_FOUND = { accountId: Long -> ErrorDto(1, "Account with id = $accountId is not found") }
val TRANSACTION_NOT_FOUND = { transactionId: Long -> ErrorDto(2, "Transaction with id = $transactionId is not found") }
val ACCOUNT_WITH_FUNDS_CANNOT_BE_REMOVED = { accountId: Long -> ErrorDto(3, "Account with id = $accountId has non-zero funds, thus it cannot be removed") }
val ACCOUNT_BLOCKED = { accountId: Long -> ErrorDto(4, "Account with id = $accountId is blocked") }
val INSUFFICIENT_FUNDS = { accountId: Long -> ErrorDto(5, "Account with id = $accountId has insufficient funds for the operation") }
val SELF_TRANSACTION = { accountId: Long -> ErrorDto(6, "Transaction's source and target accounts have the same id $accountId") }

val JSON_PARSE_ERROR = { message: String -> ErrorDto(1000, "Failed to parse the request payload: $message") }
val WRONG_SIZE_PARAMETER_VALUE = { range: IntRange -> ErrorDto(1001, "Size parameter must be in $range range") }
val ZERO_CURSOR_VALUE = { ErrorDto(1002, "Cursor parameter must not be 0, it looks like you already reached the end of the result set") }