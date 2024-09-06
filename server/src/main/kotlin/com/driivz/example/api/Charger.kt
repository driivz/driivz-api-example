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

@Serializable
data class ChargerFindRequest (
    val ids: List<Int>?
)



@Serializable
data class ChargerProfileFindResponse(
    val requestId: String?,
    val code: String?,
    val reason: String?,
    val message: String?,
    val messages: List<Message>?,
    val httpStatusCode: Int?,
    val count: Int?,
    val data: List<ChargerProfile>?
)

@Serializable
data class Connector(
    val id: Long
)

@Serializable
data class Evse(
    val identityKey: String,
    val connectors: List<Connector>
)

@Serializable
data class ChargerProfile(
    val evses: List<Evse>?
)


@Serializable
data class OneTimePaymentStartTransactionResponse(
    val requestId: String?,
    val code: String?,
    val reason: String?,
    val message: String?,
    val messages: List<Message>?,
    val httpStatusCode: Int?,
    val count: Int?,
    val data: List<OneTimePaymentStartTransaction>?
)



