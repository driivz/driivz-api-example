package com.driivz.example.log

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.request.uri
import io.ktor.server.response.ApplicationSendPipeline
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<Unit, ApplicationCall>.logRequestAndResponse() {
    val logger = call.application.log

    // Log the incoming request
    val requestBody = call.receiveText()
    logger.info("Received Request: ${call.request.uri}")
    logger.info("Request Body: $requestBody")

    // Process the request and get the response
    proceed()

    // Log the outgoing response
    val response = call.response
    response.pipeline.intercept(ApplicationSendPipeline.After) {
        val responseBody = it as? String ?: ""
        logger.info("Sending Response: ${response.status()}")
        logger.info("Response Body: $responseBody")
    }
}