package com.edemidov.fin.api

abstract class ApiException(message: String) : RuntimeException(message) {
    abstract val status: Int
}

class ResourceNotFoundException(id: Any) : ApiException("Resource with id = $id is not found") {
    override val status = 404
}