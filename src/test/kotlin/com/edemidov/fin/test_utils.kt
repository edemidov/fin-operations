package com.edemidov.fin

import com.edemidov.fin.api.ErrorDto
import com.edemidov.fin.api.ErrorResource
import com.edemidov.fin.api.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Response
import com.google.inject.Injector
import org.assertj.core.api.Assertions.assertThat

var port = 0
lateinit var injector: Injector


inline fun <reified T> fromJsonResponse(response: Response): T {
    return mapper.readValue(String(response.body().toByteArray()))
}

fun assertResponseHasErrors(response: Response, vararg errors: ErrorDto) {
    val errorResource = fromJsonResponse<ErrorResource>(response)
    assertThat(errorResource.errors).containsExactlyInAnyOrder(*errors)
}