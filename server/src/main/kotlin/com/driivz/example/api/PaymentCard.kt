package com.driivz.example.api

import kotlinx.serialization.Serializable

@Serializable
data class PaymentCardResponse(
    val requestId: String?,
    val code: String?,
    val reason: String?,
    val message: String?,
    val messages: List<Message>?,
    val httpStatusCode: Int?,
    val count: Int?,
    val data: List<PaymentCard>?
)

@Serializable
data class Message(
    val code: String?,
    val reason: String?,
    val message: String?
)

@Serializable
data class AddPaymentCardResponse(
    val requestId: String,
    val code: String,
    val reason: String,
    val message: String,
    val messages: List<Message>,
    val httpStatusCode: Int,
    val count: Int,
    val data: List<PaymentCard>
)
