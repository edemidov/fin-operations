package com.edemidov.fin.api

import com.edemidov.fin.service.AccountService
import com.google.inject.Inject
import com.google.inject.Singleton
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Spark.*

@Singleton
class AccountRoutesInitializer @Inject constructor(private val accountService: AccountService) {

    fun init() {
        path("/api/v1/accounts") {
            get("/:id") { request, _ ->
                val accountId = getId(request)
                val accountResource = accountService.retrieveOne(accountId)
                toJson(accountResource)
            }
            get("") { request, _ ->
                val cursorRequest = extractCursorRequest(request)
                val cursorResponse = accountService.retrieveWithCursor(cursorRequest)
                toJson(cursorResponse)
            }
            post("") { request, response ->
                val newAccount = accountService.create(fromJsonBody(request))
                response.status(HttpStatus.CREATED_201)
                response.header("Location", request.url() + "/${newAccount.id}")
                toJson(newAccount)
            }
            put("/:id") { request, response ->
                val accountId = getId(request)
                val updatedAccount = accountService.update(accountId, fromJsonBody(request))
                response.status(HttpStatus.OK_200)
                toJson(updatedAccount)
            }
            delete("/:id") {request, response ->
                val accountId = getId(request)
                accountService.delete(accountId)
                response.status(HttpStatus.NO_CONTENT_204)
                ""
            }
        }
    }

    private fun getId(request: Request): Long {
        return request.params("id").toLong()
    }
}