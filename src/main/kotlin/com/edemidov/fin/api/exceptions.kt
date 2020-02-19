package com.edemidov.fin.api

import org.eclipse.jetty.http.HttpStatus

abstract class ApiException(val errors: List<ErrorDto>) : RuntimeException(errors.toString()) {
    constructor(error: ErrorDto) : this(listOf(error))
    abstract val status: Int
}

class BadRequestException(errors: List<ErrorDto>) : ApiException(errors) {
    constructor(error: ErrorDto) : this(listOf(error))
    override val status = HttpStatus.BAD_REQUEST_400
}

class ResourceNotFoundException(error: ErrorDto) : ApiException(error) {
    override val status = HttpStatus.NOT_FOUND_404
}