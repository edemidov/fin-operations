package com.edemidov.fin.service

import com.edemidov.fin.repository.TransactionRepository
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class TransactionService @Inject constructor(transactionRepository: TransactionRepository) {
}