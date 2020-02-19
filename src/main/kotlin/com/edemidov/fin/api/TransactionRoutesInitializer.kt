package com.edemidov.fin.api

import com.edemidov.fin.service.TransactionService
import com.google.inject.Inject
import com.google.inject.Singleton
import org.eclipse.jetty.http.HttpStatus
import spark.Spark.*

@Singleton
class TransactionRoutesInitializer @Inject constructor(private val transactionService: TransactionService) {
    fun init() {
        path("/api/v1/transactions") {
            get("/:id") { request, _ ->
                val transactionId = request.params("id").toLong()
                val transactionResource = transactionService.retrieveOne(transactionId)
                toJson(transactionResource)
            }
            get("") { request, _ ->
                val cursorRequest = extractCursorRequest(request)
                val sourceAccountId = request.queryParams("sourceAccountId")?.toLong()
                val targetAccountId = request.queryParams("targetAccountId")?.toLong()
                val cursorResponse = transactionService.retrieveWithCursor(cursorRequest, sourceAccountId, targetAccountId)
                toJson(cursorResponse)
            }
            post("") { request, response ->
                val transaction = transactionService.makeTransaction(fromJsonBody(request))
                response.status(HttpStatus.CREATED_201)
                response.header("Location", request.url() + "/${transaction.id}")
                toJson(transaction)
            }
        }
    }
}