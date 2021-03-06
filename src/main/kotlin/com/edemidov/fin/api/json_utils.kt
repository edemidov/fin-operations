package com.edemidov.fin.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import spark.Request

val mapper: ObjectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun toJson(obj: Any): String {
    return mapper.writeValueAsString(obj)
}

inline fun <reified T> fromJsonBody(request: Request): T {
    return mapper.readValue(request.bodyAsBytes())
}