package com.driivz.example.api

import kotlinx.serialization.Serializable


@Serializable
data class ChargerFindResponse(
    val requestId: String?,
    val code: String?,
    val reason: String?,
    val message: String?,
    val messages: List<Message>?,
    val httpStatusCode: Int?,
    val count: Int?,
    val data: List<Charger>?
)
