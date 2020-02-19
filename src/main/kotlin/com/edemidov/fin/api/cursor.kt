package com.edemidov.fin.api

import spark.Request

val validSizeRange = 1..MAX_PAGE_SIZE

fun extractCursorRequest(request: Request): CursorRequest {
    val size = request.queryParams("size")?.toInt() ?: DEFAULT_PAGE_SIZE
    val cursor = request.queryParams("cursor")?.toLong()
    if (cursor == 0L) {
        throw BadRequestException(ZERO_CURSOR_VALUE())
    }
    return CursorRequest(cursor ?: 0, size)
}

fun <T : IdentifiableResource> toCursorResponse(fetchedResources: List<T>, request: CursorRequest): CursorResponse<T> {
    val thereAreMoreEntitiesToFetch = fetchedResources.size == request.numberOfEntitiesToFetch
    return if (thereAreMoreEntitiesToFetch) {
        val content = fetchedResources.subList(0, fetchedResources.lastIndex)
        CursorResponse(content, content.last().id!!)
    } else {
        CursorResponse(fetchedResources, 0)
    }
}

data class CursorRequest(val cursor: Long, val size: Int) {
    init {
        if (size !in validSizeRange) {
            throw BadRequestException(WRONG_SIZE_PARAMETER_VALUE(validSizeRange))
        }
    }

    val numberOfEntitiesToFetch
        get() = size + 1
}

@Suppress("unused")
data class CursorResponse<T : IdentifiableResource>(val content: List<T>, val nextCursor: Long)